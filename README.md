# CollabPad

**CollabPad** is a modern multi-tenant SaaS knowledge-management platform built with Java, Spring Boot, React, and PostgreSQL. It provides self-serve company onboarding, departmental workspaces, and hierarchical document authoring with Notion-style infinite nesting, protected by strict row-level tenant data isolation and OAuth2 / OpenID Connect authentication.

---

## 🏗️ System Architecture

CollabPad uses a decoupled, cloud-ready architecture where the React SPA authenticates users through Clerk, forwards short-lived JWTs to the Spring Boot Core API, and the backend verifies the tokens directly against Clerk's JWKS endpoint before scoping all queries to the user's tenant in PostgreSQL.

👉 **[View Complete Architecture Specification & Flowcharts →](docs/architecture.md)**

### Architectural Flow:
1. **Client Tier:** React 19 + Vite SPA (deployed on Vercel Edge CDN). The frontend uses an Axios request interceptor (`src/api.js`) that dynamically extracts the active session token from `@clerk/react` and injects `Authorization: Bearer <JWT>` into every API call.
2. **Identity & Access Management:** Clerk manages user credentials, OAuth logins, and session lifecycles.
3. **Application Tier:** Spring Boot 3.4.2 Core API running in a containerized Alpine JRE runtime on Render. Spring Security functions as an **OAuth2 Resource Server**, cryptographically validating token signatures against Clerk's JWKS key discovery endpoint (`${CLERK_ISSUER_URI}/.well-known/jwks.json`).
4. **Data Isolation:** `UserService` resolves the authenticated user's `tenant_id` from the JWT claims. Every subsequent repository query and mutation is strictly filtered by this tenant boundary.
5. **Persistence Tier:** Cloud PostgreSQL on Neon with structured relational indexing and automated tenant foreign-key constraints.

---

## 🚀 Key Engineering Highlights

### 1. Multi-Tenant SaaS Isolation Model
* **Row-Level Tenant Segregation:** Rather than maintaining complex multi-database schemas, CollabPad implements a shared-database, row-level tenant isolation pattern. Core tables (`users`, `workspaces`, `documents`) enforce a non-nullable foreign key referencing `tenants(id)`.
* **Zero Cross-Tenant Leaks:** Security boundaries are verified at the Spring service layer. Before any document or workspace is read, modified, or deleted, the service validates `document.getTenant().getId().equals(tenantId)`, rejecting unauthorized attempts with a `SecurityException`.

### 2. $O(N)$ Hierarchical Document Tree Engine
* **Self-Referential Relational Hierarchy:** Documents use a self-referential `parent_document_id` column, allowing arbitrary, infinite nesting of pages within sub-pages.
* **Single-Pass In-Memory Tree Assembly:** Instead of recursive database queries ($N+1$ problem), the backend retrieves all documents for a workspace in a single indexed query (`findByWorkspaceIdAndTenantId`).
* **HashMap Adjacency Construction:** `DocumentService.getDocumentTree()` builds a lookup `Map<Long, DocumentNodeResponse>` in $O(N)$ time, iterates once to attach children to their respective parent nodes, and routes orphan nodes safely to root.
* **Recursive Positional Sorting:** Sub-trees are recursively sorted by a `positionOrder` float/double coordinate to support drag-and-drop ordering.
* **Cycle Prevention:** Explicit checks prevent cyclical hierarchy corruption (e.g., a document attempting to set itself as its own parent).

### 3. Automated Self-Serve Tenant Onboarding
* When an un-enrolled Clerk user logs in, the backend detects the absence of a database record and triggers the frontend `OnboardingWizard`.
* An atomic `@Transactional` service (`OnboardingService.enrollCompany`) executes:
  1. Creates the `Tenant` record.
  2. Registers the user as `ROLE_COMPANY_ADMIN`.
  3. Automatically provisions default department workspaces (*Engineering*, *Product*, *Marketing*, *HR*, *Legal*).
  4. Seeds initial welcome documentation for each department.

