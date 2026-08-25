# Spring Boot 3.x — Core Architecture & Cheatsheet

A developer guide to Spring Boot 3.x. This document summarizes the architecture, project structure, core annotations, and data flow patterns.

---

## 1. The Core Layered Architecture

Spring Boot follows a strict layered architectural pattern. Each layer has a single responsibility and only communicates with the layer directly below it.

```text
  [ Client (HTTP Request) ]
              │
              ▼
    ┌───────────────────┐
    │  Controller Layer │  <-- Handles routing, JSON deserialization, & HTTP status codes
    └─────────┬─────────┘
              │
              ▼
    ┌───────────────────┐
    │   Service Layer   │  <-- Executes business logic, transactions, and transformations
    └─────────┬─────────┘
              │
              ▼
    ┌───────────────────┐
    │  Repository Layer │  <-- Interacts with DB (SQL queries via JPA / Hibernate)
    └─────────┬─────────┘
              │
              ▼
    ┌───────────────────┐
    │   Database (DB)   │  <-- Persistent storage
    └───────────────────┘
```

---

## 2. Directory Structure Conventions

A standard modular packaging layout under your package `com.sumith.collabpad`:

```text
src/main/java/com/sumith/collabpad/
│
├── config/             # Application configuration beans (Security, WebSockets, Redis)
│   └── WebSocketConfig.java
│
├── controller/         # Exposes HTTP REST endpoints
│   └── DocumentController.java
│
├── service/            # Core business logic implementations & interfaces
│   ├── DocumentService.java
│   └── impl/
│       └── DocumentServiceImpl.java
│
├── repository/         # DB Query interfaces (extends JpaRepository)
│   └── DocumentRepository.java
│
├── entity/             # JPA Database Entities (represents SQL tables)
│   └── Document.java
│
└── CollabPadApplication.java  # Main Application Entrypoint
```

---

## 3. Core Annotations Cheat Sheet

### Application Configuration
*   `@SpringBootApplication`: The starting point of the application. Combines `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`.

### Controller Layer (Web)
*   `@RestController`: Marks a class as a REST endpoint controller (automatically serializes return objects to JSON).
*   `@RequestMapping("/path")`: Base path prefix for all endpoints in the controller.
*   `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`: HTTP method routes mapping paths to methods.
*   `@RequestBody`: Binds the incoming JSON request body to a Java object.
*   `@PathVariable("id")`: Extracts variables from the URL path (e.g., `/api/documents/{id}`).
*   `@RequestParam("key")`: Extracts query parameters (e.g., `/api/documents?status=ACTIVE`).

### Service Layer (Business Logic)
*   `@Service`: Marks a class as containing business logic. Makes it eligible for dependency injection.
*   `@Transactional`: Automatically wraps database actions in a transaction (rolls back on runtime exceptions).

### Repository Layer (Data Access)
*   `@Repository`: Marks a class/interface as a data access mechanism.
*   `@Entity`: Marks a Java class as a JPA database entity.
*   `@Table(name = "table_name")`: Maps the entity to a specific database table.
*   `@Id`: Specifies the primary key of the entity.
*   `@GeneratedValue(strategy = GenerationType.IDENTITY)`: Automatically generates database IDs.
*   `@Column(name = "col_name")`: Maps a field to a specific database column.

### Dependency Injection (DI)
*   `@Autowired`: Injects a bean (dependency) into a class.
    *   *Best Practice:* Use **Constructor Injection** instead of field injection.
    ```java
    // Constructor Injection (Recommended)
    private final DocumentService documentService;
    
    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }
    ```

---

## 4. End-to-End Data Flow Code Example

Below is a complete implementation of a **Retrieve Document** action showing how the layers connect.

### Step 1: Create the Entity (`entity/Document.java`)
Represents the database structure.
```java
package com.sumith.collabpad.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
```

### Step 2: Create the Repository (`repository/DocumentRepository.java`)
Gives you basic CRUD SQL methods out of the box (e.g. `save()`, `findById()`, `delete()`).
```java
package com.sumith.collabpad.repository;

import com.sumith.collabpad.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    // You get findById(), save(), deleteById(), etc. for free!
}
```

### Step 3: Create the Service (`service/DocumentService.java`)
Houses business operations (e.g. validation, calculations).
```java
package com.sumith.collabpad.service;

import com.sumith.collabpad.entity.Document;

public interface DocumentService {
    Document getDocumentById(Long id);
}
```

```java
package com.sumith.collabpad.service.impl;

import com.sumith.collabpad.entity.Document;
import com.sumith.collabpad.repository.DocumentRepository;
import com.sumith.collabpad.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;

    @Autowired
    public DocumentServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));
    }
}
```

### Step 4: Create the Controller (`controller/DocumentController.java`)
Exposes the URL to the web client.
```java
package com.sumith.collabpad.controller;

import com.sumith.collabpad.entity.Document;
import com.sumith.collabpad.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        Document doc = documentService.getDocumentById(id);
        return ResponseEntity.ok(doc);
    }
}
```
