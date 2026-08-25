package com.sumith.collabpad.service;

import com.sumith.collabpad.dto.DocumentRequest;
import com.sumith.collabpad.dto.DocumentResponse;
import java.util.List;

public interface DocumentService {
    DocumentResponse createDocument(DocumentRequest request);
    DocumentResponse getDocumentById(Long id);
    List<DocumentResponse> getAllDocuments();
    DocumentResponse updateDocument(Long id, DocumentRequest request);
    void deleteDocument(Long id);
}
