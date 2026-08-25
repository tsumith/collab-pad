# Product Requirements Document

# KnowledgeHub — Local-First Enterprise AI Knowledge Management Platform

**Document Version:** 1.0
**Status:** Product & Engineering Specification
**Primary Language:** Java 21+
**Primary Framework:** Spring Boot 3.4+
**Architecture:** Distributed, event-driven, multi-tenant, local-first enterprise platform
**Primary Deployment Target:** Docker Compose local enterprise topology
**Optional Deployment Target:** Free/low-cost cloud-compatible subset
**Repository:** Single GitHub monorepo

---

# 1. Executive Summary

KnowledgeHub is an enterprise-style, multi-tenant knowledge-management platform inspired by products such as Notion.

The platform allows organizations to:

* create workspaces
* create and manage documents
* upload documents
* process PDFs and other supported files
* search knowledge semantically
* ask AI questions about organizational knowledge
* collaborate on documents in real time
* manage users and tenant-level roles
* securely isolate tenant data
* cache frequently accessed content
* process heavy workloads asynchronously
* observe distributed requests
* recover from infrastructure failures
* test system behavior under high load
* automate infrastructure and deployments

The project is intentionally designed as a **local-first enterprise system**.

The complete architecture must be executable locally through Docker Compose, even when a particular component is unsuitable for free cloud hosting.

The project therefore does not compromise architectural depth merely to fit a free cloud provider.

The local environment is the authoritative environment for demonstrating:

* Kafka clusters
* Redis topology
* PostgreSQL sharding
* ShardingSphere
* Qdrant
* Ollama
* Keycloak
* Temporal/Saga
* OpenTelemetry
* Prometheus
* Jaeger
* Grafana
* WebSockets
* chaos/failover testing
* k6 load testing
* infrastructure automation

A smaller cloud deployment may use a reduced topology, but it must not replace or remove the full local implementation.

---

# 2. Product Vision

The goal is to create a portfolio project that demonstrates not merely Java syntax or CRUD development, but the engineering principles used to construct distributed backend systems.

The system should demonstrate:

```text
Java
Spring Boot
Spring Security
REST APIs
Virtual Threads
Kafka
Redis
PostgreSQL
Database Sharding
Qdrant
Vector Embeddings
Local LLM/Embedding Infrastructure
Keycloak
Background Processing
Distributed Transactions
Saga Pattern
WebSockets
OpenTelemetry
Prometheus
Grafana
Jaeger
Docker
Terraform
GitHub Actions
Testcontainers
k6
Chaos Testing
Disaster Recovery
```

The project must emphasize understanding, measurable behavior, and architectural reasoning rather than simply adding technologies for appearance.

---

# 3. Primary Product Users

## 3.1 Tenant Organization

A tenant represents an independent organization using KnowledgeHub.

Example:

```text
Tenant A
└── Acme Corporation

Tenant B
└── Example Corporation
```

Tenant data must remain isolated.

---

## 3.2 Tenant Administrator

The tenant administrator can:

* manage workspace membership
* assign roles
* create/delete workspaces
* manage tenant documents
* view tenant-level usage information
* inspect tenant-related system metrics where permitted

Role:

```text
ROLE_ADMIN
```

---

## 3.3 Tenant User

A normal tenant user can:

* access authorized workspaces
* create documents
* edit documents
* upload files
* search knowledge
* ask AI questions
* collaborate through WebSockets

Role:

```text
ROLE_TENANT_USER
```

---

# 4. Core Product Concept

The fundamental hierarchy is:

```text
Tenant
  │
  ├── Users
  │
  └── Workspaces
        │
        ├── Documents
        │
        ├── Uploaded Files
        │
        └── Knowledge Chunks
                │
                └── Vector Embeddings
```

Example:

```text
Acme Corporation
│
├── Alice
├── Bob
│
├── Engineering Workspace
│   ├── API Documentation
│   ├── Database Architecture
│   └── Deployment Guide
│
└── HR Workspace
    ├── Leave Policy
    └── Employee Handbook
```

---

# 5. Product Goals

The system shall:

1. Provide secure multi-tenant document management.
2. Provide enterprise-grade authentication and authorization.
3. Support high-concurrency Java APIs.
4. Process heavy document operations asynchronously.
5. Use Kafka for real-time/event-driven data pipelines.
6. Provide semantic/vector search.
7. Provide AI-powered knowledge retrieval.
8. Provide distributed caching.
9. Support horizontal application scaling.
10. Support PostgreSQL database sharding.
11. Support real-time document collaboration.
12. Provide distributed tracing and metrics.
13. Support distributed workflows and Saga-style compensation.
14. Automate infrastructure creation.
15. Automate testing and CI/CD.
16. Provide load-testing evidence.
17. Provide disaster recovery and chaos testing.
18. Provide measurable system performance information.
19. Remain fully executable locally without paid cloud infrastructure.

---

# 6. Non-Goals

The initial product will not attempt to reproduce every feature of Notion.

The project does not initially require:

* full Notion UI parity
* advanced visual page builders
* enterprise billing
* payment processing
* commercial LLM APIs
* paid cloud infrastructure
* production-scale globally distributed deployment

The backend architecture is the primary engineering focus.

---

# 7. High-Level Architecture

The complete local topology shall follow this conceptual architecture:

```text
                         ┌─────────────────────┐
                         │ Frontend / API Client│
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │        k6           │
                         │   Load Testing      │
                         └──────────┬──────────┘
                                    │
                                    ▼
                  ┌─────────────────────────────────┐
                  │       Spring Cloud Gateway      │
                  │                                 │
                  │ Rate Limiting                   │
                  │ Payload Validation              │
                  │ Circuit Breaking                 │
                  │ Security Middleware              │
                  └────────────────┬────────────────┘
                                   │
                                   ▼
                    ┌────────────────────────────┐
                    │      Core API Service      │
                    │                            │
                    │ Spring Boot 3.4+           │
                    │ Java 21+                    │
                    │ Virtual Threads             │
                    │ Spring Security              │
                    └──────────────┬─────────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              │                    │                    │
              ▼                    ▼                    ▼
           Redis              Kafka Cluster       ShardingSphere
        Cache Layer          3 Brokers              Proxy
              │                    │                    │
              │                    ▼              ┌─────┴─────┐
              │              Worker Service       │           │
              │                    │             DB1         DB2
              │                    │          PostgreSQL  PostgreSQL
              │                    │
              │                    ▼
              │                Ollama
              │                    │
              │                    ▼
              │              Qdrant Vector DB
              │
              ▼
       Cached Documents


     Observability:
     
     API ─────┐
     Gateway ─┼──> OpenTelemetry Collector
     Worker ──┘             │
                    ┌───────┼────────┐
                    ▼       ▼        ▼
                  Jaeger Prometheus Grafana
```

---

# 8. Service Architecture

The repository shall contain the following major services:

```text
frontend
api-gateway
core-api
worker-service
```

Supporting infrastructure shall include:

```text
Kafka
Redis
PostgreSQL Node 1
PostgreSQL Node 2
ShardingSphere Proxy
Qdrant
Ollama
Keycloak
OpenTelemetry Collector
Jaeger
Prometheus
Grafana
Temporal (or equivalent Saga infrastructure)
```

