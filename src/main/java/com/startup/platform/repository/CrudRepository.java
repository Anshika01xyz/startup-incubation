package com.startup.platform.repository;

import com.startup.platform.model.*;
import com.startup.platform.util.*;

import com.startup.platform.model.Identifiable;
import java.util.List;
import java.util.Optional;

/** Generic CRUD contract; JDBC DAOs implement the same persistence responsibilities per module. */
public interface CrudRepository<T extends Identifiable<ID>, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    T save(T entity);
    boolean deleteById(ID id);
}
