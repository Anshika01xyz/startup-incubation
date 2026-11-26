package com.startup.platform.model;

import java.time.LocalDateTime;

public final class InvestorProfile extends AbstractProfile {
    private final String organization;
    public InvestorProfile(long id, String name, String email, String organization, LocalDateTime createdAt) {
        super(id, name, email, createdAt);
        this.organization = organization == null ? "Independent" : organization.trim();
    }
    public String organization() { return organization; }
    @Override public String profileType() { return "INVESTOR"; }
    @Override public String summary() { return fullName() + " — investor at " + organization; }
}