---

# 9. Requirement 1 — High-Throughput API Service

## 9.1 Technology

The core API shall use:

* Java 21+
* Spring Boot 3.4+
* Embedded Tomcat
* Java Virtual Threads

Virtual threads shall be enabled where appropriate for request handling and I/O-heavy workloads.

---

## 9.2 Purpose

The service must efficiently handle large numbers of concurrent requests without requiring an equivalent number of heavyweight platform threads.

---

## 9.3 API Characteristics

The API must support:

* REST
* JSON
* validation
* structured errors
* authentication
* authorization
* pagination
* filtering
* database transactions
* caching
* asynchronous processing

---

## 9.4 Performance Testing

k6 shall be used to generate high-concurrency traffic.

A local load test shall attempt workloads including:

```text
10,000+ concurrent requests
```

and, where hardware permits, a target workload of:

```text
10,000+ requests/second
```

The result must not be represented as a universal production capacity claim.

The README shall document:

* hardware
* Docker configuration
* test duration
* number of virtual users
* request rate
* average latency
* p50
* p95
* p99
* error rate
* CPU utilization
* memory utilization

---

# 10. Requirement 2 — Real-Time Data Pipeline

## 10.1 Kafka

Apache Kafka shall be used as the primary event-streaming platform.

The local environment shall contain:

```text
Kafka Broker 1
Kafka Broker 2
Kafka Broker 3
```

The original architecture specifies:

```text
3 Kafka brokers + ZooKeeper
```

The implementation shall preserve the required three-broker Kafka topology. Where the selected Kafka version uses KRaft instead of ZooKeeper, the repository documentation shall explicitly explain the compatibility difference rather than silently removing the intended clustered Kafka architecture.

---

## 10.2 Events

Examples:

```text
DocumentCreated
DocumentUpdated
DocumentDeleted
DocumentUploaded
DocumentProcessingRequested
DocumentProcessingCompleted
DocumentProcessingFailed
WorkspaceDeleted
VectorIndexRequested
VectorIndexCompleted
```

---

## 10.3 Exactly-Once Processing

Kafka consumers shall use transactional processing where appropriate.

The intended Spring Kafka configuration shall use:

```text
transactional management
exactly_once_v2
```

The system shall prevent duplicate document indexing under normal retry/reprocessing scenarios.

The implementation must also acknowledge the distinction between Kafka's processing guarantees and true end-to-end exactly-once effects across external systems such as Qdrant.

---

# 11. Requirement 3 — Vector Search Engine

## 11.1 Vector Database

The local system shall run:

```text
Qdrant
```

or, if explicitly selected during implementation:

```text
Milvus
```

The architecture shall use a dedicated vector database rather than PostgreSQL full-text search for semantic retrieval.

---

## 11.2 AI Integration

Java shall use:

```text
LangChain4j
```

for AI/embedding integration where appropriate.

---

## 11.3 Local Embedding Infrastructure

The system shall run:

```text
Ollama
```

locally.

The intended embedding model is:

```text
nomic-embed-text
```

The exact model can be configurable.

---

## 11.4 Processing Pipeline

```text
Document
   ↓
Text Extraction
   ↓
Text Cleaning
   ↓
Chunking
   ↓
Ollama Embedding
   ↓
Vector
   ↓
Qdrant
```

---

## 11.5 Search

A semantic search request shall:

```text
User Query
   ↓
Embedding
   ↓
Qdrant similarity search
   ↓
Metadata filtering
   ↓
Relevant chunks
```

Metadata filters must include tenant/workspace security boundaries.

A user must never retrieve vectors belonging to another tenant.

---

# 12. Requirement 4 — Distributed Cache Layer

## 12.1 Redis

Redis shall provide distributed caching.

The local topology shall target:

```text
3 Redis Masters
3 Redis Replicas
```

with Sentinel-based management where applicable.

---

## 12.2 Cache Responsibilities

Redis shall be used for:

* frequently accessed documents
* hot workspace information
* rate limiting
* temporary processing state
* distributed coordination where appropriate
* WebSocket Pub/Sub

---

## 12.3 Java Integration

The Java application shall use:

```text
Lettuce
```

for Redis connectivity.

---

## 12.4 Atomic Rate Limiting

Custom Lua scripts shall be implemented for atomic token-bucket or equivalent rate-limiting validation.

The algorithm must avoid race conditions such as:

```text
Request A reads token count
Request B reads token count
Request A decrements
Request B decrements
```

Instead, Redis-side Lua execution shall make the operation atomic.

---

# 13. Requirement 5 — Multi-Tenant Authentication System

## 13.1 Identity Provider

Keycloak shall run as an independent Docker container.

---

## 13.2 Java Security

Spring Security shall operate as an OAuth2 Resource Server.

The backend shall validate incoming JWTs.

---

## 13.3 JWT Information

The system shall extract information including:

```text
user identity
tenant/organization identity
roles
```

---

## 13.4 Roles

At minimum:

```text
ROLE_ADMIN
ROLE_TENANT_USER
```

---

## 13.5 Method Security

Spring method-level security shall be used.

Example conceptual policy:

```text
@PreAuthorize("hasRole('ADMIN')")
```

Access policies shall be applied at both:

* endpoint/security layer
* service/business authorization layer

where appropriate.

---

# 14. Multi-Tenant Data Isolation

Every tenant-owned entity shall have an identifiable tenant boundary.

Conceptually:

```text
documents
-------------------------
id
tenant_id
workspace_id
title
content
created_at
updated_at
```

Queries must never accidentally omit tenant isolation.

The implementation should investigate Hibernate tenant resolution mechanisms where appropriate, including a custom tenant identifier resolver if the final persistence architecture supports it.

---

# 15. Requirement 6 — Background Job Queue

## 15.1 Worker Service

Heavy processing shall not occur directly inside normal API request handling.

A dedicated:

```text
worker-service
```

shall be packaged as its own Java application/JAR and run in its own Docker container.

---

## 15.2 Worker Responsibilities

The worker shall handle:

* PDF parsing
* text extraction
* document splitting
* embedding generation
* vector indexing
* heavy document processing
* cleanup jobs
* retryable asynchronous operations

---

## 15.3 JobRunr / Kafka

The architecture allows:

```text
JobRunr Enterprise
```

or the preferred open architecture:

```text
Kafka + dedicated Spring Boot Worker
```

The project shall prioritize the Kafka-based worker architecture to avoid requiring a commercial component.

---

## 15.4 Dead-Letter Queue

Failed messages shall eventually enter:

```text
DLQ
```

after configurable retries.

Example:

```text
Kafka Topic
    ↓
Worker
    ↓
Failure
    ↓
Retry
    ↓
Retry
    ↓
Retry exhausted
    ↓
Dead Letter Queue
```

DLQ messages shall contain enough metadata to diagnose the failure.

---

# 16. Requirement 7 — Observability Platform

The complete local observability stack shall include:

```text
OpenTelemetry Collector
Prometheus
Jaeger
Grafana
```

---

## 16.1 Tracing

The system shall instrument:

