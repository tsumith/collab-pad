package com.sumith.collabpad.service;

import com.sumith.collabpad.entity.Tenant;
import com.sumith.collabpad.entity.User;
import com.sumith.collabpad.repository.TenantRepository;
import com.sumith.collabpad.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sumith.collabpad.entity.Workspace;
import com.sumith.collabpad.repository.WorkspaceRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final WorkspaceRepository workspaceRepository;

    public UserService(UserRepository userRepository, TenantRepository tenantRepository,
            WorkspaceRepository workspaceRepository) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Transactional
    public User getOrCreateUserFromJwt(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            // Fallback if email is missing (for our dummy testuser)
            email = jwt.getClaimAsString("preferred_username") + "@example.com";
        }

        final String finalEmail = email;

        return userRepository.findByEmail(finalEmail).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(jwt.getClaimAsString("email") != null ? jwt.getClaimAsString("email") : finalEmail);
            String name = jwt.getClaimAsString("name");
            if (name == null) {
                name = jwt.getClaimAsString("preferred_username");
            }
            // Instead of extracting a hardcoded tenant from Keycloak,
            // we instantly auto-provision a personal Tenant for them! (Notion style)
            Tenant newTenant = new Tenant();
            newTenant.setName(newUser.getName() + "'s Workspace");
            newTenant = tenantRepository.save(newTenant);

            // Create a default Workspace inside their new Tenant
            Workspace newWorkspace = new Workspace();
            newWorkspace.setName("General");
            newWorkspace.setTenant(newTenant);
            workspaceRepository.save(newWorkspace);

            newUser.setRole("ROLE_TENANT_USER");
            newUser.setTenant(newTenant);

            return userRepository.save(newUser);
        });
    }
}
