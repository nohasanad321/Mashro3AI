package com.mashro3ai.shared.error;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, "Request validation failed");
        problem.setTitle("Invalid request");
        problem.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
            .map(error -> new FieldError(error.getField(), error.getDefaultMessage())).toList());
        return handleExceptionInternal(ex, problem, headers, status, request);
    }
    @org.springframework.web.bind.annotation.ExceptionHandler({
        org.springframework.dao.OptimisticLockingFailureException.class,
        org.springframework.dao.DataIntegrityViolationException.class})
    public ResponseEntity<ProblemDetail> conflict(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, "The draft changed concurrently. Reload it and retry."));
    }
    public record FieldError(String field, String message) {}
}
