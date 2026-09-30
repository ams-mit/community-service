package kln.ams.community.exception;

import kln.ams.community.dto.ApiErrorResponse;
import kln.ams.community.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String requestId = SecurityUtils.getCurrentRequestId();
        List<Map<String, String>> fieldErrors = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> err = new LinkedHashMap<>();
            err.put("field", fe.getField());
            err.put("message", fe.getDefaultMessage());
            fieldErrors.add(err);
        }

        ApiErrorResponse response = ApiErrorResponse.of(
                "Validation failed",
                "VALIDATION_ERROR",
                fieldErrors,
                requestId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        String requestId = SecurityUtils.getCurrentRequestId();
        ApiErrorResponse response = ApiErrorResponse.of(
                ex.getMessage(),
                ex.getErrorCode() != null ? ex.getErrorCode() : "RESOURCE_NOT_FOUND",
                null,
                requestId
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        String requestId = SecurityUtils.getCurrentRequestId();
        ApiErrorResponse response = ApiErrorResponse.of(
                ex.getMessage(),
                "RESOURCE_NOT_FOUND",
                null,
                requestId
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRule(BusinessRuleViolationException ex) {
        String requestId = SecurityUtils.getCurrentRequestId();
        ApiErrorResponse response = ApiErrorResponse.of(
                ex.getMessage(),
                ex.getErrorCode() != null ? ex.getErrorCode() : "BUSINESS_RULE_VIOLATION",
                null,
                requestId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ApiErrorResponse> handleStatusTransition(InvalidStatusTransitionException ex) {
        String requestId = SecurityUtils.getCurrentRequestId();
        ApiErrorResponse response = ApiErrorResponse.of(
                ex.getMessage(),
                ex.getErrorCode() != null ? ex.getErrorCode() : "STATUS_TRANSITION_NOT_ALLOWED",
                null,
                requestId
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateResource(DuplicateResourceException ex) {
        String requestId = SecurityUtils.getCurrentRequestId();
        ApiErrorResponse response = ApiErrorResponse.of(
                ex.getMessage(),
                ex.getErrorCode() != null ? ex.getErrorCode() : "DUPLICATE_RESOURCE",
                null,
                requestId
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        String requestId = SecurityUtils.getCurrentRequestId();
        ApiErrorResponse response = ApiErrorResponse.of(
                ex.getMessage(),
                "UNAUTHORIZED",
                null,
                requestId
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(ForbiddenException ex) {
        String requestId = SecurityUtils.getCurrentRequestId();
        ApiErrorResponse response = ApiErrorResponse.of(
                ex.getMessage(),
                "FORBIDDEN",
                null,
                requestId
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(DependencyUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleDependencyUnavailable(DependencyUnavailableException ex) {
        String requestId = SecurityUtils.getCurrentRequestId();
        ApiErrorResponse response = ApiErrorResponse.of(
                ex.getMessage(),
                "DEPENDENCY_UNAVAILABLE",
                null,
                requestId
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        String requestId = SecurityUtils.getCurrentRequestId();
        ApiErrorResponse response = ApiErrorResponse.of(
                ex.getMessage(),
                "VALIDATION_ERROR",
                null,
                requestId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex) {
        String requestId = SecurityUtils.getCurrentRequestId();
        ApiErrorResponse response = ApiErrorResponse.of(
                ex.getMessage(),
                "BUSINESS_RULE_VIOLATION",
                null,
                requestId
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        String requestId = SecurityUtils.getCurrentRequestId();
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String reason = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        ApiErrorResponse response = ApiErrorResponse.of(
                reason,
                status.name(),
                null,
                requestId
        );
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        String requestId = SecurityUtils.getCurrentRequestId();
        log.error("Unhandled exception processing request {}: {}", requestId, ex.getMessage(), ex);
        ApiErrorResponse response = ApiErrorResponse.of(
                "An unexpected internal error occurred",
                "INTERNAL_SERVER_ERROR",
                null,
                requestId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
