# CollabPad — System Architecture Specification

> **Module:** System Architecture & Topology  
> **Last Verified:** Current `main` branch  
> **Related Documents:** [Database Schema](database_schema.md) | [Comprehensive Architecture & Workflows](architecture_and_diagrams.md)

---

## 1. System Topology Overview

CollabPad is a modern multi-tenant SaaS knowledge management platform built on a decoupled, cloud-native architecture. The client SPA authenticates users through Clerk, forwards short-lived JWTs to the Spring Boot Core API, and the backend verifies the tokens directly against Clerk's JWKS endpoint before scoping all queries to the user's tenant in PostgreSQL.

![System Overview & Deployment Topology](images/01_system_topology.png)

<details>
<summary>▶ View Mermaid Diagram Source</summary>

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

## 2. Tier Breakdown

### 2.1 Client Tier (React 19 + Vite)
* **Hosting:** Vercel Edge CDN with global edge caching and automatic SSL.
* **Authentication Interceptor:** An Axios interceptor in `src/api.js` intercepts every outgoing HTTP request, dynamically fetches an active session JWT from `@clerk/react`, and injects `Authorization: Bearer <JWT>` into headers.
* **UI Components:**
  * `Sidebar.jsx`: Dynamic tree hierarchy navigation, workspace switcher, and mobile drawer.
  * `Dashboard.jsx`: Workspace overview and flat/recent document cards.
  * `DocumentEditor.jsx`: Document reading and editing interface.
  * `OnboardingWizard.jsx`: Automated company registration modal for un-enrolled users.

### 2.2 Identity & Access Management (Clerk)
* Handles user authentication, social logins, passwordless magic links, and session lifecycles.
* Exposes a public JSON Web Key Set (JWKS) endpoint at `${CLERK_ISSUER_URI}/.well-known/jwks.json`.
* Eliminates the need to store passwords or implement bespoke token rotation in the backend.

### 2.3 Application Tier (Spring Boot 3.4.2)
* **Runtime:** Containerized Eclipse Temurin Alpine JRE 17 running on Render.
* **Security Layer:** Spring Security operates as an **OAuth2 Resource Server**, cryptographically validating JWT signatures against Clerk's JWKS key discovery endpoint.
* **Tenant Scoping:** `UserService.getOrCreateUserFromJwt(jwt)` maps claims to the tenant boundary. Every subsequent repository query and mutation is strictly filtered by `tenant_id`.

### 2.4 Persistence Tier (PostgreSQL on Neon)
* Serverless PostgreSQL 16 hosted on Neon with connection pooling.
* Enforces row-level isolation via non-nullable `tenant_id` foreign keys on core tables.
* Structured relational indexing on `(workspace_id, tenant_id)` and self-referential `parent_document_id`.

---

## 3. End-to-End Authentication & Authorization Lifecycle

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

---

## 4. First-Time User Onboarding Lifecycle

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

---

## 5. CI/CD & Deployment Architecture

```mermaid
flowchart LR
    Dev["Developer push to main"] --> GHA["GitHub Actions CI"]
    
    subgraph CI["Automated CI Verification"]
        BEBuild["Backend: Maven test-compile & package"]
        FEBuild["Frontend: Vite build & lint check"]
    end
    
    GHA --> BEBuild
    GHA --> FEBuild
    
    BEBuild --> RenderHook["Deploy Trigger: Render Webhook"]
    FEBuild --> VercelHook["Deploy Trigger: Vercel Git Hook"]
    
    RenderHook --> RenderSvc["Render Core API (Containerized JRE)"]
    VercelHook --> VercelSvc["Vercel Edge Network (SPA)"]
```
