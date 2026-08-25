package com.sumith.collabpad.service;

import com.sumith.collabpad.entity.Tenant;
import com.sumith.collabpad.entity.User;
import com.sumith.collabpad.entity.Workspace;
import com.sumith.collabpad.repository.TenantRepository;
import com.sumith.collabpad.repository.UserRepository;
import com.sumith.collabpad.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository,
            TenantRepository tenantRepository,
            UserRepository userRepository) {
        this.workspaceRepository = workspaceRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Workspace createWorkspace(Long tenantId, Long ownerId, String name, String description) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Make sure the owner actually belongs to the tenant
        if (!owner.getTenant().getId().equals(tenant.getId())) {
            throw new SecurityException("User does not belong to this tenant");
        }

        Workspace workspace = new Workspace();
        workspace.setName(name);
        workspace.setDescription(description);
        workspace.setTenant(tenant);
        workspace.setOwner(owner);

        return workspaceRepository.save(workspace);
    }

    public List<Workspace> getWorkspacesByTenant(Long tenantId) {
        return workspaceRepository.findByTenantId(tenantId);
    }
}
