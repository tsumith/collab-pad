package com.sumith.collabpad.repository;

import com.sumith.collabpad.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    Optional<Workspace> findByWorkspaceId(UUID workspaceId);

    Optional<Workspace> findByWorkspaceIdAndTenantId(UUID workspaceId, Long tenantId);

    // all workspaces for a specific tenant
    List<Workspace> findByTenantId(Long tenantId);
}
