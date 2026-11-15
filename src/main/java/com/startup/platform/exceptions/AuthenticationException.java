package com.startup.platform.exceptions;

/** Checked exception: wrong email/password or unknown user. */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) { super(message); }
    public AuthenticationException(String message, Throwable cause) { super(message, cause); }
}
