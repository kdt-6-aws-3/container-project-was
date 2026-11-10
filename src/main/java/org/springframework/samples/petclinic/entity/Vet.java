package org.springframework.samples.petclinic.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * vets 테이블: 수의사. 전문 분야는 vet_specialties 연결 테이블로 이어진다.
 */
@Entity
@Table(name = "vets")
public class Vet extends BaseEntity {

	@Column(name = "vet_name", nullable = false)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "hospital_id", nullable = false)
	private Hospital hospital;

	@ManyToMany
	@JoinTable(name = "vet_specialties", joinColumns = @JoinColumn(name = "vet_id"),
			inverseJoinColumns = @JoinColumn(name = "specialty_id"))
	@OrderBy("name")
	private Set<Specialty> specialties = new LinkedHashSet<>();

	public String getName() {
		return this.name;
	}

	public Hospital getHospital() {
		return this.hospital;
	}

	public Set<Specialty> getSpecialties() {
		return this.specialties;
	}

}
