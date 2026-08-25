package com.sumith.collabpad.repository;

import com.sumith.collabpad.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByDocumentId(UUID documentId);

    Optional<Document> findByDocumentIdAndTenantId(UUID documentId, Long tenantId);

    // all documents in a specific workspace, isolated by tenant
    List<Document> findByWorkspaceIdAndTenantId(Long workspaceId, Long tenantId);
}
