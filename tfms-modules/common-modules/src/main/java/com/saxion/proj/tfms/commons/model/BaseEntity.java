package com.saxion.proj.tfms.commons.model;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@MappedSuperclass
public abstract class BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        ZonedDateTime now = ZonedDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }
}