```text
Gateway
Core API
Worker
```

and export traces through OpenTelemetry.

---

## 16.2 Micrometer

Java shall use:

```text
micrometer-tracing-bridge-otel
```

where compatible with the selected Spring Boot version.

---

## 16.3 Trace Correlation

Logs shall include:

```text
trace_id
span_id
```

where supported by the logging configuration.

---

## 16.4 Distributed Trace

A request should be visually traceable:

```text
Client
 ↓
Gateway
 ↓
Core API
 ↓
Kafka
 ↓
Worker
 ↓
Qdrant
```

---

## 16.5 Metrics

Metrics shall include, where applicable:

* request count
* request latency
* error rate
* JVM memory
* JVM CPU
* thread information
* virtual thread information where available
* Kafka consumer lag
* Redis operations
* database pool utilization
* document processing duration
* vector indexing duration

---

# 17. Requirement 8 — Event-Driven Microservices / Saga

## 17.1 Distributed Workflow

The system shall support long-running or multi-step workflows.

Temporal.io Java SDK may be used.

Alternatively, a Spring-based implementation may use:

```text
Spring Cloud Stream
+
Saga Pattern
```

---

## 17.2 Workspace Deletion Example

When a workspace is deleted:

```text
Delete Workspace
       ↓
Delete / invalidate vectors
       ↓
Delete PostgreSQL records
       ↓
Invalidate Redis cache
```

---

## 17.3 Failure Handling

If vector deletion fails after database state changes, the system shall execute an appropriate compensating action.

Conceptually:

```text
Workspace Delete
       ↓
DB operation
       ↓
Vector deletion
       ↓
FAILURE
       ↓
Compensating Action
       ↓
Restore / reconcile DB state
```

The final implementation must document whether it uses:

* Temporal workflows
* orchestration-based Saga
* choreography-based Saga

and why.

---

# 18. Requirement 9 — Infrastructure as Code

Terraform shall be maintained under:

```text
/infra
```

---

## 18.1 Infrastructure

Terraform/local automation shall provision or configure as much of the local topology as practical, including:

```text
Kafka
Redis
PostgreSQL
Keycloak
Qdrant
Observability infrastructure
```

The prompt specifies the Docker/LocalStack provider approach.

Where LocalStack is not technically appropriate for a non-AWS component, the repository must use the appropriate Terraform provider or Docker provider rather than falsely claiming LocalStack provisions it.

---

## 18.2 Expected Workflow

Target workflow:

```bash
terraform init
terraform plan
terraform apply
```

The result should produce the required local infrastructure with minimal manual clicking.

---

# 19. Requirement 10 — Database Sharding Strategy

## 19.1 Physical Database Nodes

At least two PostgreSQL containers shall exist:

```text
PostgreSQL Node 1
PostgreSQL Node 2
```

---

## 19.2 ShardingSphere

Apache ShardingSphere Proxy shall sit between the application and physical PostgreSQL databases.

Architecture:

```text
Core API
   ↓
ShardingSphere Proxy
   ├── PostgreSQL Node 1
   └── PostgreSQL Node 2
```

The application should see the proxy as its database endpoint.

---

## 19.3 Tenant-Based Sharding

Tenant identity shall be used as the sharding key.

Conceptually:

```text
tenant_id
    ↓
hash
    ↓
sharding algorithm
    ↓
physical database
```

Example:

```text
Tenant A → DB1
Tenant B → DB2
Tenant C → DB1
Tenant D → DB2
```

The exact distribution algorithm shall be documented.

---

## 19.4 Important Constraint

The project shall not claim that sharding automatically makes the system scalable.

Documentation must explain:

* why sharding was introduced
* what bottleneck it addresses
* what additional complexity it introduces
* how cross-shard queries behave
* how migrations are handled
* how tenant movement between shards would work

---

# 20. Requirement 11 — Security Middleware

## 20.1 Gateway

Spring Cloud Gateway shall operate as the reverse proxy/API gateway.

Responsibilities:

* routing
* authentication-related enforcement
* rate limiting
* payload validation
* circuit breaking
* request filtering

---

## 20.2 Resilience4j

Resilience4j shall be used for resilience functionality such as:

```text
Circuit Breaker
Retry
Timeout
Bulkhead
```

where appropriate.

---

## 20.3 Payload Protection

The gateway shall enforce configurable:

* maximum request size
* maximum upload size
* allowed content types
* header validation
* malformed request rejection

---

## 20.4 Injection Protection

The system shall protect against SQL injection through:

* parameterized queries
* JPA/Hibernate query mechanisms
* strict input validation
* avoidance of unsafe string-built SQL

The gateway may detect obvious malicious patterns as an additional defensive layer, but SQL-injection security must not rely on regex-based blocking alone.

---

## 20.5 Rate Limiting

Rate limits shall be enforced through Redis-backed atomic token-bucket logic.

Limits should be configurable per:

```text
tenant
user
IP
endpoint
```

where appropriate.

---

# 21. Requirement 12 — CI/CD Pipeline

GitHub Actions shall provide the main CI/CD pipeline.

---

## 21.1 Pipeline Stages

A typical pipeline shall contain:

```text
Checkout
 ↓
Set up Java
 ↓
Compile
 ↓
Unit Tests
 ↓
Static Analysis
 ↓
Integration Tests
 ↓
Testcontainers
 ↓
Docker Build
 ↓
Container Validation
 ↓
Deployment / Release
```

---

## 21.2 Testcontainers

Integration tests shall use Testcontainers where practical.

Tests may launch temporary:

```text
PostgreSQL
Kafka
Redis
Qdrant
```

containers.

The GitHub workflow shall therefore validate behavior against realistic infrastructure rather than only mocks.

---

## 21.3 Local Runner Simulation

The repository shall support reproducing the major CI workflow locally where practical.

The phrase "local runner simulations" means developers should be able to execute equivalent validation locally, even if GitHub Actions remains the authoritative CI system.

---

# 22. Requirement 13 — Real-Time WebSocket Server

## 22.1 Technology

The real-time server shall use:

```text
Spring WebFlux
Project Reactor
WebSockets
```

---

## 22.2 Use Case

When two users are viewing the same document:

```text
User A
   │
   │ edit
   ▼
Server
   │
   ▼
Redis Pub/Sub
   │
   ├──────────► Server Instance 1
   │
   └──────────► Server Instance 2
                       │
                       ▼
                    User B
```

---

## 22.3 Real-Time Events

Examples:

```text
document.user_joined
document.user_left
document.content_changed
document.cursor_changed
document.editing_started
document.editing_stopped
```

---

## 22.4 Horizontal Scaling

Redis Pub/Sub shall allow multiple API/WebSocket server instances to synchronize events.

The system must not assume that all connected users are attached to one Java process.

---

# 23. Requirement 14 — Cost Optimization / FinOps Dashboard

Even though the system is primarily local, resource optimization must be treated as an engineering concern.

---

## 23.1 Monitoring

A background component shall monitor metrics such as:

```text
Application memory
CPU usage
Active virtual threads where measurable
Request volume
Tenant activity
Cache utilization
Document re-index operations
```

---

## 23.2 Tenant Resource Behavior

