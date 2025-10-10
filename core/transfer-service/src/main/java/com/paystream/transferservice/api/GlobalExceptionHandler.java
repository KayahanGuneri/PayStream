

// Centralized exception-to-HTTP mapping using RFC7807 Problem+JSON.


package com.paystream.transferservice.api;

import com.paystream.transferservice.domain.DomainValidationException;
import com.paystream.transferservice.domain.IdempotencyConflictException;
import com.paystream.transferservice.domain.NotFoundException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


@RestControllerAdvice


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestControllerAdvice(basePackages = "com.paystream.transferservice")




public class GlobalExceptionHandler {

    // ---- Body validation
    @ExceptionHandler(MethodArgumentNotValidException.class)

    public ResponseEntity<ErrorResponse> handleBodyValidation(MethodArgumentNotValidException ex) {
        var msg = ex.getBindingResult()
                .getFieldErrors()
                .stream()



    public ResponseEntity<ProblemDetail> onBodyValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = firstFieldError(ex).map(fe -> fe.getField() + " " + fe.getDefaultMessage())

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
        String msg = ex.getFieldErrors().stream().findFirst()

        String msg = ex.getFieldErrors().stream()

                .findFirst()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .orElse("Validation error");
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation Error", msg, req,
                "https://docs.paystream.dev/problems/validation");
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(BindException ex) {
        var msg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("Binding error");
        return ResponseEntity.badRequest().body(new ErrorResponse("BIND_ERROR", msg));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(BindException ex) {
        var msg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("Binding error");
        return ResponseEntity.badRequest().body(new ErrorResponse("BIND_ERROR", msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> onConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        String msg = ex.getConstraintViolations().stream().findFirst()
                .map(ConstraintViolation::getMessage).orElse("Constraint violation");

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


    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        String msg = "Missing header: " + ex.getHeaderName();
        return ResponseEntity.badRequest().body(new ErrorResponse("MISSING_HEADER", msg));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        String msg = "Missing header: " + ex.getHeaderName();
        return ResponseEntity.badRequest().body(new ErrorResponse("MISSING_HEADER", msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse("BAD_REQUEST", ex.getMessage()));

    // ---- Header / path / JSON parse

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ProblemDetail> onMissingHeader(MissingRequestHeaderException ex, HttpServletRequest req) {
        String header = ex.getHeaderName();
        String msg = (header != null ? header : "Required header") + " is required";
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Bad Request", msg, req,
                "https://docs.paystream.dev/problems/bad-request");
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> onPathTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String name = ex.getName();
        String msg = (ex.getRequiredType() != null && UUID.class.isAssignableFrom(ex.getRequiredType()))
                ? name + " must be a valid UUID"
                : "Invalid value for " + name;
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Bad Request", msg, req,
                "https://docs.paystream.dev/problems/bad-request");
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> onUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String msg = "Malformed request body";
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            String field = (ife.getPath() != null && !ife.getPath().isEmpty())
                    ? ife.getPath().get(0).getFieldName() : "field";
            if (ife.getTargetType() != null && UUID.class.isAssignableFrom(ife.getTargetType())) {
                msg = field + " must be a valid UUID";
            } else {
                msg = "Invalid value for " + field;
            }
        }
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Bad Request", msg, req,
                "https://docs.paystream.dev/problems/bad-request");
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    // ---- Domain
    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ProblemDetail> onDomainValidation(DomainValidationException ex, HttpServletRequest req) {
    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ProblemDetail> onDomainValidation(DomainValidationException ex,
                                                            HttpServletRequest req) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req,
                "https://docs.paystream.dev/problems/bad-request");
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ProblemDetail> onIdempotency(IdempotencyConflictException ex, HttpServletRequest req) {

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
    public ResponseEntity<ProblemDetail> onIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req,
                "https://docs.paystream.dev/problems/bad-request");
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    // ---- Fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> onUnexpected(Exception ex, HttpServletRequest req) {
        String errorId = UUID.randomUUID().toString();
        log.error("UNEXPECTED errorId={} {} {}", errorId, req.getMethod(), req.getRequestURI(), ex);

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

    // ---- Helpers
    private ProblemDetail problem(HttpStatus status, String title, String detail,
                                  HttpServletRequest req, String typeUrl) {

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
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("method", req.getMethod());
        pd.setProperty("traceId", UUID.randomUUID().toString());
        return pd;
    }

    private Optional<FieldError> firstFieldError(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream().findFirst();
    }

    @ExceptionHandler(org.springframework.web.client.RestClientResponseException.class)
    public ResponseEntity<ProblemDetail> onDownstreamStatus(RestClientResponseException ex, HttpServletRequest req) {
        // Ledger vb. downstream 4xx/5xx’lerini yansıtalım (gateway mantığı)
        HttpStatus status = HttpStatus.valueOf(ex.getRawStatusCode());
        String msg = "Downstream error (" + status.value() + "): " + ex.getStatusText();
        ProblemDetail pd = problem(status, "Downstream Error", msg, req,
                "https://docs.paystream.dev/problems/downstream");
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(org.springframework.web.client.ResourceAccessException.class)
    public ResponseEntity<ProblemDetail> onDownstreamIo(ResourceAccessException ex, HttpServletRequest req) {
        ProblemDetail pd = problem(HttpStatus.SERVICE_UNAVAILABLE, "Downstream Unavailable",
                "Could not reach downstream service", req,
                "https://docs.paystream.dev/problems/downstream-unavailable");
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> onDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        String msg = "Data conflict (possibly idempotency key already used)";
        ProblemDetail pd = problem(HttpStatus.CONFLICT, "Conflict", msg, req,
                "https://docs.paystream.dev/problems/conflict");
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }


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
