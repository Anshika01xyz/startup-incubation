package com.startup.platform.model;

import java.time.LocalDateTime;

public final class FounderProfile extends AbstractProfile {
    private final String bio;
    public FounderProfile(long id, String name, String email, String bio, LocalDateTime createdAt) {
        super(id, name, email, createdAt);
        this.bio = bio == null ? "" : bio.trim();
    }
    public String bio() { return bio; }
    @Override public String profileType() { return "FOUNDER"; }
    @Override public String summary() { return fullName() + " — founder" + (bio.isBlank() ? "" : ": " + bio); }
}
