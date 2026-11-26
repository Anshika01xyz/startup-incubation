package com.startup.platform.util;

import com.startup.platform.config.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Small reusable collection/generics examples based on real platform data. */
public final class CollectionExercises {
    private CollectionExercises() {}

    public static <T> List<T> nonNullCopy(List<T> source) {
        return source.stream().filter(java.util.Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
    }

    public static Map<String, Long> countStatuses(List<String> statuses) {
        Map<String, Long> counts = new HashMap<>();
        for (String status : statuses) {
            if (status != null && !status.isBlank()) counts.merge(status, 1L, Long::sum);
        }
        return Map.copyOf(counts);
    }

    public static Optional<String> firstMatching(List<String> values, String prefix) {
        return values.stream().filter(v -> v != null && v.startsWith(prefix)).findFirst();
    }
}
