package com.sumith.collabpad.service;

import com.sumith.collabpad.dto.EnrollmentRequest;
import com.sumith.collabpad.entity.Document;
import com.sumith.collabpad.entity.Tenant;
import com.sumith.collabpad.entity.User;
import com.sumith.collabpad.entity.Workspace;
import com.sumith.collabpad.repository.DocumentRepository;
import com.sumith.collabpad.repository.TenantRepository;
import com.sumith.collabpad.repository.UserRepository;
import com.sumith.collabpad.repository.WorkspaceRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OnboardingService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final WorkspaceRepository workspaceRepository;
    private final DocumentRepository documentRepository;
    private final UserService userService;

    public OnboardingService(UserRepository userRepository, TenantRepository tenantRepository,
                             WorkspaceRepository workspaceRepository, DocumentRepository documentRepository,
                             UserService userService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.workspaceRepository = workspaceRepository;
        this.documentRepository = documentRepository;
        this.userService = userService;
    }

    @Transactional
    public User enrollCompany(Jwt jwt, EnrollmentRequest request) {
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            email = jwt.getClaimAsString("preferred_username") + "@example.com";
        }
        
        final String finalEmail = email;
        
        User user = userRepository.findByEmail(finalEmail).orElse(null);
        Tenant tenant;
        
        if (user == null) {
            // First time ever seeing this user
            tenant = new Tenant();
            tenant.setName(request.getCompanyName());
            tenant = tenantRepository.save(tenant);
            
            user = new User();
            user.setEmail(finalEmail);
            String name = jwt.getClaimAsString("name");
            if (name == null) {
                name = jwt.getClaimAsString("preferred_username");
            }
            user.setName(name);
            user.setRole("ROLE_COMPANY_ADMIN");
            user.setTenant(tenant);
            user = userRepository.save(user);
        } else {
            // User exists (maybe they got a personal tenant via fallback), update their tenant
            tenant = user.getTenant();
            tenant.setName(request.getCompanyName());
            tenantRepository.save(tenant);
            
            user.setRole("ROLE_COMPANY_ADMIN");
            userRepository.save(user);
        }

        // Bootstrap selected workspaces
        List<String> departments = request.getSelectedDepartments();
        if (departments != null && !departments.isEmpty()) {
            for (String dept : departments) {
                Workspace workspace = new Workspace();
                workspace.setName(dept);
                workspace.setTenant(tenant);
                workspace.setOwner(user);
                workspace = workspaceRepository.save(workspace);
                
                Document doc = new Document();
                doc.setTitle("Welcome to " + dept);
                doc.setContent("This is the default starting page for the " + dept + " department.");
                doc.setTenant(tenant);
                doc.setWorkspace(workspace);
                doc.setAuthor(user);
                documentRepository.save(doc);
            }
        }
        
        return user;
    }
}