The system should identify suspicious usage patterns.

Example:

```text
Tenant A
↓
Excessive requests
↓
Low cache hit rate
↓
Repeated document re-indexing
↓
High CPU utilization
```

The application shall log or expose an alert.

---

## 23.3 Optimization Signals

Potential signals include:

```text
excessive uncached reads
excessive re-indexing
high request frequency
high processing frequency
abnormally high CPU
abnormally high memory
```

---

# 24. Requirement 15 — Disaster Recovery and Chaos Testing

## 24.1 PostgreSQL Failure

The system shall support a local failure experiment in which the primary database container is stopped.

Example:

```bash
docker stop db-primary
```

---

## 24.2 Failover

The application should recover against an available replica/failover mechanism without producing an uncontrolled cascade of fatal errors.

The exact implementation must distinguish between:

* PostgreSQL streaming replication
* connection failover
* application retry
* service discovery
* automatic primary promotion

The project shall not claim "instant failover" unless it has been measured.

---

## 24.3 Chaos Script

The repository shall include:

```text
scripts/chaos-failover.sh
```

The script shall:

1. identify the primary
2. stop/fail it
3. observe application behavior
4. verify replica/promotion behavior
5. verify application recovery
6. collect logs
7. report success/failure

---

# 25. Document Management Requirements

The core product must support:

## Create Document

```text
POST /documents
```

## Retrieve Document

```text
GET /documents/{id}
```

## Update Document

```text
PUT /documents/{id}
```

## Delete Document

```text
DELETE /documents/{id}
```

---

# 26. Workspace Requirements

The system shall support:

```text
Create Workspace
List Workspaces
Get Workspace
Update Workspace
Delete Workspace
```

Workspace access must be tenant-aware.

---

# 27. File Upload Requirements

The system shall support uploading documents such as:

```text
PDF
TXT
Markdown
```

Additional file formats may be added later.

After upload:

```text
File
 ↓
Metadata persisted
 ↓
Processing event
 ↓
Kafka
 ↓
Worker
 ↓
Text extraction
 ↓
Chunking
 ↓
Embedding
 ↓
Qdrant
```

---

# 28. AI Knowledge Query Requirements

A user should be able to ask:

```text
"What is our annual leave policy?"
```

The system shall:

```text
Question
 ↓
Embedding
 ↓
Semantic Retrieval
 ↓
Tenant-filtered Qdrant Search
 ↓
Relevant Chunks
 ↓
LLM / RAG Layer
 ↓
Answer
```

The system should return source references where practical:

```text
Answer:
Employees receive ...

Sources:
- Employee Handbook
- Leave Policy
```

---

# 29. Search Requirements

The system shall support:

## Semantic Search

Search by meaning rather than exact keywords.

## Metadata Filtering

At minimum:

```text
tenant_id
workspace_id
document_id
```

## Security

Search results must never cross tenant boundaries.

---

# 30. Caching Requirements

The cache shall support:

```text
Document Cache
Workspace Cache
Rate-Limit State
Temporary Processing State
```

Cache invalidation must happen when relevant underlying data changes.

Example:

```text
Update Document
 ↓
PostgreSQL update
 ↓
Invalidate Redis document cache
 ↓
Publish DocumentUpdated event
```

---

# 31. Event Model

Events should have a documented schema.

Example:

```json
{
  "eventId": "uuid",
  "eventType": "DocumentCreated",
  "tenantId": "tenant-123",
  "workspaceId": "workspace-456",
  "documentId": "document-789",
  "timestamp": "2026-08-06T10:00:00Z",
  "version": 1
}
```

Events must be designed with:

* versioning
* idempotency
* correlation IDs
* tenant identity
* timestamps

---

# 32. API Security Requirements

The backend shall enforce:

* authentication
* authorization
* tenant isolation
* input validation
* payload limits
* rate limiting
* secure password/identity handling through Keycloak
* JWT validation
* secure headers where applicable
* safe database access
* audit-friendly logging

---

# 33. Data Consistency Requirements

The system shall document consistency boundaries.

Examples:

### Strongly consistent

Core transactional database operations.

### Eventually consistent

Vector indexes.

### Eventually consistent

Caches.

### Asynchronous

Document processing.

### Event-driven

Cross-service processing.

The API shall communicate processing states clearly.

Example:

```text
UPLOADED
PROCESSING
INDEXED
FAILED
```

---

# 34. Document Processing State Machine

A document may move through:

```text
CREATED
   ↓
UPLOADED
   ↓
PROCESSING
   ↓
TEXT_EXTRACTED
   ↓
CHUNKED
   ↓
EMBEDDING
   ↓
INDEXED
```

Failure:

```text
PROCESSING
    ↓
FAILED
    ↓
RETRY
    ↓
PROCESSING
```

After retry exhaustion:

```text
FAILED
 ↓
DLQ
```

---

# 35. Reliability Requirements

The platform shall handle:

* temporary Kafka failures
* Redis failures
* database connection failures
* worker failures
* malformed documents
* duplicate events
* failed vector indexing
* delayed processing
* network failures
* service timeouts

Resilience mechanisms shall be explicit rather than hidden.

---

# 36. Idempotency

Important operations must be designed to tolerate retries.

For example:

```text
DocumentUploaded event
```

may arrive more than once.

The worker must not blindly create duplicate vector indexes.

Idempotency keys/event IDs shall be used where appropriate.

---

# 37. Logging Requirements

Logs must contain useful structured information.

Where applicable:

```text
timestamp
level
service
trace_id
span_id
tenant_id
user_id
event_id
request_id
operation
error
```

Sensitive information must not be logged.

---

# 38. Database Requirements

PostgreSQL shall store core relational data including:

```text
tenants
users / user references
workspaces
workspace_members
documents
document_versions
file_metadata
processing_jobs
audit records
```

Vector embeddings shall reside in Qdrant according to the primary architecture.

---

# 39. Database Transaction Requirements

Transactional boundaries shall be explicitly defined.

For example:

```text
Create Document
    ↓
BEGIN TRANSACTION
    ↓
Insert document
    ↓
Insert metadata
    ↓
COMMIT
    ↓
Publish asynchronous processing event
```

The design must avoid assuming that a database transaction automatically includes Kafka or Qdrant.

---

# 40. WebSocket Consistency Requirements

Real-time collaboration shall not blindly equate:

```text
WebSocket event = database commit
```

The system shall document the relationship between:

* real-time events
* persistence
* ordering
* reconnection
* duplicate messages
* eventual consistency

---

# 41. API Error Model

Errors shall have a consistent structure.

Example:

```json
{
  "timestamp": "...",
  "status": 404,
  "code": "DOCUMENT_NOT_FOUND",
  "message": "Document was not found",
  "traceId": "abc123"
}
```

The exact schema may evolve.

---

# 42. Testing Strategy

Testing must occur at multiple levels.

## Unit Tests

Test:

* services
* validation
* business rules
* token-bucket logic
* utility classes

## Integration Tests

Test:

```text
Spring Boot
PostgreSQL
Kafka
Redis
Qdrant
```

using Testcontainers where practical.

## Security Tests

Test:

