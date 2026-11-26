package com.startup.platform.model;

import java.time.LocalDateTime;

public final class MentorProfile extends AbstractProfile {
    private final String expertise;
    private final int experienceYears;
    public MentorProfile(long id, String name, String email, String expertise, int experienceYears, LocalDateTime createdAt) {
        super(id, name, email, createdAt);
        if (experienceYears < 0) throw new IllegalArgumentException("Experience cannot be negative.");
        this.expertise = requireText(expertise, "Expertise");
        this.experienceYears = experienceYears;
    }
    public String expertise() { return expertise; }
    public int experienceYears() { return experienceYears; }
    @Override public String profileType() { return "MENTOR"; }
    @Override public String summary() { return fullName() + " — " + expertise + " (" + experienceYears + " years)"; }
}
