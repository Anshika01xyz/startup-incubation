package com.startup.platform.exceptions;

/** Checked exception: an account with this email already exists. */
public class DuplicateUserException extends Exception {
    public DuplicateUserException(String message) { super(message); }
}
