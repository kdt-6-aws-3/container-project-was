package org.springframework.samples.petclinic.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * owners 테이블: 보호자. 보호자를 지우면 반려동물과 진료 기록도 함께 지워진다.
 */
@Entity
@Table(name = "owners")
public class Owner extends BaseEntity {

	@Column(name = "owner_name", nullable = false)
	private String name;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false, length = 50)
	private String phone;

	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("name, id")
	private List<Pet> pets = new ArrayList<>();

	public String getName() {
		return this.name;
	}

	public String getAddress() {
		return this.address;
	}

	public String getPhone() {
		return this.phone;
	}

	public List<Pet> getPets() {
		return this.pets;
	}

	public void update(String name, String address, String phone) {
		this.name = name;
		this.address = address;
		this.phone = phone;
	}

}
