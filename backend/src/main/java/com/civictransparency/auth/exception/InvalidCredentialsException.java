package com.civictransparency.auth.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("The email or username and password combination is not valid.");
    }
}
