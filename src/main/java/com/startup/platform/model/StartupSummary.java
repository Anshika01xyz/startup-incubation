package com.startup.platform.model;

import com.startup.platform.model.Identifiable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/** Immutable view model used by analytics and reports. */
public record StartupSummary(Long id, String name, String sector, String city,
                             double progressPercent, double readinessScore, int completedMilestones,
                             int totalMilestones, BigDecimal fundingRequested,
                             BigDecimal fundingCommitted, LocalDateTime updatedAt)
        implements Identifiable<Long> {
    public StartupSummary {
        if (id == null || id <= 0) throw new IllegalArgumentException("Startup ID must be positive.");
        name = Objects.requireNonNull(name, "name").trim();
        sector = sector == null ? "Unspecified" : sector.trim();
        city = city == null ? "Unspecified" : city.trim();
        if (progressPercent < 0 || progressPercent > 100) throw new IllegalArgumentException("Progress must be between 0 and 100.");
        if (readinessScore < 0 || readinessScore > 100) throw new IllegalArgumentException("Readiness score must be between 0 and 100.");
        if (completedMilestones < 0 || totalMilestones < completedMilestones) throw new IllegalArgumentException("Milestone counts are inconsistent.");
        fundingRequested = Objects.requireNonNullElse(fundingRequested, BigDecimal.ZERO);
        fundingCommitted = Objects.requireNonNullElse(fundingCommitted, BigDecimal.ZERO);
        updatedAt = Objects.requireNonNullElseGet(updatedAt, LocalDateTime::now);
    }
}
