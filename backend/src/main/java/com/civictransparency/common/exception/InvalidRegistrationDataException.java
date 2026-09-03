package com.civictransparency.common.exception;

public class InvalidRegistrationDataException
        extends RuntimeException {

    public InvalidRegistrationDataException() {
        super("The registration details provided are invalid.");
    }
}
