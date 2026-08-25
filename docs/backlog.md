# KnowledgeHub Backlog / Tech Debt

This document tracks future refactoring, technical debt, and items we are intentionally deferring to later stages of the implementation plan.

## Future Changes / Refactors

### Security & Roles (Stage 6)
- **User Role Management**: Currently, the `role` in the `User` entity is stored as a simple `String`. In Stage 6, when we introduce **Keycloak**, we need to remove this field and rely on Keycloak's OIDC tokens and Claims for role-based access control.
- **API Authentication Headers**: The `DocumentController` currently relies on `X-Tenant-Id` and `X-User-Id` HTTP headers. In Stage 6, these must be removed and replaced by extracting the IDs securely from the Keycloak JWT token to prevent spoofing.

### Infrastructure
- **PostgreSQL**: Switch from H2 in-memory DB back to PostgreSQL once Docker/WSL issues are resolved.
