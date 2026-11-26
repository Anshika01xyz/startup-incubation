package com.startup.platform.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/** Serializable point-in-time snapshot for the Java serialization coursework demonstration. */
public record PlatformSnapshot(LocalDateTime generatedAt, List<String> startupSummaries,
                               List<String> milestoneSummaries) implements Serializable {
    private static final long serialVersionUID = 1L;
}
