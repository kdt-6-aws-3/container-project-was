package org.springframework.samples.petclinic.dto;

import java.time.LocalDateTime;

import org.springframework.samples.petclinic.entity.Appointment;
import org.springframework.samples.petclinic.entity.AppointmentStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 진료 예약 API의 요청·응답 형식.
 */
public final class AppointmentDto {

	private AppointmentDto() {
	}

	/** 등록·수정 요청 */
	public record Request(@NotNull Integer hospitalId, @NotNull Integer petId, @NotNull Integer vetId,
			@NotNull LocalDateTime appointmentAt, @Size(max = 255) String reason) {
	}

	/** 상태 변경 요청 */
	public record StatusRequest(@NotNull AppointmentStatus status) {
	}

	/** 병원·반려동물·수의사는 id와 이름만 펼쳐서 보낸다 */
	public record Response(Integer id, Integer hospitalId, String hospitalName, Integer petId, String petName,
			Integer vetId, String vetName, LocalDateTime appointmentAt, AppointmentStatus status, String reason,
			LocalDateTime createdAt) {

		public static Response from(Appointment appointment) {
			return new Response(appointment.getId(), appointment.getHospital().getId(),
					appointment.getHospital().getName(), appointment.getPet().getId(), appointment.getPet().getName(),
					appointment.getVet().getId(), appointment.getVet().getName(), appointment.getAppointmentAt(),
					appointment.getStatus(), appointment.getReason(), appointment.getCreatedAt());
		}

	}

}
