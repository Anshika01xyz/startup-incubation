package com.startup.platform.service;

import com.startup.platform.model.*;
import com.startup.platform.repository.*;
import com.startup.platform.exception.*;
import com.startup.platform.util.*;
import com.startup.platform.config.*;

import com.startup.platform.model.StartupSummary;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Stream-based analytics used for dashboard summaries and transparent readiness scoring. */
public final class StartupAnalyticsService {
    /** Uses the scorecard value already calculated by the same rule as the MySQL Startup 360 view. */
    public double readinessScore(StartupSummary startup) {
        return startup.readinessScore();
    }

    public Map<String, Long> countBySector(List<StartupSummary> startups) {
        return startups.stream().collect(Collectors.groupingBy(
                s -> s.sector().isBlank() ? "Unspecified" : s.sector(),
                Collectors.counting()));
    }

    public List<StartupSummary> topByReadiness(List<StartupSummary> startups, int limit) {
        if (limit < 1) throw new IllegalArgumentException("Limit must be positive.");
        return startups.stream().sorted(Comparator.comparingDouble(this::readinessScore).reversed()
                .thenComparing(StartupSummary::name)).limit(limit).toList();
    }

    public Optional<StartupSummary> findById(List<StartupSummary> startups, long id) {
        return startups.stream().filter(s -> s.id() == id).findFirst();
    }
}
