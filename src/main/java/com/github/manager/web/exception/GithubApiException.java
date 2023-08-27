package com.github.manager.web.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class GithubApiException extends RuntimeException {
    private final HttpStatusCode statusCode;

    public GithubApiException(String message, HttpStatusCode statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