* missing JWT
* invalid JWT
* expired JWT
* unauthorized role
* cross-tenant access

## API Tests

Test complete HTTP workflows.

## Event Tests

Test:

* publishing
* consuming
* retry
* duplicate messages
* DLQ

## Failure Tests

Test:

* Redis unavailable
* Kafka unavailable
* database unavailable
* worker unavailable

---

# 43. Load Testing

The repository shall include:

```text
/load-tests
```

with k6 scripts.

Tests should cover:

```text
Authentication
Document creation
Document retrieval
Search
Cached reads
Uncached reads
Concurrent updates
AI search
```

Metrics:

```text
RPS
p50
p95
p99
error rate
throughput
CPU
memory
```

---

# 44. Local Enterprise Environment

The complete system must be executable locally.

Docker Compose shall orchestrate the infrastructure.

The local topology shall include, as applicable:

```text
API Gateway
Core API
Worker
Kafka Broker 1
Kafka Broker 2
Kafka Broker 3
Redis Master 1
Redis Master 2
Redis Master 3
Redis Replica 1
Redis Replica 2
Redis Replica 3
Redis Sentinel
PostgreSQL Node 1
PostgreSQL Node 2
ShardingSphere Proxy
Qdrant
Ollama
Keycloak
OpenTelemetry Collector
Prometheus
Jaeger
Grafana
Temporal / Saga infrastructure
```

The exact container topology may be refined during implementation as long as the required architectural capabilities remain present.

---

# 45. Local-First Principle

The project must never remove an important engineering requirement simply because a free cloud provider cannot host it.

Instead:

```text
FULL INDUSTRIAL SYSTEM
        ↓
Docker Compose
        ↓
Developer's machine
```

Cloud deployment is an optional compatibility layer.

---

# 46. Cloud Deployment Strategy

The full architecture may exceed free cloud limits.

Therefore:

## Local

The complete topology runs locally.

## Cloud

A reduced deployment may use:

```text
Spring Boot
PostgreSQL
Redis
Keycloak / compatible identity
Qdrant / compatible vector service
```

where available.

Components that cannot reasonably be deployed on free infrastructure remain demonstrable locally.

The README must explicitly distinguish:

```text
FULL LOCAL TOPOLOGY
```

from:

```text
OPTIONAL CLOUD TOPOLOGY
```

---

# 47. Docker Requirements

Each application service shall have its own Dockerfile.

Target services:

```text
api-gateway
core-api
worker-service
```

Infrastructure shall be defined through Docker Compose.

Multi-stage Docker builds should be used where appropriate.

---

# 48. Repository Structure

The single GitHub repository shall use the following conceptual structure:

```text
knowledgehub/
│
├── .github/
│   └── workflows/
│       ├── ci.yml
│       └── cd.yml
│
├── infra/
│   ├── terraform/
│   └── README.md
│
├── local-environment/
│   ├── docker-compose.yml
│   ├── kafka/
│   ├── redis/
│   ├── postgres/
│   ├── keycloak/
│   ├── qdrant/
│   ├── ollama/
│   └── observability/
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── Dockerfile
│   └── package.json
│
├── api-gateway/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
│
├── core-api/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
│
├── worker-service/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
│
├── load-tests/
│   ├── k6/
│   └── README.md
│
├── scripts/
│   ├── chaos-failover.sh
│   ├── setup-local.sh
│   └── teardown-local.sh
│
├── docs/
│   ├── architecture/
│   ├── api/
│   ├── decisions/
│   ├── operations/
│   └── disaster-recovery/
│
├── README.md
└── LICENSE
```

---

# 49. Architecture Decision Records

The repository should contain ADRs documenting major decisions.

Examples:

```text
ADR-001 Why Spring Boot?
ADR-002 Why Java Virtual Threads?
ADR-003 Why Kafka?
ADR-004 Why Redis?
ADR-005 Why Qdrant?
ADR-006 Why Keycloak?
ADR-007 Why ShardingSphere?
ADR-008 Why tenant-based sharding?
ADR-009 Why WebFlux for WebSockets?
ADR-010 Why Saga/Temporal?
ADR-011 Why local-first architecture?
ADR-012 Why Docker Compose?
```

Each ADR should explain:

```text
Problem
Options considered
Decision
Trade-offs
Consequences
```

---

# 50. Observability Dashboard Requirements

Grafana dashboards should eventually expose:

### API

* request rate
* latency
* errors
* throughput

### JVM

* heap
* non-heap
* GC
* CPU
* thread behavior

### Kafka

* consumer lag
* throughput
* failures

### Redis

* operations
* hit/miss rate
* memory

### PostgreSQL

* connections
* query duration
* transaction behavior

### Worker

* jobs processed
* processing duration
* failures
* DLQ count

### AI

* embedding requests
* embedding latency
* vector search latency
* indexing failures

---

# 51. FinOps Dashboard Requirements

The cost/resource dashboard shall be interpreted locally as a resource-efficiency dashboard.

It shall track:

```text
CPU consumption
Memory consumption
Virtual-thread activity
Request volume
Cache hit rate
Re-index operations
Tenant workload
```

Alerts should identify potentially wasteful behavior.

---

# 52. Disaster Recovery Documentation

The repository shall document:

```text
What happens if PostgreSQL fails?
What happens if Redis fails?
What happens if Kafka fails?
What happens if a worker dies?
What happens if Qdrant fails?
What happens if an API instance dies?
What happens if the Gateway dies?
```

For each:

```text
Failure
↓
Detection
↓
System behavior
↓
Recovery
↓
Data consistency
↓
User impact
```

---

# 53. Chaos Engineering

Chaos tests shall include, where feasible:

```text
Stop PostgreSQL primary
Stop PostgreSQL replica
Stop Redis node
Stop Kafka broker
Stop worker
Restart API
Restart Gateway
Introduce processing failures
Force DLQ behavior
```

The objective is not simply to make the application crash.

The objective is to measure:

```text
How does the system behave under failure?
```

---

# 54. Security Boundaries

The security architecture is:

```text
Client
 ↓
Gateway
 ↓
JWT validation / routing
 ↓
Core API
 ↓
Authorization
 ↓
Tenant isolation
 ↓
Database / cache / vector store
```

Tenant identity must propagate through asynchronous workflows.

For example:

```text
HTTP Request
tenant=A
   ↓
Kafka Event
tenant=A
   ↓
Worker
tenant=A
   ↓
Qdrant
filter tenant=A
```

---

# 55. Data Privacy

The system must ensure:

* no cross-tenant vector retrieval
* no cross-tenant document access
* no sensitive information in logs
* authentication tokens are not logged
* uploaded files are protected
* access checks occur before document retrieval

---

# 56. Performance Engineering Requirements

The project shall not simply claim scalability.

It shall measure it.

Each optimization should answer:

```text
What was the bottleneck?
What change was made?
How was it measured?
What was the result?
What trade-off was introduced?
```

Examples:

```text
Redis caching
Virtual threads
Database indexing
Database sharding
Kafka asynchronous processing
Connection pool tuning
Qdrant indexing
```

---

# 57. Performance Evidence

The repository shall contain evidence such as:

