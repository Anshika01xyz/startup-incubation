package com.startup.platform.service;

import com.startup.platform.model.*;
import com.startup.platform.repository.*;
import com.startup.platform.exception.*;
import com.startup.platform.util.*;
import com.startup.platform.config.*;

import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** File persistence and Java object serialization for the reporting coursework unit. */
public final class SnapshotService {
    private SnapshotService() {}

    public static Path export(PlatformService service, Session session) throws Exception {
        List<String> startups = new ArrayList<>(), milestones = new ArrayList<>();
        for (String[] row : service.startup360(session)) startups.add(String.join(" | ", row));
        for (String[] row : service.milestoneHealth(session)) milestones.add(String.join(" | ", row));
        PlatformSnapshot snapshot = new PlatformSnapshot(LocalDateTime.now(), startups, milestones);
        Path directory = Path.of("reports");
        Files.createDirectories(directory);
        Path file = directory.resolve("platform-snapshot.ser");
        try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(file))) {
            output.writeObject(snapshot);
        }
        return file;
    }

    public static PlatformSnapshot read(Path file) throws Exception {
        if (file == null || !Files.isRegularFile(file) || !Files.isReadable(file))
            throw new IllegalArgumentException("Select a readable snapshot file.");
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(file))) {
            input.setObjectInputFilter(info -> {
                Class<?> type = info.serialClass();
                if (type == null) return ObjectInputFilter.Status.UNDECIDED;
                String name = type.getName();
                if (type.isArray() && (type.getComponentType() == Object.class || type.getComponentType() == String.class))
                    return ObjectInputFilter.Status.ALLOWED;
                return name.equals(PlatformSnapshot.class.getName()) || name.equals(String.class.getName())
                        || name.startsWith("java.time.") || name.startsWith("java.util.")
                        ? ObjectInputFilter.Status.ALLOWED : ObjectInputFilter.Status.REJECTED;
            });
            Object value = input.readObject();
            if (!(value instanceof PlatformSnapshot snapshot))
                throw new IllegalArgumentException("The selected file is not a Foundry platform snapshot.");
            return snapshot;
        }
    }
}
