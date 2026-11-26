package com.startup.platform;

import com.startup.platform.util.Security;
import com.startup.platform.model.PlatformSnapshot;
import com.startup.platform.service.SnapshotService;
import com.startup.platform.util.DatasetFileService;
import com.startup.platform.service.ReadinessCalculator;
import com.startup.platform.service.StartupAnalyticsService;
import com.startup.platform.model.EvaluationScorecard;
import com.startup.platform.model.StartupSummary;
import com.startup.platform.repository.InMemoryRepository;
import com.startup.platform.util.CollectionExercises;
import com.startup.platform.util.CsvImporter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CoreVerification {
    public static void main(String[] args) {
        check(new ReadinessCalculator().calculate(new EvaluationScorecard(8, 8, 8, 8)) == 80.0, "readiness score");
        String hash = Security.hash("FoundryDemo#2026");
        check(Security.verify("FoundryDemo#2026", hash), "password verification");
        check(!Security.verify("wrong-password", hash), "wrong password rejection");
        var first = new StartupSummary(1L, "Aster", "Health", "Ludhiana", 80, 80, 1, 2, BigDecimal.TEN, BigDecimal.ONE, LocalDateTime.now());
        var second = new StartupSummary(2L, "Birch", "Health", "Patiala", 60, 60, 0, 2, BigDecimal.TEN, BigDecimal.ZERO, LocalDateTime.now());
        var repo = new InMemoryRepository<StartupSummary, Long>(); repo.save(first); repo.save(second);
        check(repo.findById(1L).isPresent(), "generic repository and Optional");
        var analytics = new StartupAnalyticsService();
        check(analytics.countBySector(repo.findAll()).get("Health") == 2L, "Stream grouping");
        check(analytics.topByReadiness(repo.findAll(), 1).getFirst().id().equals(1L), "Stream sorting");
        check(CollectionExercises.countStatuses(List.of("OPEN", "OPEN")).get("OPEN") == 2L, "Map aggregation");
        try {
            Path csv = Files.createTempFile("foundry-preview", ".csv");
            Files.writeString(csv, "company_name,sector,excerpt\n\"Aster\nLabs\",Health,\"first\nround\"\nBirch,Health,second\n");
            check(new DatasetFileService().preview(csv, 1).dataRows() == 2, "CSV file preview and row counting");
            var parsed = CsvImporter.parseCsv(new java.io.StringReader("company_name,excerpt\n\"Aster\nLabs\",\"quoted, text\"\n"));
            check(parsed.size() == 2 && parsed.get(1)[0].equals("Aster\nLabs") && parsed.get(1)[1].equals("quoted, text"), "CSV quoted comma and multiline parser");
            Path snapshot = Files.createTempFile("foundry-snapshot", ".ser");
            try (var out = new java.io.ObjectOutputStream(Files.newOutputStream(snapshot))) {
                out.writeObject(new PlatformSnapshot(LocalDateTime.now(), List.of("startup"), List.of("milestone")));
            }
            check(SnapshotService.read(snapshot).startupSummaries().size() == 1, "Filtered Java serialization read");
            Files.deleteIfExists(csv); Files.deleteIfExists(snapshot);
        } catch (Exception exception) { throw new AssertionError("File/serialization verification failed", exception); }
        System.out.println("PASS: 11 core checks completed.");
    }
    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError("FAILED: " + name);
        System.out.println("PASS: " + name);
    }
}
