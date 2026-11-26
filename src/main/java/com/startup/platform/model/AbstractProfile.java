package com.startup.platform.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** Shared state and polymorphic behaviour for platform account profiles. */
public abstract class AbstractProfile {
    private final long userId;
    private final String fullName;
    private final String email;
    private final LocalDateTime createdAt;

    protected AbstractProfile(long userId, String fullName, String email, LocalDateTime createdAt) {
        if (userId <= 0) throw new IllegalArgumentException("User ID must be positive.");
        this.userId = userId;
        this.fullName = requireText(fullName, "Full name");
        this.email = requireText(email, "Email");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public final long userId() { return userId; }
    public final String fullName() { return fullName; }
    public final String email() { return email; }
    public final LocalDateTime createdAt() { return createdAt; }
    public abstract String profileType();
    public abstract String summary();

    protected static String requireText(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " is required.");
        return value.trim();
    }
}
