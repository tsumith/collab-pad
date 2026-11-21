# CollabPad — System Architecture, Workflows & Diagrams

> **Target Audience:** New engineers, contributors, and technical stakeholders onboarding onto CollabPad.  
> **Repository:** Monorepo containing Spring Boot backend (`collabpad/`) and React Vite frontend (`frontend/`).  
> **Related Document:** [Database Schema & ERD](database_schema.md)  
> **Image Assets:** High-resolution PNGs and vector SVGs available in `docs/images/`.

---

## 1. System Overview & Deployment Topology

CollabPad is a modern multi-tenant SaaS knowledge management platform. It uses a decoupled client-server architecture where the frontend interacts directly with the Spring Boot backend using OAuth2 JWT authentication provided by Clerk.

![System Overview & Deployment Topology](images/01_system_topology.png)

<details>
<summary>▶ View Mermaid Diagram Source Code</summary>

```mermaid
flowchart TD
    subgraph ClientTier["Client Tier (Web Browser)"]
        UI["React 19 + Vite SPA\n(Hosted on Vercel Edge CDN)"]
        ClerkSDK["@clerk/react\n(Client-Side Auth & Session)"]
        AxiosClient["Axios HTTP Client\n(Bearer JWT Interceptor in api.js)"]
        UI --> ClerkSDK
        UI --> AxiosClient
    end

    subgraph AuthTier["Identity & Access Management (Clerk)"]
        ClerkAuth["Clerk Cloud Auth Service"]
        ClerkJWKS["Clerk JWKS Key Set Endpoint\n(/.well-known/jwks.json)"]
        ClerkAuth --- ClerkJWKS
    end

    subgraph BackendTier["Application Tier (Render Container)"]
        DockerContainer["Spring Boot 3.4.2 Core API\n(Docker on Render, Port $PORT)"]
        SecurityFilter["Spring Security Filter Chain\n(OAuth2 Resource Server)"]
        Controllers["REST Controllers\n(/api/v1/onboarding, /workspaces, /documents)"]
        ServiceLayer["Business Services\n(OnboardingService, DocumentService, etc.)"]
        DataLayer["Spring Data JPA / Hibernate"]

        DockerContainer --> SecurityFilter
        SecurityFilter --> Controllers
        Controllers --> ServiceLayer
        ServiceLayer --> DataLayer
    end

    subgraph PersistenceTier["Persistence Tier (Neon Cloud)"]
        NeonDB[("PostgreSQL 16 DB\n(Multi-Tenant Schema + pgvector ready)")]
        DataLayer --> NeonDB
    end

    ClerkSDK <-->|"User Login & Token Refresh"| ClerkAuth
    AxiosClient -->|"HTTPS REST Requests + Authorization: Bearer JWT"| SecurityFilter
    SecurityFilter -->|"Validate JWT Signature against JWKS"| ClerkJWKS
```
</details>

---

## 2. Entity-Relationship Data Model (ERD)

The database enforces a **Row-Level Tenant Isolation** model. All core business data (`workspaces`, `documents`, `users`) contain a foreign key reference to `tenants.id`.

![Entity-Relationship Data Model](images/02_entity_relationship_model.png)

<details>
<summary>▶ View Mermaid Diagram Source Code</summary>

```mermaid
erDiagram
    TENANT ||--o{ USER : "has members"
    TENANT ||--o{ WORKSPACE : "owns departments"
    TENANT ||--o{ DOCUMENT : "isolates data"
    USER ||--o{ WORKSPACE : "owns / administers"
    USER ||--o{ DOCUMENT : "authors"
    WORKSPACE ||--o{ DOCUMENT : "contains"
    DOCUMENT ||--o{ DOCUMENT : "parent of (hierarchical tree)"

    TENANT {
        bigint id PK "Auto-incrementing ID"
        varchar name "Company / Organization name"
        timestamp created_at "Created timestamp"
        timestamp updated_at "Updated timestamp"
    }

    USER {
        bigint id PK "Auto-incrementing ID"
        uuid user_id "Unique external UUID"
        varchar name "Full Name"
        varchar email UK "Unique email address"
        varchar role "ROLE_COMPANY_ADMIN, ROLE_MEMBER, etc."
        bigint tenant_id FK "References tenants.id (NOT NULL)"
        timestamp created_at "Created timestamp"
        timestamp updated_at "Updated timestamp"
    }

    WORKSPACE {
        bigint id PK "Auto-incrementing ID"
        varchar name "Workspace department name (e.g. Engineering)"
        varchar description "Optional workspace description"
        bigint tenant_id FK "References tenants.id (NOT NULL)"
        bigint owner_id FK "References users.id (nullable)"
        timestamp created_at "Created timestamp"
        timestamp updated_at "Updated timestamp"
    }

    DOCUMENT {
        bigint id PK "Auto-incrementing ID"
        uuid document_id "Unique document UUID"
        varchar title "Document title (max 255)"
        text content "Raw document body"
        bigint workspace_id FK "References workspaces.id (NOT NULL)"
        bigint tenant_id FK "References tenants.id (NOT NULL)"
        bigint author_id FK "References users.id (NOT NULL)"
        bigint parent_document_id FK "References documents.id (Self-ref, nullable)"
        varchar icon "Emoji or icon identifier"
        varchar cover_image_url "Header banner URL"
        double_precision position_order "Ordering rank among siblings"
        timestamp created_at "Created timestamp"
        timestamp updated_at "Updated timestamp"
    }
```
</details>

