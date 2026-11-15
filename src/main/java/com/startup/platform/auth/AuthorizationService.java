package com.startup.platform.auth;

import com.startup.platform.exceptions.UnauthorizedException;

import java.util.Set;

/**
 * COMMON AUTHORIZATION CONTRACT. Owner: Shaffy.
 *
 * Usage rule for every service method that changes or exposes protected data:
 *
 *     authz.requirePermission(actor, Permission.APPLICATION_DECIDE);
 *
 * Hiding a button in the UI is NOT enough; the service must call this.
 */
public interface AuthorizationService {

    boolean hasPermission(User user, Permission permission);

    /** @throws UnauthorizedException if user is null, inactive, or lacks the permission */
    void requirePermission(User user, Permission permission);

    /** @throws UnauthorizedException if user holds none of the given roles */
    void requireAnyRole(User user, Role... roles);

    boolean canAccessModule(User user, AppModule module);

    /** Used by the JavaFX shell to build the navigation menu. */
    Set<AppModule> getAccessibleModules(User user);

    Set<Permission> getPermissions(Role role);
}