```text
k6 output
Grafana screenshots
Jaeger traces
database metrics
Kafka consumer metrics
Redis cache statistics
```

The README should provide context for every benchmark.

---

# 58. Scalability Model

The architecture should conceptually support:

```text
                    Load Balancer
                         │
              ┌──────────┼──────────┐
              ▼          ▼          ▼
             API        API        API
              │          │          │
              └──────┬───┴────┬─────┘
                     │        │
                  Redis     Kafka
                     │        │
                     │    ┌───┼───┐
                     │    ▼   ▼   ▼
                     │   W1  W2  W3
                     │
                     ▼
               ShardingSphere
                  │       │
                  ▼       ▼
                 DB1     DB2
```

The local environment should simulate this architecture even when only one physical developer machine is available.

---

# 59. Functional Acceptance Criteria

The product is functionally successful when:

### Authentication

* users can authenticate through Keycloak
* JWT validation works
* roles are recognized
* unauthorized users are rejected

### Multi-tenancy

* tenant A cannot access tenant B's data
* tenant identity propagates through events
* tenant filtering applies to vector searches

### Documents

* users can create documents
* users can update documents
* users can delete documents
* users can retrieve documents

### Files

* users can upload supported files
* files are processed asynchronously
* failures can retry
* failed jobs reach DLQ

### Search

* documents are chunked
* embeddings are generated locally
* vectors are stored in Qdrant
* semantic search works
* metadata filters work

### AI

* users can ask questions
* relevant document chunks are retrieved
* the AI response is grounded in retrieved knowledge where applicable

### Cache

* cache hits avoid unnecessary database queries
* cache invalidation works
* rate limiting works atomically

### Kafka

* events are published
* workers consume events
* duplicate processing is controlled
* failed messages can reach DLQ

### WebSockets

* connected users receive real-time document events
* multiple server instances can synchronize through Redis Pub/Sub

### Observability

* traces are visible in Jaeger
* metrics are visible in Prometheus
* dashboards are visible in Grafana
* trace IDs correlate logs and requests

### Infrastructure

* local environment can be started through Docker Compose
* Terraform configuration exists
* CI pipeline executes successfully

### Disaster Recovery

* PostgreSQL failure scenario can be reproduced
* failover/recovery behavior is documented and tested

---

# 60. Non-Functional Acceptance Criteria

The system must be:

## Secure

Tenant boundaries cannot be bypassed through normal API operations.

## Observable

Distributed requests can be traced.

## Testable

Major infrastructure-dependent functionality has integration tests.

## Reproducible

Another developer can start the local environment using documented commands.

## Automated

Build/test/container validation is automated.

## Measurable

Performance claims include actual benchmark results.

## Recoverable

Important infrastructure failures have documented recovery behavior.

## Maintainable

Major architecture decisions are documented.

---

# 61. Definition of Done

The project is not considered complete merely because the API works.

A feature is considered complete when applicable:

```text
Implementation
+
Unit tests
+
Integration tests
+
Security validation
+
Logging
+
Metrics
+
Tracing
+
Failure behavior
+
Documentation
```

A system-level feature is considered complete when:

```text
Local deployment
+
Automated test
+
Failure test
+
Observability
+
Performance evidence
+
Documentation
```

---

# 62. Final Portfolio Demonstration

The final demonstration should show:

```text
1. Start infrastructure
        ↓
2. Start Gateway
        ↓
3. Start Core API
        ↓
4. Start Worker
        ↓
5. Authenticate through Keycloak
        ↓
6. Create tenant
        ↓
7. Create workspace
        ↓
8. Upload PDF
        ↓
9. Kafka event generated
        ↓
10. Worker consumes event
        ↓
11. PDF text extracted
        ↓
12. Text chunked
        ↓
13. Ollama generates embeddings
        ↓
14. Qdrant indexes vectors
        ↓
15. User performs semantic search
        ↓
16. AI answers question using retrieved context
        ↓
17. Redis caches frequently accessed data
        ↓
18. WebSocket broadcasts document changes
        ↓
19. Jaeger shows distributed trace
        ↓
20. Grafana shows system metrics
        ↓
21. k6 generates high load
        ↓
22. Chaos script kills database
        ↓
23. Recovery/failover is demonstrated
```

---

# 63. Final Technology Matrix

| Area                | Technology                   |
| ------------------- | ---------------------------- |
| Language            | Java 21+                     |
| Backend             | Spring Boot 3.4+             |
| API                 | Spring MVC / REST            |
| High Concurrency    | Java Virtual Threads         |
| Gateway             | Spring Cloud Gateway         |
| Resilience          | Resilience4j                 |
| Authentication      | Keycloak                     |
| Authorization       | Spring Security              |
| Messaging           | Apache Kafka                 |
| Async Worker        | Dedicated Spring Boot Worker |
| Job Processing      | Kafka / optional JobRunr     |
| Relational DB       | PostgreSQL                   |
| DB Proxy / Sharding | Apache ShardingSphere        |
| Cache               | Redis                        |
| Redis Client        | Lettuce                      |
| Atomic Rate Limit   | Redis Lua                    |
| Vector DB           | Qdrant                       |
| Embeddings          | Ollama + nomic-embed-text    |
| AI Integration      | LangChain4j                  |
| Workflow            | Temporal.io / Saga           |
| Real-Time           | Spring WebFlux               |
| WebSockets          | Reactive WebSockets          |
| Pub/Sub             | Redis Pub/Sub                |
| Metrics             | Micrometer + Prometheus      |
| Tracing             | OpenTelemetry                |
| Trace UI            | Jaeger                       |
| Dashboard           | Grafana                      |
| Containers          | Docker                       |
| Local Orchestration | Docker Compose               |
| IaC                 | Terraform                    |
| CI/CD               | GitHub Actions               |
| Integration Testing | Testcontainers               |
| Load Testing        | k6                           |
| Disaster Recovery   | PostgreSQL WAL + scripts     |
| Chaos Testing       | Bash/Python                  |
| Repository          | Single GitHub monorepo       |

---

# 64. Final Repository Architecture

```text
knowledgehub/
│
├── .github/workflows/
│   ├── ci.yml
│   └── cd.yml
│
├── infra/
│   └── terraform/
│
├── local-environment/
│   ├── docker-compose.yml
│   ├── kafka/
│   ├── redis/
│   ├── postgres/
│   ├── sharding-sphere/
│   ├── keycloak/
│   ├── qdrant/
│   ├── ollama/
│   └── observability/
│
├── frontend/
│
├── api-gateway/
│
├── core-api/
│
├── worker-service/
│
├── load-tests/
│   └── k6/
│
├── scripts/
│   ├── chaos-failover.sh
│   ├── setup-local.sh
│   └── teardown-local.sh
│
├── docs/
│   ├── architecture/
│   ├── adr/
│   ├── api/
│   ├── operations/
│   ├── performance/
│   └── disaster-recovery/
│
└── README.md
```

---

# 65. Engineering Philosophy

KnowledgeHub shall be developed with the following principles:

