package com.sumith.collabpad.controller;

import com.sumith.collabpad.dto.DocumentRequest;
import com.sumith.collabpad.dto.DocumentResponse;
import com.sumith.collabpad.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // In Stage 6, these X-Headers will be replaced by extracting IDs securely from the Keycloak JWT Token.
    // For now, we simulate authentication by passing them as custom HTTP headers.
    
    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long authorId,
            @Valid @RequestBody DocumentRequest request) {
            
        // We pass the secure context down to the Service Layer
        // Notice the parentheses on request.workspaceId()!
        DocumentResponse response = documentService.createDocument(tenantId, request.workspaceId(), authorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @PathVariable Long id) {
            
        DocumentResponse response = documentService.getDocumentById(tenantId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam("workspaceId") Long workspaceId) {
            
        List<DocumentResponse> responses = documentService.getAllDocuments(tenantId, workspaceId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequest request) {
            
        DocumentResponse response = documentService.updateDocument(tenantId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @PathVariable Long id) {
            
        documentService.deleteDocument(tenantId, id);
        return ResponseEntity.noContent().build();
    }
}
