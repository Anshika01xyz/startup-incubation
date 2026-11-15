package com.startup.platform.auth;

/**
 * Platform roles. Names MUST match roles.role_name in the database.
 * Owner: Shaffy. Changes need team agreement (see docs/design/module2_auth_design.md).
 */
public enum Role {
    ADMIN,
    FOUNDER,
    INCUBATOR_MANAGER,
    EVALUATOR,
    MENTOR,
    INVESTOR;

    /** Safe lookup from a database string. */
    public static Role fromDb(String value) {
        return Role.valueOf(value.trim().toUpperCase());
    }
}
