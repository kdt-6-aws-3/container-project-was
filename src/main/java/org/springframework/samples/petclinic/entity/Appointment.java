package org.springframework.samples.petclinic.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * appointments 테이블: 진료 예약. 병원·수의사·반려동물별로 시간순 조회가 많아 인덱스를 둔다.
 */
@Entity
@Table(name = "appointments", indexes = {
		@Index(name = "idx_appointments_hospital_time", columnList = "hospital_id, appointment_at"),
		@Index(name = "idx_appointments_vet_time", columnList = "vet_id, appointment_at"),
		@Index(name = "idx_appointments_pet_time", columnList = "pet_id, appointment_at") })
public class Appointment extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "hospital_id", nullable = false)
	private Hospital hospital;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "pet_id", nullable = false)
	private Pet pet;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "vet_id", nullable = false)
	private Vet vet;

	@Column(nullable = false)
	private LocalDateTime appointmentAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AppointmentStatus status = AppointmentStatus.REQUESTED;

	@Column
	private String reason;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	public Hospital getHospital() {
		return this.hospital;
	}

	public Pet getPet() {
		return this.pet;
	}

	public Vet getVet() {
		return this.vet;
	}

	public LocalDateTime getAppointmentAt() {
		return this.appointmentAt;
	}

	public AppointmentStatus getStatus() {
		return this.status;
	}

	public String getReason() {
		return this.reason;
	}

	public LocalDateTime getCreatedAt() {
		return this.createdAt;
	}

	public void update(Hospital hospital, Pet pet, Vet vet, LocalDateTime appointmentAt, String reason) {
		this.hospital = hospital;
		this.pet = pet;
		this.vet = vet;
		this.appointmentAt = appointmentAt;
		this.reason = reason;
	}

	public void changeStatus(AppointmentStatus status) {
		this.status = status;
	}

}
