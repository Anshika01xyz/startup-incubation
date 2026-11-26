package com.startup.platform.util;

import com.startup.platform.config.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** File-I/O and Stream API helpers for validating a selected external CSV before import. */
public final class DatasetFileService {
    public record Preview(String header, long dataRows, List<String> sampleRows) {}

    public Preview preview(Path csv, int sampleLimit) throws IOException {
        if (csv == null || !Files.isRegularFile(csv) || !Files.isReadable(csv))
            throw new IOException("Select a readable CSV file.");
        if (sampleLimit < 0 || sampleLimit > 20) throw new IllegalArgumentException("Sample limit must be between 0 and 20.");
        String content = Files.readString(csv, StandardCharsets.UTF_8);
        if (content.isBlank()) throw new IOException("The selected CSV is empty.");
        long logicalRows = countCsvRecords(content);
        try (var lines = Files.lines(csv, StandardCharsets.UTF_8)) {
            List<String> nonBlank = lines.filter(line -> !line.isBlank()).toList();
            String header = nonBlank.getFirst().replace("\uFEFF", "");
            return new Preview(header, Math.max(0, logicalRows - 1),
                    nonBlank.stream().skip(1).limit(sampleLimit).toList());
        }
    }

    private static long countCsvRecords(String content) throws IOException {
        long records = 0;
        boolean quoted = false;
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < content.length() && content.charAt(i + 1) == '"') i++;
                else quoted = !quoted;
            } else if ((ch == '\n' || ch == '\r') && !quoted) {
                records++;
                if (ch == '\r' && i + 1 < content.length() && content.charAt(i + 1) == '\n') i++;
            }
        }
        if (quoted) throw new IOException("CSV contains an unclosed quoted field.");
        if (!content.endsWith("\n") && !content.endsWith("\r")) records++;
        return records;
    }
}
