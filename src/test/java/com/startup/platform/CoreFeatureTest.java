package com.startup.platform;

import com.startup.platform.service.ReadinessCalculator;
import com.startup.platform.service.StartupAnalyticsService;
import com.startup.platform.model.EvaluationScorecard;
import com.startup.platform.model.FounderProfile;
import com.startup.platform.model.MentorProfile;
import com.startup.platform.model.StartupSummary;
import com.startup.platform.repository.InMemoryRepository;
import com.startup.platform.util.CollectionExercises;
import com.startup.platform.util.CsvImporter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoreFeatureTest {
    @Test void readinessScoreUsesEqualFourCriterionWeights() {
        assertEquals(80.0, new ReadinessCalculator().calculate(new EvaluationScorecard(8, 8, 8, 8)));
    }

    @Test void invalidScorecardIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new EvaluationScorecard(11, 8, 8, 8));
    }

    @Test void profileInheritanceProvidesPolymorphicSummaries() {
        var now = LocalDateTime.now();
        assertEquals("FOUNDER", new FounderProfile(1, "A", "a@b.in", "Builder", now).profileType());
        assertTrue(new MentorProfile(2, "B", "b@b.in", "Finance", 5, now).summary().contains("5 years"));
    }

    @Test void genericRepositoryAndOptionalWork() {
        var repository = new InMemoryRepository<StartupSummary, Long>();
        var startup = sample(1L, "Aster");
        repository.save(startup);
        assertTrue(repository.findById(1L).isPresent());
        assertTrue(repository.deleteById(1L));
        assertTrue(repository.findById(1L).isEmpty());
    }

    @Test void streamAnalyticsSortAndGroupStartups() {
        var analytics = new StartupAnalyticsService();
        var data = List.of(sample(1L, "Aster"), sample(2L, "Birch"));
        assertEquals(2, analytics.countBySector(data).get("Health").longValue());
        assertEquals("Aster", analytics.topByReadiness(data, 1).getFirst().name());
        assertTrue(analytics.findById(data, 2L).isPresent());
    }

    @Test void collectionHelpersUseGenericsAndStreams() {
        assertEquals(List.of("A", "B"), CollectionExercises.nonNullCopy(java.util.Arrays.asList("A", null, "B")));
        assertEquals(2L, CollectionExercises.countStatuses(List.of("OPEN", "OPEN", "CLOSED")).get("OPEN"));
        assertEquals("Alpha", CollectionExercises.firstMatching(List.of("Beta", "Alpha"), "Al").orElseThrow());
    }


    @Test void csvParserPreservesQuotedCommasAndMultilineFields() throws Exception {
        var rows = CsvImporter.parseCsv(new java.io.StringReader("company_name,excerpt\n\"Aster\nLabs\",\"quoted, text\"\n"));
        assertEquals(2, rows.size());
        assertEquals("Aster\nLabs", rows.get(1)[0]);
        assertEquals("quoted, text", rows.get(1)[1]);
    }

    @Test void readinessAnalyticsUsesPersistedScorecardValue() {
        var analytics = new StartupAnalyticsService();
        assertEquals(80.0, analytics.readinessScore(sample(9L, "Cedar")));
    }

    private static StartupSummary sample(Long id, String name) {
        return new StartupSummary(id, name, "Health", "Ludhiana", 80, 80, 1, 2,
                BigDecimal.valueOf(100), BigDecimal.valueOf(50), LocalDateTime.now());
    }
}
