package org.springframework.samples.petclinic.exception;

/**
 * 경로의 id에 해당하는 데이터가 없을 때 던진다. 404로 응답한다.
 */
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String message) {
		super(message);
	}

}