---

## 3. End-to-End Authentication & Authorization Flow

CollabPad uses Clerk for authentication and Spring Security Resource Server for token verification.

![End-to-End Authentication & Authorization Flow](images/03_authentication_flow.png)

<details>
<summary>▶ View Mermaid Diagram Source Code</summary>

```mermaid
sequenceDiagram
    autonumber
    actor User as End User
    participant Browser as React App (Vite)
    participant Clerk as Clerk Identity Provider
    participant Axios as api.js (Axios Interceptor)
    participant SpringSec as Spring Security (Filter Chain)
    participant UserService as UserService.java
    participant Controller as REST Controller

    User->>Browser: Opens CollabPad web app
    Browser->>Clerk: Check authentication state
    alt User is not signed in
        Clerk-->>Browser: Session inactive
        Browser-->>User: Display <SignIn /> widget
        User->>Clerk: Submits credentials / Social OAuth
        Clerk-->>Browser: Session established, returns session token
    end

    Note over Browser,Axios: Request to Backend Core API
    Browser->>Axios: Triggers API call (e.g. GET /api/v1/workspaces)
    Axios->>Clerk: window.Clerk.session.getToken()
    Clerk-->>Axios: Returns signed Clerk JWT
    Axios->>SpringSec: GET /api/v1/workspaces (Authorization: Bearer <JWT>)

    Note over SpringSec,Clerk: JWT Cryptographic Verification
    SpringSec->>Clerk: Fetch public keys from CLERK_ISSUER_URI/.well-known/jwks.json
    SpringSec->>SpringSec: Validate signature, issuer, and expiration
    alt Invalid or Expired Token
        SpringSec-->>Browser: HTTP 401 Unauthorized
    else Valid Token
        SpringSec->>Controller: Route to Controller (@AuthenticationPrincipal Jwt jwt)
        Controller->>UserService: getOrCreateUserFromJwt(jwt)
        UserService->>UserService: Extract email from claims ("email", "preferred_username")
        UserService->>UserService: Query userRepository.findByEmail(email)
        alt User not in database
            UserService-->>Controller: Throws RuntimeException("USER_NOT_ENROLLED")
            Controller-->>Browser: HTTP 500 / 401 (Triggers Onboarding in UI)
        else User found
            UserService-->>Controller: Returns User entity (with Tenant info)
            Controller->>Controller: Execute business operation scoped to tenant.getId()
            Controller-->>Browser: HTTP 200 OK with JSON response
        end
    end
```
</details>

---

## 4. Multi-Tenant Onboarding Flow (First-Time User Experience)

When a brand new user registers with Clerk, they do not yet have a record in the database. The system detects this and triggers the `OnboardingWizard`.

![Multi-Tenant Onboarding Flow](images/04_onboarding_lifecycle.png)

<details>
<summary>▶ View Mermaid Diagram Source Code</summary>

