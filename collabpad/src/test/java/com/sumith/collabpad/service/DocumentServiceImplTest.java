package com.sumith.collabpad.service;

import com.sumith.collabpad.dto.DocumentRequest;
import com.sumith.collabpad.dto.DocumentResponse;
import com.sumith.collabpad.entity.Document;
import com.sumith.collabpad.exception.DocumentNotFoundException;
import com.sumith.collabpad.repository.DocumentRepository;
import com.sumith.collabpad.service.impl.DocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentServiceImpl(documentRepository);
    }

    @Test
    void createDocument_ShouldReturnResponse() {
        DocumentRequest request = new DocumentRequest("Test Title", "Test Content");
        Document savedDoc = new Document("Test Title", "Test Content");
        savedDoc.setId(1L);

        when(documentRepository.save(any(Document.class))).thenReturn(savedDoc);

        DocumentResponse response = documentService.createDocument(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Test Title", response.title());
        assertEquals("Test Content", response.content());
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void getDocumentById_WhenExists_ShouldReturnResponse() {
        Document doc = new Document("Test Title", "Test Content");
        doc.setId(1L);

        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        DocumentResponse response = documentService.getDocumentById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        verify(documentRepository, times(1)).findById(1L);
    }

    @Test
    void getDocumentById_WhenNotExists_ShouldThrowException() {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DocumentNotFoundException.class, () -> documentService.getDocumentById(1L));
        verify(documentRepository, times(1)).findById(1L);
    }

    @Test
    void getAllDocuments_ShouldReturnList() {
        Document doc = new Document("Test Title", "Test Content");
        doc.setId(1L);

        when(documentRepository.findAll()).thenReturn(List.of(doc));

        List<DocumentResponse> responseList = documentService.getAllDocuments();

        assertNotNull(responseList);
        assertEquals(1, responseList.size());
        assertEquals(1L, responseList.get(0).id());
        verify(documentRepository, times(1)).findAll();
    }
}
