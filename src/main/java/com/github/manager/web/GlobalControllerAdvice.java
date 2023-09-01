package com.github.manager.web;

import com.github.manager.web.exception.RestErrorResponse;
import com.github.manager.web.exception.github.RequestLimitExceededException;
import com.github.manager.web.exception.github.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(RequestLimitExceededException.class)
    public ResponseEntity<RestErrorResponse> handleForbiddenException(RequestLimitExceededException ex) {
        return handleException(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<RestErrorResponse> handleNotFoundException(UserNotFoundException ex) {
        return handleException(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    private ResponseEntity<RestErrorResponse> handleException(HttpStatus responseStatus, String message) {
        var errorBody = new RestErrorResponse(responseStatus.value(), message);
        return ResponseEntity.status(errorBody.status()).body(errorBody);
    }
}
