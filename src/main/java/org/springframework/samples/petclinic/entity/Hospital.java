package org.springframework.samples.petclinic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * hospitals 테이블: 동물병원.
 */
@Entity
@Table(name = "hospitals")
public class Hospital extends BaseEntity {

	@Column(name = "hospital_name", nullable = false)
	private String name;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false, unique = true, length = 50)
	private String phone;

	public String getName() {
		return this.name;
	}

	public String getAddress() {
		return this.address;
	}

	public String getPhone() {
		return this.phone;
	}

}