```mermaid
flowchart TD
    Start(["User Logs In via Clerk"]) --> FetchWorkspaces["Sidebar calls GET /api/v1/workspaces"]
    FetchWorkspaces --> CheckEnrolled{"Backend User Check\n(userRepository.findByEmail)"}

    CheckEnrolled -- "User Exists" --> LoadWorkspaces["Load authorized workspaces & document tree"]
    LoadWorkspaces --> ShowDashboard["Display Workspace Dashboard"]

    CheckEnrolled -- "USER_NOT_ENROLLED" --> CatchError["Sidebar catches 401/403/500"]
    CatchError --> TriggerWizard["App.jsx sets needsOnboarding = true"]
    TriggerWizard --> RenderWizard["Render <OnboardingWizard /> modal"]
    RenderWizard --> UserInput["User enters Company Name\n& selects initial departments"]
    UserInput --> SubmitEnroll["POST /api/v1/onboarding/enroll\n{ companyName, selectedDepartments }"]
    
    subgraph BackendEnrollment["OnboardingService.enrollCompany Transaction"]
        CreateTenant["1. Create & Save Tenant Entity"]
        CreateAdmin["2. Create User as ROLE_COMPANY_ADMIN"]
        LoopDepts["3. For each selected department:\n- Create Workspace\n- Create initial 'Welcome' Document"]
        CreateTenant --> CreateAdmin --> LoopDepts
    end

    SubmitEnroll --> BackendEnrollment
    BackendEnrollment --> EnrollSuccess["HTTP 200 OK"]
    EnrollSuccess --> ReloadApp["window.location.reload()"]
    ReloadApp --> Start
```
</details>

---

## 5. Hierarchical Document Tree: In-Memory Recursive Assembly

