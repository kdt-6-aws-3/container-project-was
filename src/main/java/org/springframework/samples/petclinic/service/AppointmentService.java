package org.springframework.samples.petclinic.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.samples.petclinic.dto.AppointmentDto;
import org.springframework.samples.petclinic.dto.PageResponse;
import org.springframework.samples.petclinic.entity.Appointment;
import org.springframework.samples.petclinic.entity.AppointmentStatus;
import org.springframework.samples.petclinic.entity.Hospital;
import org.springframework.samples.petclinic.entity.Pet;
import org.springframework.samples.petclinic.entity.Vet;
import org.springframework.samples.petclinic.exception.ApiValidationException;
import org.springframework.samples.petclinic.exception.ApiValidationException.FieldError;
import org.springframework.samples.petclinic.exception.ResourceNotFoundException;
import org.springframework.samples.petclinic.repository.AppointmentRepository;
import org.springframework.samples.petclinic.repository.HospitalRepository;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 진료 예약. 예약은 30분 단위 슬롯이며, 같은 시각에 수의사·반려동물이 겹칠 수 없다(취소된 예약 제외).
 */
@Service
@Transactional(readOnly = true)
public class AppointmentService {

	private static final int SLOT_MINUTES = 30;

	private static final String NOT_FOUND = "찾을 수 없습니다";

	private final AppointmentRepository appointments;

	private final HospitalRepository hospitals;

	private final PetRepository pets;

	private final VetRepository vets;

	public AppointmentService(AppointmentRepository appointments, HospitalRepository hospitals, PetRepository pets,
			VetRepository vets) {
		this.appointments = appointments;
		this.hospitals = hospitals;
		this.pets = pets;
		this.vets = vets;
	}

	public PageResponse<AppointmentDto.Response> findAppointments(Integer hospitalId, Integer vetId, Integer petId,
			AppointmentStatus status, int page, int size) {
		return PageResponse.from(this.appointments.search(hospitalId, vetId, petId, status,
				PageResponse.request(page, size, Sort.unsorted())), AppointmentDto.Response::from);
	}

	/** 수의사의 하루 일정 */
	public List<AppointmentDto.Response> getVetSchedule(int vetId, LocalDate date) {
		if (!this.vets.existsById(vetId)) {
			throw new ResourceNotFoundException("Vet " + vetId + " not found");
		}
		return this.appointments.findVetSchedule(vetId, date.atStartOfDay(), date.plusDays(1).atStartOfDay())
			.stream()
			.map(AppointmentDto.Response::from)
			.toList();
	}

	public AppointmentDto.Response getAppointment(int appointmentId) {
		return AppointmentDto.Response.from(findAppointment(appointmentId));
	}

	@Transactional
	public AppointmentDto.Response createAppointment(AppointmentDto.Request request) {
		Appointment appointment = new Appointment();
		applyAndValidate(request, appointment);
		return AppointmentDto.Response.from(this.appointments.save(appointment));
	}

	@Transactional
	public AppointmentDto.Response updateAppointment(int appointmentId, AppointmentDto.Request request) {
		Appointment appointment = findAppointment(appointmentId);
		throwIfClosed(appointment);
		applyAndValidate(request, appointment);
		return AppointmentDto.Response.from(appointment);
	}

	@Transactional
	public AppointmentDto.Response changeStatus(int appointmentId, AppointmentStatus status) {
		Appointment appointment = findAppointment(appointmentId);
		throwIfClosed(appointment);
		appointment.changeStatus(status);
		return AppointmentDto.Response.from(appointment);
	}

	/** 부하 테스트 뒤 예약 데이터 초기화용. 지운 건수를 돌려준다. */
	@Transactional
	public int deleteAllAppointments() {
		return this.appointments.deleteAllAppointments();
	}

	@Transactional
	public void deleteAppointment(int appointmentId) {
		this.appointments.delete(findAppointment(appointmentId));
	}

	/** 요청을 검사하고, 문제가 없을 때만 예약에 반영한다. 문제는 한 번에 모아서 400으로 알린다. */
	private void applyAndValidate(AppointmentDto.Request request, Appointment appointment) {
		List<FieldError> errors = new ArrayList<>();
		Hospital hospital = this.hospitals.findById(request.hospitalId()).orElse(null);
		Pet pet = this.pets.findById(request.petId()).orElse(null);
		Vet vet = this.vets.findById(request.vetId()).orElse(null);
		if (hospital == null) {
			errors.add(new FieldError("hospitalId", "notFound", NOT_FOUND));
		}
		if (pet == null) {
			errors.add(new FieldError("petId", "notFound", NOT_FOUND));
		}
		if (vet == null) {
			errors.add(new FieldError("vetId", "notFound", NOT_FOUND));
		}
		else if (hospital != null && !hospital.getId().equals(vet.getHospital().getId())) {
			errors.add(new FieldError("vetId", "appointment.vetNotInHospital", "담당 수의사가 예약 병원 소속이 아닙니다"));
		}

		LocalDateTime at = request.appointmentAt();
		if (!at.plusMinutes(SLOT_MINUTES).isAfter(LocalDateTime.now())) {
			errors.add(new FieldError("appointmentAt", "appointment.slotPassed", "이미 지난 예약 시간입니다"));
		}
		if (at.getMinute() % SLOT_MINUTES != 0 || at.getSecond() != 0 || at.getNano() != 0) {
			errors.add(new FieldError("appointmentAt", "appointment.notOnSlot", "예약은 정각 또는 30분에만 가능합니다"));
		}
		else {
			if (vet != null && this.appointments.isVetBooked(vet.getId(), at, appointment.getId())) {
				errors.add(new FieldError("appointmentAt", "appointment.vetTimeTaken", "담당 수의사가 이 시간에 이미 예약이 있습니다"));
			}
			if (pet != null && this.appointments.isPetBooked(pet.getId(), at, appointment.getId())) {
				errors.add(new FieldError("appointmentAt", "appointment.petTimeTaken", "반려동물이 이 시간에 이미 예약이 있습니다"));
			}
		}

		if (!errors.isEmpty()) {
			throw new ApiValidationException(errors);
		}
		appointment.update(hospital, pet, vet, at, request.reason());
	}

	private static void throwIfClosed(Appointment appointment) {
		if (appointment.getStatus().isClosed()) {
			throw new ApiValidationException("status", "appointment.closed", "취소되었거나 완료된 예약은 변경할 수 없습니다");
		}
	}

	private Appointment findAppointment(int appointmentId) {
		return this.appointments.findById(appointmentId)
			.orElseThrow(() -> new ResourceNotFoundException("Appointment " + appointmentId + " not found"));
	}

}
