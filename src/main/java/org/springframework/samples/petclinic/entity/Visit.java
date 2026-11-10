package org.springframework.samples.petclinic.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * visits 테이블: 진료 기록.
 */
@Entity
@Table(name = "visits")
public class Visit extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "pet_id", nullable = false)
	private Pet pet;

	@Column(name = "visit_date", nullable = false)
	private LocalDate date;

	@Column(nullable = false)
	private String description;

	@Column
	private String prescription;

	protected Visit() {
	}

	public Visit(Pet pet) {
		this.pet = pet;
	}

	public LocalDate getDate() {
		return this.date;
	}

	public String getDescription() {
		return this.description;
	}

	public String getPrescription() {
		return this.prescription;
	}

	public void update(LocalDate date, String description, String prescription) {
		this.date = date;
		this.description = description;
		this.prescription = prescription;
	}

}