CollabPad stores hierarchical relationships relationally (`parent_document_id`), but the frontend needs an arbitrarily deep recursive tree node structure. The tree building logic lives in [`DocumentService.getDocumentTree`](file:///c:/sumith/projects/collab-pad/collabpad/src/main/java/com/sumith/collabpad/service/DocumentService.java#L101-L153).

### 5.1 Algorithm Flowchart

![Document Tree Building Algorithm](images/05_document_tree_algorithm.png)

<details>
<summary>▶ View Mermaid Diagram Source Code</summary>

```mermaid
flowchart TD
    Req["GET /api/v1/documents/tree?workspaceId=W"]
    --> QueryDB["Query DB: documentRepository.findByWorkspaceIdAndTenantId(W, Tenant)"]
    --> MapNodes["Transform List<Document> into Map<Long, DocumentNodeResponse>\nInitialize empty children list for each node"]
    --> InitRoots["Initialize empty List<DocumentNodeResponse> roots"]
    --> Iterate{"Iterate through each Document in allDocs"}

    Iterate -- "doc.getParentDocument() == null" --> AddRoot["Add node to roots list"]
    Iterate -- "doc.getParentDocument() != null" --> CheckParent{"Does parent node exist in Map?"}

    CheckParent -- "Yes" --> AppendChild["parentNode.children().add(node)"]
    CheckParent -- "No (Orphan)" --> AddRoot

    AddRoot --> NextDoc{"More docs?"}
    AppendChild --> NextDoc

    NextDoc -- "Yes" --> Iterate
    NextDoc -- "No" --> SortRoots["Sort roots by positionOrder"]
    SortRoots --> RecurseSort["Recursively sort all children by positionOrder"]
    RecurseSort --> ReturnTree["Return List<DocumentNodeResponse> JSON to client"]
```
</details>

### 5.2 Tree Visual Representation

![Workspace Document Hierarchy](images/06_document_tree_structure.png)

<details>
<summary>▶ View Mermaid Diagram Source Code</summary>

```mermaid
graph TD
    classDef root fill:#2563eb,stroke:#1d4ed8,color:#fff;
    classDef subpage fill:#475569,stroke:#334155,color:#fff;
    classDef leaf fill:#0f766e,stroke:#115e59,color:#fff;

    R1["📁 Company Wiki (Root)"]:::root
    R2["📁 Engineering Guidelines (Root)"]:::root

    R1 --> C1["📄 Getting Started (Sub-page)"]:::subpage
    R1 --> C2["📄 Remote Work Policy (Sub-page)"]:::subpage

    R2 --> C3["📐 Architecture Specs (Sub-page)"]:::subpage
    R2 --> C4["🚀 Release Runbook (Sub-page)"]:::subpage
    C3 --> C31["📝 Neon DB Setup (Deep Leaf)"]:::leaf
    C3 --> C32["📝 Clerk Auth Config (Deep Leaf)"]:::leaf
```
</details>

---

## 6. Document CRUD Operations & Tenant Security Isolation

Every single database query or mutation verifies that the resource belongs to the tenant stored in the user's JWT. Cross-tenant access attempts immediately fail.

![Document CRUD Operations & Tenant Security Isolation](images/07_document_crud_security.png)

<details>
<summary>▶ View Mermaid Diagram Source Code</summary>

```mermaid
sequenceDiagram
    autonumber
    actor User as User (Tenant #1)
    participant Controller as DocumentController.java
    participant Service as DocumentService.java
    participant DB as PostgreSQL (Neon)

    Note over User,DB: Scenario 1: Creating a Document
    User->>Controller: POST /api/v1/documents (DocumentRequest with title, content, workspaceId, parentId)
    Controller->>Service: createDocument(tenantId=1, workspaceId, authorId, request)
    Service->>DB: Verify workspace belongs to Tenant #1
    alt Workspace belongs to Tenant #2
        Service-->>Controller: Throws SecurityException("Workspace does not belong to this tenant!")
        Controller-->>User: HTTP 403 Forbidden
    end
    alt ParentId provided
        Service->>DB: Verify parent document belongs to same workspace & Tenant #1
        alt Cross-workspace parent attempted
            Service-->>Controller: Throws SecurityException("Parent document must be in the same workspace")
            Controller-->>User: HTTP 403 Forbidden
        end
    end
    Service->>DB: Save new Document
    DB-->>Service: Document saved with generated ID & UUID
    Service-->>Controller: DocumentResponse DTO
    Controller-->>User: HTTP 201 Created

    Note over User,DB: Scenario 2: Fetching / Updating / Deleting a Document
    User->>Controller: PUT /api/v1/documents/{id}
    Controller->>Service: updateDocument(tenantId=1, id, request)
    Service->>DB: SELECT * FROM documents WHERE id = {id}
    DB-->>Service: Found Document
    alt document.getTenant().getId() != tenantId
        Service-->>Controller: Throws SecurityException("Unauthorized access to document!")
        Controller-->>User: HTTP 403 Forbidden
    else document.getId() == request.parentId()
        Service-->>Controller: Throws IllegalArgumentException("Document cannot be its own parent")
        Controller-->>User: HTTP 400 Bad Request
    else Valid Tenant & Valid Hierarchy
        Service->>DB: Update document fields & save
        DB-->>Service: Saved
        Service-->>Controller: DocumentResponse DTO
        Controller-->>User: HTTP 200 OK
    end
```
</details>

---

## 7. Frontend State Machine & UI Navigation

The React client state is managed primarily at the top level in [`App.jsx`](file:///c:/sumith/projects/collab-pad/frontend/src/App.jsx).

![Frontend State Machine & UI Navigation](images/08_frontend_state_machine.png)

<details>
<summary>▶ View Mermaid Diagram Source Code</summary>

```mermaid
stateDiagram-v2
    [*] --> IdentityLoading: App Mounts

    state IdentityLoading {
        [*] --> CheckClerk: isLoaded?
    }

    IdentityLoading --> SignedOut: isSignedIn === false
    IdentityLoading --> SignedIn: isSignedIn === true

    state SignedOut {
        [*] --> RenderClerkSignIn: Render <SignIn />
    }

    state SignedIn {
        [*] --> CheckEnrollment

        state CheckEnrollment {
            [*] --> WorkspaceCheck: Fetch /api/v1/workspaces
            WorkspaceCheck --> Enrolled: 200 OK
            WorkspaceCheck --> NotEnrolled: Error / Needs Onboarding
        }

        state OnboardingWizardView {
            RenderWizardModal: Render <OnboardingWizard />
            SubmitCompany: Submit company & departments
        }

        NotEnrolled --> OnboardingWizardView: needsOnboarding = true
        OnboardingWizardView --> Enrolled: Reload on completion

        state MainWorkspaceView {
            [*] --> DashboardView: currentView === 'dashboard'
            DashboardView --> EditorView: Click document card / tree node
            EditorView --> DashboardView: Click 'Back' button
            DashboardView --> TeamView: Click 'Team Directory' nav
            TeamView --> DashboardView: Click 'Dashboard' nav
            EditorView --> TeamView: Click 'Team Directory' nav
        }

        Enrolled --> MainWorkspaceView
    }
```
</details>

---

## 8. Backend Software Architecture (Layers & Responsibilities)

The Spring Boot backend strictly follows clean layered architecture principles:

![Backend Software Architecture](images/09_backend_class_architecture.png)

<details>
<summary>▶ View Mermaid Diagram Source Code</summary>

```mermaid
classDiagram
    class SecurityConfig {
        +filterChain(HttpSecurity) SecurityFilterChain
        +corsConfigurationSource() CorsConfigurationSource
    }

    class DocumentController {
        -DocumentService documentService
        -UserService userService
        +createDocument(Jwt, DocumentRequest) ResponseEntity
        +getDocument(Jwt, Long) ResponseEntity
        +getAllDocuments(Jwt, Long) ResponseEntity
        +getDocumentTree(Jwt, Long) ResponseEntity
        +updateDocument(Jwt, Long, DocumentRequest) ResponseEntity
        +deleteDocument(Jwt, Long) ResponseEntity
    }

    class WorkspaceController {
        -WorkspaceService workspaceService
        -UserService userService
        +getAllWorkspaces(Jwt) ResponseEntity
    }

    class OnboardingController {
        -OnboardingService onboardingService
        +enrollCompany(Jwt, EnrollmentRequest) ResponseEntity
    }

    class DocumentService {
        -DocumentRepository documentRepository
        -TenantRepository tenantRepository
        -WorkspaceRepository workspaceRepository
        -UserRepository userRepository
        +createDocument(...) DocumentResponse
        +getDocumentById(...) DocumentResponse
        +getAllDocuments(...) List~DocumentResponse~
        +getDocumentTree(...) List~DocumentNodeResponse~
        +updateDocument(...) DocumentResponse
        +deleteDocument(...) void
    }

    class UserService {
        -UserRepository userRepository
        -TenantRepository tenantRepository
        -WorkspaceRepository workspaceRepository
        +getOrCreateUserFromJwt(Jwt) User
    }

    class OnboardingService {
        -UserRepository userRepository
        -TenantRepository tenantRepository
        -WorkspaceRepository workspaceRepository
        -DocumentRepository documentRepository
        +enrollCompany(Jwt, EnrollmentRequest) User
    }

    class Document {
        -Long id
        -UUID documentId
        -String title
        -String content
        -Workspace workspace
        -Tenant tenant
        -User author
        -Document parentDocument
        -String icon
        -String coverImageUrl
        -Double positionOrder
        -OffsetDateTime createdAt
        -OffsetDateTime updatedAt
    }

    class Tenant {
        -Long id
        -String name
        -OffsetDateTime createdAt
        -OffsetDateTime updatedAt
    }

    class User {
        -Long id
        -UUID userId
        -String name
        -String email
        -Role role
        -Tenant tenant
        -OffsetDateTime createdAt
        -OffsetDateTime updatedAt
    }

    class Workspace {
        -Long id
        -String name
        -String description
        -Tenant tenant
        -User owner
        -OffsetDateTime createdAt
        -OffsetDateTime updatedAt
    }

    DocumentController ..> DocumentService
    WorkspaceController ..> UserService
    OnboardingController ..> OnboardingService
    DocumentService ..> Document
    DocumentService ..> Tenant
    DocumentService ..> Workspace
    DocumentService ..> User
    UserService ..> User
```
</details>

---

## 9. Summary Cheat Sheet for New Developers

| Question / Action | Location in Codebase |
| :--- | :--- |
| **How does authentication work?** | Frontend: `src/api.js` attaches Clerk JWT token. Backend: [`SecurityConfig.java`](file:///c:/sumith/projects/collab-pad/collabpad/src/main/java/com/sumith/collabpad/config/SecurityConfig.java) verifies with Clerk JWKS. |
| **Where is the user's company identified?** | [`UserService.java`](file:///c:/sumith/projects/collab-pad/collabpad/src/main/java/com/sumith/collabpad/service/UserService.java) resolves the `User` entity by email claim, granting access to `user.getTenant()`. |
| **How is document nesting represented?** | In DB: `documents.parent_document_id`. In Backend: [`DocumentService.java`](file:///c:/sumith/projects/collab-pad/collabpad/src/main/java/com/sumith/collabpad/service/DocumentService.java) transforms this to `DocumentNodeResponse` trees. In UI: [`Sidebar.jsx`](file:///c:/sumith/projects/collab-pad/frontend/src/components/Sidebar.jsx) renders recursive `TreeItem`s. |
| **How to run the system locally?** | Run PostgreSQL (or Docker `docker-compose up -d postgres`), then `./mvnw spring-boot:run` in `collabpad/`, and `npm run dev` in `frontend/`. |
