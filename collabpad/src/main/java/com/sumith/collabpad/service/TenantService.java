package com.sumith.collabpad.service;

import com.sumith.collabpad.entity.Tenant;
import com.sumith.collabpad.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public Tenant createTenant(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant name cannot be empty");
        }

        Tenant tenant = new Tenant();
        tenant.setName(name);

        return tenantRepository.save(tenant);
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Optional<Tenant> getTenantById(UUID tenantId) {
        return tenantRepository.findByTenantId(tenantId);
    }
}
