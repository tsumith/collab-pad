package com.sumith.collabpad.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sumith.collabpad.dto.DocumentRequest;
import com.sumith.collabpad.dto.DocumentResponse;
import com.sumith.collabpad.exception.DocumentNotFoundException;
import com.sumith.collabpad.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.BeforeEach;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DocumentService documentService() {
            return mock(DocumentService.class);
        }
    }

    @BeforeEach
    void setUp() {
        reset(documentService);
    }

    @Test
    void createDocument_WithValidInput_ShouldReturnCreated() throws Exception {
        DocumentRequest request = new DocumentRequest("Title", "Content");
        DocumentResponse response = new DocumentResponse(1L, "Title", "Content", OffsetDateTime.now(), OffsetDateTime.now());

        when(documentService.createDocument(any(DocumentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.content").value("Content"));
    }

    @Test
    void createDocument_WithBlankTitle_ShouldReturnBadRequest() throws Exception {
        DocumentRequest request = new DocumentRequest("", "Content");

        mockMvc.perform(post("/api/v1/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").value("Title is required"));
    }

    @Test
    void getDocumentById_WhenExists_ShouldReturnOk() throws Exception {
        DocumentResponse response = new DocumentResponse(1L, "Title", "Content", OffsetDateTime.now(), OffsetDateTime.now());

        when(documentService.getDocumentById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    void getDocumentById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(documentService.getDocumentById(1L)).thenThrow(new DocumentNotFoundException("Document not found with ID: 1"));

        mockMvc.perform(get("/api/v1/documents/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Document not found with ID: 1"));
    }

    @Test
    void updateDocument_WithValidInput_ShouldReturnUpdated() throws Exception {
        DocumentRequest request = new DocumentRequest("Updated Title", "Updated Content");
        DocumentResponse response = new DocumentResponse(1L, "Updated Title", "Updated Content", OffsetDateTime.now(), OffsetDateTime.now());

        when(documentService.updateDocument(eq(1L), any(DocumentRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/documents/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"));
    }

    @Test
    void deleteDocument_WhenExists_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/documents/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteDocument_WhenNotExists_ShouldReturnNotFound() throws Exception {
        doThrow(new DocumentNotFoundException("Document not found with ID: 1")).when(documentService).deleteDocument(1L);

        mockMvc.perform(delete("/api/v1/documents/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Document not found with ID: 1"));
    }
}
