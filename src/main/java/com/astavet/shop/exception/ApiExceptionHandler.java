package com.astavet.shop.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);
    @ExceptionHandler(ShopException.class)
    public ResponseEntity<Map<String, String>> business(ShopException exception) { return error(exception.status(), exception.getMessage()); }
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class, ConstraintViolationException.class})
    public ResponseEntity<Map<String, String>> invalid(Exception exception) { return error(400, "Please check the submitted fields."); }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> forbidden(AccessDeniedException exception) { return error(403, "Access denied."); }
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> database(DataAccessException exception) {
        LOG.atWarn().addKeyValue("errorType", exception.getClass().getSimpleName()).log("Database operation failed");
        return error(503, "The order service is busy. Retry with the same checkout details.");
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> unsupportedMethod(HttpRequestMethodNotSupportedException exception) {
        return protocolError(exception, "This request method is not supported.");
    }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, String>> unsupportedContentType(HttpMediaTypeNotSupportedException exception) {
        return protocolError(exception, "This content type is not supported.");
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> unexpected(Exception exception) {
        LOG.atError().addKeyValue("errorType", exception.getClass().getSimpleName()).log("Unexpected request failure");
        return error(500, "Something went wrong. Please try again.");
    }
    private ResponseEntity<Map<String, String>> protocolError(ErrorResponse exception, String message) {
        return ResponseEntity.status(exception.getStatusCode()).headers(exception.getHeaders()).body(Map.of("message", message));
    }
    private ResponseEntity<Map<String, String>> error(int status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
