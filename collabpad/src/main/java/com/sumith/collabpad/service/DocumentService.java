package com.sumith.collabpad.service;

import com.sumith.collabpad.dto.DocumentRequest;
import com.sumith.collabpad.dto.DocumentResponse;
import com.sumith.collabpad.entity.Document;
import com.sumith.collabpad.entity.Tenant;
import com.sumith.collabpad.entity.User;
import com.sumith.collabpad.entity.Workspace;
import com.sumith.collabpad.exception.DocumentNotFoundException;
import com.sumith.collabpad.repository.DocumentRepository;
import com.sumith.collabpad.repository.TenantRepository;
import com.sumith.collabpad.repository.UserRepository;
import com.sumith.collabpad.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final TenantRepository tenantRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    public DocumentService(DocumentRepository documentRepository,
                           TenantRepository tenantRepository,
                           WorkspaceRepository workspaceRepository,
                           UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.tenantRepository = tenantRepository;
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DocumentResponse createDocument(Long tenantId, Long workspaceId, Long authorId, DocumentRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
                
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
                
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!workspace.getTenant().getId().equals(tenantId)) {
            throw new SecurityException("Workspace does not belong to this tenant!");
        }
        if (!author.getTenant().getId().equals(tenantId)) {
            throw new SecurityException("User does not belong to this tenant!");
        }

        Document document = new Document();
        document.setTitle(request.title());
        document.setContent(request.content());
        document.setTenant(tenant);
        document.setWorkspace(workspace);
        document.setAuthor(author);

        Document savedDocument = documentRepository.save(document);
        return mapToResponse(savedDocument);
    }

    public DocumentResponse getDocumentById(Long tenantId, Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));
                
        if (!document.getTenant().getId().equals(tenantId)) {
             throw new SecurityException("Unauthorized access to document!");
        }
        
        return mapToResponse(document);
    }

    public List<DocumentResponse> getAllDocuments(Long tenantId, Long workspaceId) {
        return documentRepository.findByWorkspaceIdAndTenantId(workspaceId, tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DocumentResponse updateDocument(Long tenantId, Long id, DocumentRequest request) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));
                
        if (!document.getTenant().getId().equals(tenantId)) {
             throw new SecurityException("Unauthorized access to document!");
        }
        
        document.setTitle(request.title());
        document.setContent(request.content());
        
        Document updatedDocument = documentRepository.save(document);
        return mapToResponse(updatedDocument);
    }

    @Transactional
    public void deleteDocument(Long tenantId, Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));
                
        if (!document.getTenant().getId().equals(tenantId)) {
             throw new SecurityException("Unauthorized access to document!");
        }
        
        documentRepository.delete(document);
    }

    private DocumentResponse mapToResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
