package org.springframework.samples.petclinic.exception;

import java.util.Objects;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.exception.ApiValidationException.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 모든 오류를 RFC 9457 ProblemDetail(JSON)로 응답한다. 입력 오류는 errors[{field, code, message}]를 함께 보낸다.
 */
@NullMarked
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(ApiExceptionHandler.class));

	private static final String VALIDATION_FAILED = "Validation failed";

	/** @Valid 요청 본문 검증 실패 */
	@Override
	protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ProblemDetail problem = ex.getBody();
		problem.setDetail(VALIDATION_FAILED);
		problem.setProperty("errors", ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> new FieldError(error.getField(), error.getCode(), error.getDefaultMessage()))
			.toList());
		return handleExceptionInternal(ex, problem, headers, status, request);
	}

	@ExceptionHandler(ApiValidationException.class)
	ProblemDetail handleApiValidation(ApiValidationException ex) {
		logger.warn(ex.getMessage());
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, VALIDATION_FAILED);
		problem.setProperty("errors", ex.getErrors());
		return problem;
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	ProblemDetail handleNotFound(ResourceNotFoundException ex) {
		logger.warn("Resource not found: {}", ex.getMessage());
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail handleConflict(DataIntegrityViolationException ex) {
		logger.warn("Request rejected by a database constraint: {}", ex.getMostSpecificCause().getMessage());
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "The request conflicts with existing data");
	}

	@ExceptionHandler(Exception.class)
	ProblemDetail handleUnexpected(Exception ex) {
		logger.error("Unexpected error while handling an API request", ex);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
	}

}
