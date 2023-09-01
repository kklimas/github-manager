package com.github.manager.web.exception.github;

public class RequestLimitExceededException extends RuntimeException {
    private static final String FORBIDDEN_MESSAGE = "API rate limit exceeded.";

    public RequestLimitExceededException() {
        super(FORBIDDEN_MESSAGE);
    }
}
