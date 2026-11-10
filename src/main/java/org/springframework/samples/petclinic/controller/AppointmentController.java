package org.springframework.samples.petclinic.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.dto.AppointmentDto;
import org.springframework.samples.petclinic.dto.PageResponse;
import org.springframework.samples.petclinic.entity.AppointmentStatus;
import org.springframework.samples.petclinic.service.AppointmentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/** 진료 예약과 수의사 일정 */
@RestController
@RequestMapping("/api")
class AppointmentController {

	private final AppointmentService appointmentService;

	AppointmentController(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	@GetMapping("/appointments")
	PageResponse<AppointmentDto.Response> findAppointments(@RequestParam(required = false) @Nullable Integer hospitalId,
			@RequestParam(required = false) @Nullable Integer vetId,
			@RequestParam(required = false) @Nullable Integer petId,
			@RequestParam(required = false) @Nullable AppointmentStatus status,
			@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
		return this.appointmentService.findAppointments(hospitalId, vetId, petId, status, page, size);
	}

	@GetMapping("/vets/{vetId}/appointments")
	List<AppointmentDto.Response> showVetSchedule(@PathVariable int vetId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return this.appointmentService.getVetSchedule(vetId, date);
	}

	@GetMapping("/appointments/{appointmentId}")
	AppointmentDto.Response showAppointment(@PathVariable int appointmentId) {
		return this.appointmentService.getAppointment(appointmentId);
	}

	@PostMapping("/appointments")
	ResponseEntity<AppointmentDto.Response> createAppointment(@Valid @RequestBody AppointmentDto.Request request) {
		AppointmentDto.Response created = this.appointmentService.createAppointment(request);
		return ResponseEntity.status(HttpStatus.CREATED)
			.header(HttpHeaders.LOCATION, "/api/appointments/" + created.id())
			.body(created);
	}

	@PutMapping("/appointments/{appointmentId}")
	AppointmentDto.Response updateAppointment(@PathVariable int appointmentId,
			@Valid @RequestBody AppointmentDto.Request request) {
		return this.appointmentService.updateAppointment(appointmentId, request);
	}

	@PatchMapping("/appointments/{appointmentId}/status")
	AppointmentDto.Response changeStatus(@PathVariable int appointmentId,
			@Valid @RequestBody AppointmentDto.StatusRequest request) {
		return this.appointmentService.changeStatus(appointmentId, request.status());
	}

	/** 진료 예약 전체 삭제 (부하 테스트 정리용) */
	@DeleteMapping("/appointments")
	Map<String, Integer> deleteAllAppointments() {
		return Map.of("deleted", this.appointmentService.deleteAllAppointments());
	}

	@DeleteMapping("/appointments/{appointmentId}")
	ResponseEntity<Void> deleteAppointment(@PathVariable int appointmentId) {
		this.appointmentService.deleteAppointment(appointmentId);
		return ResponseEntity.noContent().build();
	}

}