```text
Understand before optimizing.

Measure before claiming scalability.

Use asynchronous processing for genuinely asynchronous work.

Use caching when it solves a measured bottleneck.

Use sharding only where the data model and workload justify it.

Treat tenant isolation as a security boundary.

Treat external systems as failure-prone.

Design events for retries and idempotency.

Make distributed workflows observable.

Automate infrastructure.

Test failures, not only happy paths.

Prefer reproducible local infrastructure over inaccessible paid infrastructure.

Document architectural trade-offs.

Do not add technology solely for resume keywords.
```

---

# 66. Ultimate Project Objective

The finished KnowledgeHub project should demonstrate that its developer understands how a modern distributed Java backend is designed, implemented, tested, measured, deployed, observed, and recovered.

The project should ultimately answer these engineering questions with working code:

```text
How do we handle thousands of concurrent requests?

How do we authenticate and authorize users?

How do we isolate tenants?

How do we process expensive work without blocking API requests?

How do we reliably move events between services?

How do we prevent duplicate event processing?

How do we perform semantic search?

How do we integrate AI into a backend system?

How do we cache expensive reads?

How do we rate-limit distributed clients?

How do we scale database storage?

How do we coordinate distributed workflows?

How do multiple server instances communicate in real time?

How do we trace a request across services?

How do we monitor system health?

How do we automate infrastructure?

How do we test real infrastructure in CI?

How do we measure system performance?

How do we respond to infrastructure failure?

How do we recover from database failure?

How do we prove that our architecture works?
```

The final project is therefore not merely a "Notion clone."

It is a:

**Local-First, Multi-Tenant, AI-Powered, Event-Driven, Distributed Knowledge Management Platform built with Java and Spring Boot, with enterprise infrastructure, observability, scalability testing, automated infrastructure, CI/CD, real-time collaboration, and disaster-recovery experimentation.**
A real user should be able to open the application, register/login, create a workspace, create/edit documents, upload files, collaborate with others, search their knowledge, ask the AI questions, and see the entire request flow travel through the real backend infrastructure — all the way from UI → gateway → Java services → databases/message systems/AI → response. 

---

# 67. Core User Experience & Detailed User Journeys

To ensure the platform is built with a user-centric mindset, the frontend and backend must seamlessly cooperate to deliver several complete user journeys. Below are the detailed specifications for these journeys:

## 67.1 User Journey 1 — Onboarding, Identity, and Workspace Set-up
1. **Accessing the App**: The user navigates to the application URL. The frontend detects the absence of a valid OAuth2 session and redirects the user to the Keycloak login screen.
2. **Registration and Authentication**: The user either registers a new account or logs in with existing credentials. Keycloak handles password validation and user registration, mapping the user to a specific tenant ID.
3. **Session Establishment**: Upon successful authentication, Keycloak redirects the user back to the application gateway with an authorization code. The frontend exchanges this code for a JWT access token containing the user's roles (`ROLE_ADMIN` or `ROLE_TENANT_USER`) and `tenant_id`.
4. **Tenant Workspace Selection**: The user enters the dashboard. If they do not belong to an active workspace, they are prompted to create a new workspace (e.g., "Engineering Workspace").
5. **Team Management**: A user with `ROLE_ADMIN` invites other users to the workspace. Invites generate invitations that assign roles (`ROLE_TENANT_USER`) within the tenant boundary. The new users can then log in via Keycloak and gain access to the shared workspace.

## 67.2 User Journey 2 — Collaborative Document Lifecycle and WebSockets
1. **Document Creation**: A tenant user clicks "New Document". The frontend sends a `POST /api/v1/documents` request through the API Gateway to the Core API. The Core API commits the document to PostgreSQL and invalidates the Redis cache.
2. **Opening a Document**: The user opens the document. The frontend opens a WebSocket connection to the reactive WebFlux gateway on `/ws/documents/{docId}`. The connection is authenticated using the JWT access token.
3. **Collaborative Workspace Editing**: When another user joins the document, they establish their own WebSocket connection. As User A edits the document, change events (`document.content_changed`, `document.cursor_changed`) are sent over the WebSocket.
4. **Horizontal Synchronization**: The API instance receiving User A's changes publishes the event to Redis Pub/Sub. Other API instances subscribe to Redis Pub/Sub and forward the event to User B's WebSocket connection, achieving real-time synchrony.
5. **Persistence**: Transient cursor movements are not persisted, but actual content edits are debounced on the client and saved to the PostgreSQL database via a `PUT` request.

## 67.3 User Journey 3 — Document Ingestion, Background Processing, and RAG Q&A
1. **Document Upload**: A user drops a PDF file (e.g., `employee_handbook.pdf`) into the workspace file upload panel. The frontend sends a multipart request to the Core API, which persists the file metadata in PostgreSQL as `UPLOADED`.
2. **Asynchronous Trigger**: The Core API publishes a `DocumentUploaded` event to Kafka. The HTTP thread completes instantly, notifying the user that the file is processing.
3. **Worker Processing**: A Kafka consumer in the Worker service picks up the event. The worker changes the status to `PROCESSING` in PostgreSQL, downloads the document, extracts raw text, splits it into semantic chunks, and calls Ollama to generate vector embeddings.
4. **Vector Database Insertion**: The worker sends the embeddings to Qdrant, writing the vectors with metadata including the `tenant_id`, `workspace_id`, and `document_id`. The document status is updated to `INDEXED`.
5. **Status Observation**: The frontend polls or receives WebSocket updates (`document.processing_completed`), updating the status icon to green.
6. **AI Question & Answer**: The user types a question: *"What is the annual leave policy?"*. The frontend sends the query to the Core API.
7. **Similarity Retrieval**: The Core API converts the question to an embedding, queries Qdrant with a metadata filter specifying `tenant_id = user_tenant_id` to enforce data isolation, and retrieves the top-K relevant chunks.
8. **Prompt Construction and Answer Generation**: The retrieved text chunks are formatted into a prompt template and sent to Ollama (running the LLM locally). The LLM generates a grounded response.
9. **Display with Citations**: The frontend displays the answer along with clickable source citations referencing the specific documents and chunks retrieved from Qdrant.

---

# 68. Frontend Application Requirements

The frontend application must present a polished, modern, and highly responsive user interface that abstracts the underlying infrastructure complexity. It should include the following core views:

## 68.1 Authentication and Onboarding Screens
* **Login/Registration Pages**: Styled login interface, delegating username/password handling to Keycloak via OAuth2/OIDC redirect.
* **Workspace Setup**: A guided flow for new users to create their first tenant workspace or select from existing tenants they belong to.

## 68.2 Dashboard and Document Explorer
* **Sidebar Navigation**: Hierarchical list of workspaces and documents, with icons indicating document state (e.g., standard document vs. processing PDF file).
* **Workspace Management Console**: Accessible to `ROLE_ADMIN` users, allowing them to invite new users, assign roles, view tenant resource consumption, or request workspace deletion.

## 68.3 Interactive Document Editor and Viewer
* **Collaborative Rich-Text Canvas**: An editor component rendering Markdown structure, displaying cursor indicators of other active users.
* **WebSocket Status Banner**: Visual confirmation of the real-time collaboration status (e.g., "Connected", "Reconnecting", or "Offline").

