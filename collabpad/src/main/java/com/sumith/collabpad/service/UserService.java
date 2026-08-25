package com.sumith.collabpad.service;

import com.sumith.collabpad.entity.Tenant;
import com.sumith.collabpad.entity.User;
import com.sumith.collabpad.repository.TenantRepository;
import com.sumith.collabpad.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    public UserService(UserRepository userRepository, TenantRepository tenantRepository) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public User createUser(Long tenantId, String name, String email, String role) {

        // A user cannot exist without a tenant
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + tenantId));

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setTenant(tenant);

        return userRepository.save(user);
    }

    public List<User> getUsersByTenant(Long tenantId) {
        return userRepository.findByTenantId(tenantId);
    }
}
