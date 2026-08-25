package com.sumith.collabpad.service.impl;

import com.sumith.collabpad.dto.DocumentRequest;
import com.sumith.collabpad.dto.DocumentResponse;
import com.sumith.collabpad.entity.Document;
import com.sumith.collabpad.exception.DocumentNotFoundException;
import com.sumith.collabpad.repository.DocumentRepository;
import com.sumith.collabpad.service.DocumentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    @Transactional
    public DocumentResponse createDocument(DocumentRequest request) {
        Document document = new Document(request.title(), request.content());
        Document savedDocument = documentRepository.save(document);
        return mapToResponse(savedDocument);
    }

    @Override
    public DocumentResponse getDocumentById(Long id) {
        return documentRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));
    }

    @Override
    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DocumentResponse updateDocument(Long id, DocumentRequest request) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));
        
        document.setTitle(request.title());
        document.setContent(request.content());
        
        Document updatedDocument = documentRepository.save(document);
        return mapToResponse(updatedDocument);
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new DocumentNotFoundException("Document not found with ID: " + id);
        }
        documentRepository.deleteById(id);
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
