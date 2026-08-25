package com.sumith.collabpad.repository;

import com.sumith.collabpad.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUserId(UUID userId);
    
    Optional<User> findByEmail(String email);

    // Find all users belonging to a specific tenant
    List<User> findByTenantId(Long tenantId);
}
