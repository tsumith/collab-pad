This document tracks future refactoring, technical debt, and items we are intentionally deferring to later stages.

## Deferred Features (Moved to Backlog)

### Real-Time WebSockets & Caching
- **WebSockets**: Live collaborative editing via STOMP/WebSockets.
- **Redis Pub/Sub**: Multi-server sync for WebSockets.
- *(Note: We pivoted to build the Auth/Gateway foundation first. We will return to this when the core is rock solid).*

### Event-Driven & Async Processing
- **Apache Kafka**: Decoupled events and background worker processes.
- **Saga Pattern**: Distributed transactions.

### AI & Semantic Search Integration
- **Qdrant**: Vector database for storing document embeddings.
- **Ollama**: Local LLM for RAG queries.

## Addressed Items
- ~~**PostgreSQL Infrastructure**~~: Completed (Docker Compose).

## Technical Debt (To Fix Next)
- **User Mapping**: Replace hardcoded `authorId = 1L` in `DocumentController.java` by properly syncing Keycloak users with our Postgres `User` table.
- **Workspace Selection**: Replace hardcoded `activeWorkspaceId = 1` in `App.jsx` by building a Workspace dropdown selector in the UI.
- ~~**User Role Management & API Headers**~~: We are implementing Keycloak right now!
