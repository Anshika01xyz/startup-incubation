package com.startup.platform.exceptions;

/** Checked exception: credentials are valid but the account is deactivated. */
public class InactiveUserException extends AuthenticationException {
    public InactiveUserException(String message) { super(message); }
}
