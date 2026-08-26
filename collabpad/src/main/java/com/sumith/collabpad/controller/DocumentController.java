package com.sumith.collabpad.controller;

import com.sumith.collabpad.dto.DocumentRequest;
import com.sumith.collabpad.dto.DocumentResponse;
import com.sumith.collabpad.service.DocumentService;
import com.sumith.collabpad.service.UserService;
import com.sumith.collabpad.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@CrossOrigin(origins = "*") // Added to allow our React app to access the API!
public class DocumentController {

    private final DocumentService documentService;
    private final UserService userService;

    public DocumentController(DocumentService documentService, UserService userService) {
        this.documentService = documentService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DocumentRequest request) {

        // Extract the tenant_id from JWT
        Long tenantId = Long.parseLong(jwt.getClaimAsStringList("tenant_id").get(0));

        // Dynamically fetch or create the user in our Postgres Database based on their
        // Keycloak Token!
        User user = userService.getOrCreateUserFromJwt(jwt);
        Long authorId = user.getId();

        DocumentResponse response = documentService.createDocument(tenantId, request.workspaceId(), authorId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        Long tenantId = Long.parseLong(jwt.getClaimAsStringList("tenant_id").get(0));
        DocumentResponse response = documentService.getDocumentById(tenantId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long workspaceId) {
        Long tenantId = Long.parseLong(jwt.getClaimAsStringList("tenant_id").get(0));
        List<DocumentResponse> responses = documentService.getAllDocuments(tenantId, workspaceId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequest request) {
        Long tenantId = Long.parseLong(jwt.getClaimAsStringList("tenant_id").get(0));
        DocumentResponse response = documentService.updateDocument(tenantId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        Long tenantId = Long.parseLong(jwt.getClaimAsStringList("tenant_id").get(0));
        documentService.deleteDocument(tenantId, id);
        return ResponseEntity.noContent().build();
    }
}
