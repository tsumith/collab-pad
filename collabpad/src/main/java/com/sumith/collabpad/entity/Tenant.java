package com.sumith.collabpad.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A unique identifier for the tenant, useful for external APIs instead of
    // exposing the database Long ID.
    @Column(nullable = false, unique = true, updatable = false)
    private UUID tenantId = UUID.randomUUID();

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    // This method runs automatically right before the entity is saved to the
    // database for the first time
    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    // This method runs automatically right before the entity is updated in the
    // database
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
