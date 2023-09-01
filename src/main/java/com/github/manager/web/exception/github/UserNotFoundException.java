package com.github.manager.web.exception.github;

public class UserNotFoundException extends RuntimeException {
    private static final String NOT_FOUND_MESSAGE = "Github user was not found.";

    public UserNotFoundException() {
        super(NOT_FOUND_MESSAGE);
    }
}
