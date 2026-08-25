package com.sumith.collabpad.controller;

import com.sumith.collabpad.dto.DocumentRequest;
import com.sumith.collabpad.dto.DocumentResponse;
import com.sumith.collabpad.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Document Management", description = "Endpoints for managing documents in the system")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @Operation(summary = "Create a new document", description = "Registers a new collaborative document in the database.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Document created successfully",
            content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data supplied")
    })
    public ResponseEntity<DocumentResponse> createDocument(@Valid @RequestBody DocumentRequest request) {
        DocumentResponse response = documentService.createDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a document by ID", description = "Retrieves metadata and content for a single document.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document found and returned",
            content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Document not found with the given ID")
    })
    public ResponseEntity<DocumentResponse> getDocumentById(
            @Parameter(description = "ID of the document to retrieve", example = "1")
            @PathVariable Long id) {
        DocumentResponse response = documentService.getDocumentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all documents", description = "Retrieves a list of all documents registered in the system.")
    @ApiResponse(responseCode = "200", description = "Successful retrieval of document list")
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        List<DocumentResponse> responses = documentService.getAllDocuments();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing document", description = "Modifies the title and content of an existing document.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document updated successfully",
            content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data supplied"),
        @ApiResponse(responseCode = "404", description = "Document not found with the given ID")
    })
    public ResponseEntity<DocumentResponse> updateDocument(
            @Parameter(description = "ID of the document to update", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequest request) {
        DocumentResponse response = documentService.updateDocument(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document", description = "Permanently removes a document from the database.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Document deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Document not found with the given ID")
    })
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "ID of the document to delete", example = "1")
            @PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
