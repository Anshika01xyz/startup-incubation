package com.startup.platform.exception;

public final class AuthorizationException extends SecurityException {
    private static final long serialVersionUID = 1L;
    public AuthorizationException(String message) { super(message); }
}
