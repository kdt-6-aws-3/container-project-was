package org.springframework.samples.petclinic.controller;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/client-errors")
class ClientErrorController {

	private static final Logger logger = LoggerFactory.getLogger(ClientErrorController.class);

	private static final int MAX_MESSAGE_LENGTH = 2000;

	private static final int MAX_STACK_LENGTH = 4000;

	@PostMapping
	ResponseEntity<Void> report(@Valid @RequestBody ClientErrorReport report) {
		logger.warn("browser-error level={} message={} url={} stack={}",
				report.level(),
				truncate(report.message(), MAX_MESSAGE_LENGTH),
				report.url(),
				truncate(report.stack() != null ? report.stack() : "", MAX_STACK_LENGTH));
		return ResponseEntity.noContent().build();
	}

	private static String truncate(String value, int max) {
		if (value == null) {
			return "";
		}
		return value.length() <= max ? value : value.substring(0, max) + "…";
	}

	record ClientErrorReport(
		@NotBlank @Size(max = 20) String level,
		@NotBlank @Size(max = 2000) String message,
		@Size(max = 500) String url,
		@Size(max = 4000) String stack,
		Instant timestamp
	) {}

}
