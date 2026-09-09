package com.sumith.collabpad.service;

import com.sumith.collabpad.dto.DocumentRequest;
import com.sumith.collabpad.dto.DocumentResponse;
import com.sumith.collabpad.dto.DocumentNodeResponse;
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
import java.util.ArrayList;
import java.util.Map;
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
        
        document.setIcon(request.icon());
        document.setCoverImageUrl(request.coverImageUrl());
        document.setPositionOrder(request.positionOrder());
        
        if (request.parentId() != null) {
            Document parent = documentRepository.findById(request.parentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent document not found"));
            if (!parent.getWorkspace().getId().equals(workspaceId)) {
                throw new SecurityException("Parent document must be in the same workspace");
            }
            document.setParentDocument(parent);
        }

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

    public List<DocumentNodeResponse> getDocumentTree(Long tenantId, Long workspaceId) {
        List<Document> allDocs = documentRepository.findByWorkspaceIdAndTenantId(workspaceId, tenantId);
        
        Map<Long, DocumentNodeResponse> nodeMap = allDocs.stream()
                .collect(Collectors.toMap(
                        Document::getId,
                        doc -> new DocumentNodeResponse(
                                doc.getId(),
                                doc.getTitle(),
                                doc.getIcon(),
                                doc.getCoverImageUrl(),
                                doc.getPositionOrder(),
                                doc.getCreatedAt(),
                                doc.getUpdatedAt(),
                                new ArrayList<>() // mutable list for children
                        )
                ));
                
        List<DocumentNodeResponse> roots = new ArrayList<>();
        
        for (Document doc : allDocs) {
            DocumentNodeResponse node = nodeMap.get(doc.getId());
            if (doc.getParentDocument() == null) {
                roots.add(node);
            } else {
                DocumentNodeResponse parentNode = nodeMap.get(doc.getParentDocument().getId());
                if (parentNode != null) {
                    parentNode.children().add(node);
                } else {
                    roots.add(node);
                }
            }
        }
        
        sortNodes(roots);
        return roots;
    }
    
    private void sortNodes(List<DocumentNodeResponse> nodes) {
        nodes.sort((a, b) -> {
            if (a.positionOrder() == null && b.positionOrder() == null) return 0;
            if (a.positionOrder() == null) return 1;
            if (b.positionOrder() == null) return -1;
            return a.positionOrder().compareTo(b.positionOrder());
        });
        for (DocumentNodeResponse node : nodes) {
            if (!node.children().isEmpty()) {
                sortNodes(node.children());
            }
        }
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
        document.setIcon(request.icon());
        document.setCoverImageUrl(request.coverImageUrl());
        document.setPositionOrder(request.positionOrder());
        
        if (request.parentId() != null) {
            if (request.parentId().equals(id)) {
                throw new IllegalArgumentException("Document cannot be its own parent");
            }
            Document parent = documentRepository.findById(request.parentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent document not found"));
            if (!parent.getWorkspace().getId().equals(document.getWorkspace().getId())) {
                throw new SecurityException("Parent document must be in the same workspace");
            }
            document.setParentDocument(parent);
        } else {
            document.setParentDocument(null);
        }
        
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
                document.getParentDocument() != null ? document.getParentDocument().getId() : null,
                document.getIcon(),
                document.getCoverImageUrl(),
                document.getPositionOrder(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
