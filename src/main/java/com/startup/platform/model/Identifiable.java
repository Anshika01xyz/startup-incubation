package com.startup.platform.model;

/** Common contract shared by persistent domain objects. */
public interface Identifiable<ID> {
    ID id();
}
