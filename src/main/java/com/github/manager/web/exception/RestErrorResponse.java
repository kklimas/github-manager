package com.github.manager.web.exception;

public record RestErrorResponse(int status, String error) {
}
