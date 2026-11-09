package org.springframework.samples.petclinic.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.data.domain.PageRequest;
import org.springframework.samples.petclinic.dto.AppointmentDetails;
import org.springframework.samples.petclinic.dto.AppointmentRequest;
import org.springframework.samples.petclinic.dto.PageResponse;
import org.springframework.samples.petclinic.entity.Appointment;
import org.springframework.samples.petclinic.entity.AppointmentStatus;
import org.springframework.samples.petclinic.exception.ApiValidationException;
import org.springframework.samples.petclinic.exception.ResourceNotFoundException;
import org.springframework.samples.petclinic.repository.AppointmentRepository;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

@Service
public class AppointmentService {

	private static final int MAX_PAGE_SIZE = 100;

	private static final Map<String, String> FIELD_ALIASES = Map.of("hospital", "hospitalId", "pet", "petId", "vet",
			"vetId");

	private final AppointmentRepository appointments;

	private final VetRepository vets;

	private final AppointmentValidator validator;

	public AppointmentService(AppointmentRepository appointments, VetRepository vets,
			AppointmentValidator validator) {
		this.appointments = appointments;
		this.vets = vets;
		this.validator = validator;
	}

	@Transactional(readOnly = true)
	public PageResponse<AppointmentDetails> findAppointments(Integer hospitalId, Integer vetId, Integer petId,
			AppointmentStatus status, int page, int size) {
		int validatedPage = Math.max(page, 1);
		int validatedSize = Math.clamp(size, 1, MAX_PAGE_SIZE);
		return PageResponse.from(this.appointments.search(hospitalId, vetId, petId, status,
				PageRequest.of(validatedPage - 1, validatedSize)), Function.identity());
	}

	@Transactional(readOnly = true)
	public List<AppointmentDetails> getVetSchedule(int vetId, LocalDate date) {
		if (this.vets.findById(vetId).isEmpty()) {
			throw new ResourceNotFoundException("Vet " + vetId + " not found");
		}
		return this.appointments.findVetSchedule(vetId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
	}

	@Transactional(readOnly = true)
	public AppointmentDetails getAppointment(int appointmentId) {
		return AppointmentDetails.from(findAppointment(appointmentId));
	}

	@Transactional
	public AppointmentDetails createAppointment(AppointmentRequest request) {
		Appointment appointment = new Appointment();
		throwIfInvalid(this.validator.applyAndValidate(request, appointment));
		appointment.setStatus(AppointmentStatus.REQUESTED);
		return AppointmentDetails.from(this.appointments.save(appointment));
	}

	@Transactional
	public AppointmentDetails updateAppointment(int appointmentId, AppointmentRequest request) {
		Appointment appointment = findAppointment(appointmentId);
		throwIfClosed(appointment);
		throwIfInvalid(this.validator.applyAndValidate(request, appointment));
		return AppointmentDetails.from(this.appointments.save(appointment));
	}

	@Transactional
	public AppointmentDetails changeStatus(int appointmentId, AppointmentStatus status) {
		Appointment appointment = findAppointment(appointmentId);
		throwIfClosed(appointment);
		appointment.setStatus(status);
		return AppointmentDetails.from(this.appointments.save(appointment));
	}

	@Transactional
	public void deleteAppointment(int appointmentId) {
		this.appointments.delete(findAppointment(appointmentId));
	}

	@Transactional
	public int deleteAllAppointments() {
		return this.appointments.deleteAllAppointments();
	}

	private static void throwIfClosed(Appointment appointment) {
		if (appointment.getStatus().isClosed()) {
			Errors errors = new BeanPropertyBindingResult(appointment, "appointment");
			errors.rejectValue("status", "appointment.closed");
			throw new ApiValidationException(errors);
		}
	}

	private static void throwIfInvalid(Errors errors) {
		if (errors.hasErrors()) {
			throw new ApiValidationException(errors, FIELD_ALIASES);
		}
	}

	private Appointment findAppointment(int appointmentId) {
		return this.appointments.findById(appointmentId)
			.orElseThrow(() -> new ResourceNotFoundException("Appointment " + appointmentId + " not found"));
	}

}