---

## 🧠 Database Schema (ERD)

Data isolation is enforced at the database row level across tenants, workspaces, and nested documents.

👉 **[View Entity-Relationship Diagram (ERD) & Schema Details →](docs/database_schema.md)**

### Core Entities:
* **`Tenant`**: The root organization entity representing a company.
* **`User`**: A team member linked to a tenant, with role enums (`ROLE_COMPANY_ADMIN`, `ROLE_WORKSPACE_ADMIN`, `ROLE_MEMBER`, `ROLE_VIEWER`).
* **`Workspace`**: Departmental space (e.g., "Engineering") owned by a tenant.
* **`Document`**: The core authoring unit. Contains rich metadata (`title`, `content`, `icon`, `cover_image_url`, `position_order`) and parent-child self-references.

---

## 📚 Technical Documentation

Explore deep-dive specifications, architecture flowcharts, and system designs:

| Document | Description |
| :--- | :--- |
| 🏗️ **[System Architecture & Diagrams](docs/architecture.md)** | System topology, Clerk JWT auth flow, onboarding lifecycle, and deep-dive Mermaid diagrams |
| 🗄️ **[Database Schema & ERD](docs/database_schema.md)** | Entity-relationship model, tables, indexing, and row-level tenant isolation |

---

## 🔌 Core API Endpoints

All endpoints require a valid Clerk Bearer JWT in the `Authorization` header.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/onboarding/enroll` | Registers a company, admin user, workspaces, and welcome docs |
| `GET` | `/api/v1/workspaces` | Lists all authorized workspaces for the user's tenant |
| `GET` | `/api/v1/documents?workspaceId={id}` | Flat list of documents in a workspace |
| `GET` | `/api/v1/documents/tree?workspaceId={id}` | Assembled hierarchical tree of nested documents |
| `GET` | `/api/v1/documents/{id}` | Retrieves a single document by ID (tenant-verified) |
| `POST` | `/api/v1/documents` | Creates a root document or nested child document |
| `PUT` | `/api/v1/documents/{id}` | Updates document title, content, icon, cover, or parent |
| `DELETE` | `/api/v1/documents/{id}` | Deletes document (tenant-verified) |

---

## 🛠️ Tech Stack

* **Backend:** Java 17, Spring Boot 3.4.2, Spring Data JPA / Hibernate, Spring Security (OAuth2 Resource Server)
* **Frontend:** React 19, Vite, Axios, Lucide React, Clerk React SDK
* **Database:** Cloud PostgreSQL (Neon Serverless)
* **Authentication:** Clerk (OAuth2 / OpenID Connect with dynamic JWKS validation)
* **DevOps & CI/CD:** GitHub Actions CI, Docker (Multi-stage build), Render (API hosting), Vercel (SPA hosting)

---

## 🏁 Getting Started Locally

### Prerequisites
* [Java 17+](https://adoptium.net/)
* [Node.js 20+](https://nodejs.org/)
* [Docker](https://www.docker.com/) (Optional, for local PostgreSQL)

### 1. Environment Setup
Create a `.env` file in the root directory (based on `.env.example`):
```env
# Database Configuration (Neon or local PostgreSQL)
DB_URL=jdbc:postgresql://localhost:5432/collab_db
DB_USERNAME=postgres
DB_PASSWORD=collabpassword

# Clerk Authentication (From Clerk Dashboard)
CLERK_ISSUER_URI=https://your-instance.clerk.accounts.dev
CLERK_PUBLISHABLE_KEY=pk_test_your_clerk_key
```

### 2. Start PostgreSQL (Local Docker)
```bash
docker compose up -d postgres
```

### 3. Run Backend (Spring Boot)
```bash
cd collabpad
./mvnw spring-boot:run
```
*The Core API will start on port `8081`.*

### 4. Run Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev
```
*Open [http://localhost:5173](http://localhost:5173) in your browser.*
