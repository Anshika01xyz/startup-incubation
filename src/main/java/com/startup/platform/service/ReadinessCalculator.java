package com.startup.platform.service;

import com.startup.platform.model.*;
import com.startup.platform.repository.*;
import com.startup.platform.exception.*;
import com.startup.platform.util.*;
import com.startup.platform.config.*;

import com.startup.platform.model.EvaluationScorecard;

/** Rule-based readiness formula: equal weight for innovation, market, feasibility and team. */
public final class ReadinessCalculator {
    public double calculate(EvaluationScorecard scorecard) {
        double average = (scorecard.innovation() + scorecard.market()
                + scorecard.feasibility() + scorecard.team()) / 4.0;
        return Math.round(Math.clamp(average * 10.0, 0.0, 100.0) * 10.0) / 10.0;
    }
}
