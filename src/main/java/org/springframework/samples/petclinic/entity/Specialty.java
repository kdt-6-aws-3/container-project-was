package org.springframework.samples.petclinic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * specialties 테이블: 수의사 전문 분야.
 */
@Entity
@Table(name = "specialties")
public class Specialty extends BaseEntity {

	@Column(name = "specialty_name", nullable = false)
	private String name;

	public String getName() {
		return this.name;
	}

}