## 68.4 Ingestion and Processing Panel
* **File Upload Dropzone**: A visual file upload area supporting PDF, TXT, and Markdown files.
* **Processing Status Matrix**: A detailed grid showing the ingestion state machine for each file: `UPLOADED` -> `PROCESSING` -> `INDEXED` (with progress bars) or `FAILED` (with diagnostic error summaries).

## 68.5 Semantic Search and AI Chat Interface
* **Search Switcher**: A search bar allowing users to toggle between keyword search and semantic (vector-based) search.
* **RAG Q&A Hub**: A split-screen interface displaying a chat window on one side and a document citation inspector on the other, allowing users to verify LLM-grounded answers.

## 68.6 System-Wide Error and Loading States
* **Skeleton Screen Loaders**: Applied during API queries or document loads to convey responsive transitions.
* **Toast Notification Hub**: Slides in to display transient messages, resilience circuit breaker warnings, rate limit block notifications, or down-service warnings.

---

# 69. Technical Ambiguities and Documented Limitations

In accordance with architectural planning, certain technically complex or potentially questionable requirements are explicitly documented with their resolutions and system limits:

## 69.1 PostgreSQL Sharding via Apache ShardingSphere Proxy
* **Technical Concern**: Sharding introduces query planning complexity, complicates cross-tenant analytics, and creates write-overhead for small datasets.
* **Proposed Solution**: ShardingSphere Proxy handles routing transparently. The system utilizes `tenant_id` as the sharding key.
* **Limitation**: Cross-tenant queries are strictly prevented by the database layer. Schema migrations must be run sequentially across each individual PostgreSQL physical node (Node 1 and Node 2).

## 69.2 Local-First LLM Inference (Ollama)
* **Technical Concern**: Local execution of models (e.g., Llama 3) on consumer hardware CPU/GPU will introduce massive latency (often >10s per response) and saturate system resources during concurrent load testing.
* **Proposed Solution**: The system is benchmarked locally using lightweight models (e.g., `nomic-embed-text` and `phi3`).
* **Cloud Strategy**: The PRD outlines a "Reduced Free-Tier Deployment" where local Ollama/Qdrant components are replaced with managed SaaS APIs (such as OpenAI/HuggingFace) to allow running on restricted free cloud resources.

## 69.3 Exactly-Once Event Processing
* **Technical Concern**: While Spring Kafka supports `exactly_once_v2` transactions, external systems (such as Qdrant vector database and Redis cache) do not support two-phase commit transactions.
* **Proposed Solution**: The Core API and Worker services implement strict idempotent receiver patterns. Document and chunk IDs are derived deterministically from the document hash, ensuring duplicate Kafka messages overwrite rather than duplicate vectors in Qdrant.

## 69.4 Event-Driven Workspace Deletion and Saga Patterns
* **Technical Concern**: A workspace deletion requires deleting data from PostgreSQL shards, Qdrant vectors, and Redis caches. A failure during this distributed sequence leaves orphaned records.
* **Orchestration**: The system implements an orchestration-based Saga using a state machine (or Temporal.io). If Qdrant vector deletion fails, the Saga logs the failure, marks the deletion as partial, and schedules a retry compensation workflow to reconcile the system.

---

# 70. Completeness Audit Checklist

The table below serves as the definitive completeness audit for all technology and topic coverage required in the Product Requirements Document:

| Required Item | PRD Section & References | Status |
| :--- | :--- | :--- |
| **Product vision** | Section 2: Product Vision | Covered |
| **End-to-end product** | Section 1, Section 7, Section 67 | Covered |
| **Frontend** | Section 7, Section 8, Section 68 | Covered |
| **Authentication** | Section 6, Section 13, Section 67 | Covered |
| **Keycloak** | Section 13.1, Section 20, Section 67 | Covered |
| **OAuth2/OIDC** | Section 13.2, Section 13.3, Section 67 | Covered |
| **JWT** | Section 13.3, Section 20, Section 67 | Covered |
| **Multi-tenancy** | Section 3.1, Section 14 | Covered |
| **Tenant isolation** | Section 14, Section 54, Section 55 | Covered |
| **Workspaces** | Section 26, Section 67 | Covered |
| **Documents** | Section 25, Section 67 | Covered |
| **File uploads** | Section 27, Section 67 | Covered |
| **AI / RAG** | Section 11, Section 28, Section 67 | Covered |
| **Ollama** | Section 11.3, Section 16, Section 69.2 | Covered |
| **Embeddings** | Section 11.3, Section 16, Section 27 | Covered |
| **Qdrant** | Section 11.1, Section 17, Section 69.3 | Covered |
| **Kafka** | Section 10, Section 14, Section 67 | Covered |
| **Events** | Section 10.2, Section 31 | Covered |
| **Consumer groups** | Section 14 | Covered |
| **Retries / DLQ** | Section 15.4, Section 34 | Covered |
| **Idempotency** | Section 10.3, Section 36, Section 69.3 | Covered |
| **Worker service** | Section 15, Section 67 | Covered |
| **PostgreSQL** | Section 19.1, Section 38 | Covered |
| **ShardingSphere / DB sharding** | Section 19.2, Section 19.3, Section 69.1 | Covered |
| **Redis / Distributed cache** | Section 12, Section 30 | Covered |
| **Lua / Rate limiting** | Section 12.4, Section 20.5 | Covered |
| **Spring Cloud Gateway** | Section 20.1, Section 67 | Covered |
| **Resilience4j** | Section 20.2 | Covered |
| **Virtual Threads** | Section 9.1, Section 9.2 | Covered |
| **WebSockets** | Section 22.1, Section 22.3, Section 67.2 | Covered |
| **Redis Pub/Sub** | Section 22.4, Section 67.2 | Covered |
| **Saga / Temporal** | Section 17.1, Section 69.4 | Covered |
| **OpenTelemetry** | Section 16, Section 50 | Covered |
| **Micrometer** | Section 16.2 | Covered |
| **Prometheus** | Section 16.5, Section 50 | Covered |
| **Grafana** | Section 16.5, Section 50 | Covered |
| **Jaeger** | Section 16.1, Section 50 | Covered |
| **Docker / Docker Compose** | Section 44, Section 47 | Covered |
| **Terraform / IaC** | Section 18, Section 48 | Covered |
| **GitHub Actions** | Section 21, Section 48 | Covered |
| **Testcontainers** | Section 21.2, Section 42 | Covered |
| **k6 / Load testing** | Section 9.4, Section 43 | Covered |
| **FinOps / resource awareness** | Section 23, Section 51 | Covered |
| **Security** | Section 20, Section 54, Section 55 | Covered |
| **Testing Layers** | Section 42 | Covered |
| **CI/CD** | Section 21 | Covered |
| **Disaster recovery / WAL** | Section 24, Section 52 | Covered |
| **Failover / Chaos testing** | Section 24.3, Section 53 | Covered |
| **Free-tier deployment** | Section 46, Section 69.2 | Covered |
| **Local-first deployment** | Section 45, Section 46 | Covered |
| **User journeys** | Section 67 | Covered |
| **Acceptance criteria** | Section 59, Section 60 | Covered |
| **Definition of Done** | Section 61 | Covered |
 
