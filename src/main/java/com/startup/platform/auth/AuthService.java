package com.startup.platform.auth;

import com.startup.platform.exceptions.AuthenticationException;
import com.startup.platform.exceptions.DuplicateUserException;

import java.util.List;
import java.util.Optional;

/**
 * COMMON AUTHENTICATION CONTRACT.
 * Owner: Shaffy. Other modules CALL this; they never edit it.
 * Implementation: AuthServiceImpl. Passwords are hashed with BCrypt.
 */
public interface AuthService {

    /**
     * Validates credentials, updates last_login_at, stores the user in SessionContext.
     * @throws AuthenticationException unknown email or wrong password (same message for both)
     * @throws com.startup.platform.exceptions.InactiveUserException account is deactivated
     * @throws IllegalArgumentException blank email or password
     */
    User login(String email, String plainPassword) throws AuthenticationException;

    /** Clears SessionContext. */
    void logout();

    /**
     * Self-registration (founders/investors) or admin-created account.
     * @throws DuplicateUserException email already registered
     * @throws IllegalArgumentException invalid email format / weak password / blank name
     */
    User register(String fullName, String email, String plainPassword, Role role)
            throws DuplicateUserException;

    /** Returns the logged-in user, or empty when nobody is logged in. */
    Optional<User> getCurrentUser();

    Optional<User> findById(int userId);

    /** @throws AuthenticationException old password is wrong */
    void changePassword(int userId, String oldPassword, String newPassword)
            throws AuthenticationException;

    // ---- Admin operations (require Permission.USER_MANAGE) ----
    List<User> listUsers(User actor);
    void assignRole(User actor, int userId, Role role);
    void deactivateUser(User actor, int userId);
    void activateUser(User actor, int userId);
}
