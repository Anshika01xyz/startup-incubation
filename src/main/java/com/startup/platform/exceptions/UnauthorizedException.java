package com.startup.platform.exceptions;

/**
 * Unchecked exception: thrown by services when the current user lacks the
 * role/permission for an operation (e.g. a FOUNDER calling an evaluator method).
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) { super(message); }
}
