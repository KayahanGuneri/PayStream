// Centralized exception-to-HTTP mapping using RFC7807 Problem+JSON.
package com.paystream.transferservice.api;

import com.paystream.transferservice.domain.DomainValidationException;
import com.paystream.transferservice.domain.IdempotencyConflictException;
import com.paystream.transferservice.domain.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestControllerAdvice(basePackages = "com.paystream.transferservice")
public class GlobalExceptionHandler {

    // ---------- Public exception handlers (ordered by specificity) ----------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> onBodyValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest req) {
        String msg = firstFieldError(ex)
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .orElse("Validation error");
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation Error", msg, req,
                "https://docs.paystream.dev/problems/validation");
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> onBind(BindException ex, HttpServletRequest req) {
        String msg = ex.getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .orElse("Validation error");
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation Error", msg, req,
                "https://docs.paystream.dev/problems/validation");
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> onConstraint(ConstraintViolationException ex,
                                                      HttpServletRequest req) {
        // Pick first violation for a concise message
        String msg = ex.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("Constraint violation");
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Constraint Violation", msg, req,
                "https://docs.paystream.dev/problems/validation");
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ProblemDetail> onDomainValidation(DomainValidationException ex,
                                                            HttpServletRequest req) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req,
                "https://docs.paystream.dev/problems/bad-request");
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ProblemDetail> onIdempotency(IdempotencyConflictException ex,
                                                       HttpServletRequest req) {
        ProblemDetail pd = problem(HttpStatus.CONFLICT, "Idempotency Conflict", ex.getMessage(), req,
                "https://docs.paystream.dev/problems/idempotency-conflict");
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> onNotFound(NotFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = problem(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req,
                "https://docs.paystream.dev/problems/not-found");
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> onIllegalArg(IllegalArgumentException ex,
                                                      HttpServletRequest req) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req,
                "https://docs.paystream.dev/problems/bad-request");
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> onUnexpected(Exception ex, HttpServletRequest req) {
        // NOTE: For unexpected failures, log with a unique errorId for support investigations.
        String errorId = UUID.randomUUID().toString();
        log.error("UNEXPECTED errorId={} {} {}", errorId, req.getMethod(), req.getRequestURI(), ex);

        ProblemDetail pd = problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error",
                "Unexpected error. Provide errorId=" + errorId + " to support.", req,
                "https://docs.paystream.dev/problems/internal");
        pd.setProperty("errorId", errorId);
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    // ---------- Helpers ----------

    /** Creates a ProblemDetail with common properties and trace/context metadata. */
    private ProblemDetail problem(HttpStatus status,
                                  String title,
                                  String detail,
                                  HttpServletRequest req,
                                  String typeUrl) {
        // Use the servlet path as "instance"; add a synthetic traceId (or fetch from MDC/OTel)
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle(title);
        pd.setDetail(detail);
        pd.setType(URI.create(typeUrl));
        pd.setProperty("instance", req.getRequestURI());
        pd.setProperty("method", req.getMethod());
        pd.setProperty("traceId", currentTraceId());
        return pd;
    }

    /** Returns a "traceId". If you use OpenTelemetry/SLF4J MDC, fetch the real one here. */
    private String currentTraceId() {
        // TODO: integrate with tracing context (e.g., MDC.get("traceId"))
        return UUID.randomUUID().toString();
    }

    private Optional<FieldError> firstFieldError(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream().findFirst();
    }
}
