package org.springframework.samples.petclinic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * types 테이블: 반려동물 종류(개, 고양이 등).
 */
@Entity
@Table(name = "types")
public class PetType extends BaseEntity {

	@Column(name = "type_name", nullable = false)
	private String name;

	public String getName() {
		return this.name;
	}

}
