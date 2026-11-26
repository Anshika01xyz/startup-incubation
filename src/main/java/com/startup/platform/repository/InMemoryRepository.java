package com.startup.platform.repository;

import com.startup.platform.model.*;
import com.startup.platform.util.*;

import com.startup.platform.model.Identifiable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Generic repository implementation used for deterministic domain tests and temporary UI state. */
public final class InMemoryRepository<T extends Identifiable<ID>, ID> implements CrudRepository<T, ID> {
    private final ConcurrentMap<ID, T> records = new ConcurrentHashMap<>();
    @Override public Optional<T> findById(ID id) { return Optional.ofNullable(records.get(id)); }
    @Override public List<T> findAll() { return records.values().stream().toList(); }
    @Override public T save(T entity) {
        if (entity == null || entity.id() == null) throw new IllegalArgumentException("Entity and its ID are required.");
        records.put(entity.id(), entity);
        return entity;
    }
    @Override public boolean deleteById(ID id) { return records.remove(id) != null; }
}
