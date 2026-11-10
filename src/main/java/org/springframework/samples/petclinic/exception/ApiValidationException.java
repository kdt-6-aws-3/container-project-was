package org.springframework.samples.petclinic.exception;

import java.util.List;

/**
 * DB 조회가 필요한 검증(존재하지 않는 id, 예약 시간 중복 등)에 실패했을 때 던진다. 400으로 응답한다.
 */
public class ApiValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final List<FieldError> errors;

	public ApiValidationException(List<? extends FieldError> errors) {
		super("Validation failed: " + errors);
		this.errors = List.copyOf(errors);
	}

	public ApiValidationException(String field, String code, String message) {
		this(List.of(new FieldError(field, code, message)));
	}

	public List<FieldError> getErrors() {
		return this.errors;
	}

	/** 응답의 errors[] 한 항목 */
	public record FieldError(String field, String code, String message) {
	}

}
