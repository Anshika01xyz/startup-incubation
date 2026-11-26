package com.startup.platform.model;

/** Four-criterion evaluation used by incubators to assess startup readiness. */
public record EvaluationScorecard(double innovation, double market, double feasibility, double team) {
    public EvaluationScorecard {
        validate("Innovation", innovation);
        validate("Market", market);
        validate("Feasibility", feasibility);
        validate("Team", team);
    }
    private static void validate(String label, double score) {
        if (!Double.isFinite(score) || score < 0 || score > 10)
            throw new IllegalArgumentException(label + " score must be between 0 and 10.");
    }
}
