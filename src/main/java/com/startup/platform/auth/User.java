package com.startup.platform.auth;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** Shared user model. passwordHash is never shown in the UI or written to logs. */
public class User {

    private int userId;
    private String fullName;
    private String email;
    private String passwordHash;
    private boolean active;
    private Set<Role> roles = EnumSet.noneOf(Role.class);
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public User() { }

    public User(int userId, String fullName, String email, String passwordHash,
                boolean active, Set<Role> roles, LocalDateTime createdAt, LocalDateTime lastLoginAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = active;
        setRoles(roles);
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public boolean hasRole(Role role) { return roles.contains(role); }

    public boolean hasAnyRole(Role... candidates) {
        for (Role r : candidates) {
            if (roles.contains(r)) return true;
        }
        return false;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Set<Role> getRoles() { return Collections.unmodifiableSet(roles); }
    public void setRoles(Set<Role> roles) {
        this.roles = (roles == null || roles.isEmpty())
                ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(roles);
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    @Override
    public String toString() {   // deliberately excludes passwordHash
        return "User{id=" + userId + ", email=" + email + ", roles=" + roles + ", active=" + active + "}";
    }
}
