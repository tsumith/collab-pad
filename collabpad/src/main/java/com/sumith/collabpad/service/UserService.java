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

        return userRepository.findByEmail(finalEmail).orElseThrow(() -> 
            new RuntimeException("USER_NOT_ENROLLED")
        );
    }
}
