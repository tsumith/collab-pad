package com.sumith.collabpad.service.impl;

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
import com.sumith.collabpad.service.DocumentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final TenantRepository tenantRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    // We inject all 4 repositories to tie everything together!
    public DocumentServiceImpl(DocumentRepository documentRepository,
                               TenantRepository tenantRepository,
                               WorkspaceRepository workspaceRepository,
                               UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.tenantRepository = tenantRepository;
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public DocumentResponse createDocument(Long tenantId, Long workspaceId, Long authorId, DocumentRequest request) {
        
        // 1. Fetch our required entities
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
                
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
                
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. Security Checks!
        if (!workspace.getTenant().getId().equals(tenantId)) {
            throw new SecurityException("Workspace does not belong to this tenant!");
        }
        if (!author.getTenant().getId().equals(tenantId)) {
            throw new SecurityException("User does not belong to this tenant!");
        }

        // 3. Create and link the Document
        Document document = new Document();
        document.setTitle(request.title());
        document.setContent(request.content());
        document.setTenant(tenant);
        document.setWorkspace(workspace);
        document.setAuthor(author);

        // 4. Save
        Document savedDocument = documentRepository.save(document);
        return mapToResponse(savedDocument);
    }

    @Override
    public DocumentResponse getDocumentById(Long tenantId, Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));
                
        // VERY IMPORTANT: Manually enforce data isolation if we didn't use a strict repository method
        if (!document.getTenant().getId().equals(tenantId)) {
             throw new SecurityException("Unauthorized access to document!");
        }
        
        return mapToResponse(document);
    }

    @Override
    public List<DocumentResponse> getAllDocuments(Long tenantId, Long workspaceId) {
        // We use the custom repository method we wrote earlier!
        return documentRepository.findByWorkspaceIdAndTenantId(workspaceId, tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
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

    @Override
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
