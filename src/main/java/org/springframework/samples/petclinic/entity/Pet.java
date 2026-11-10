package org.springframework.samples.petclinic.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * pets 테이블: 반려동물. 반려동물을 지우면 진료 기록도 함께 지워진다.
 */
@Entity
@Table(name = "pets")
public class Pet extends BaseEntity {

	@Column(name = "pet_name", nullable = false)
	private String name;

	@Column(nullable = false)
	private LocalDate birthDate;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "type_id", nullable = false)
	private PetType type;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private Owner owner;

	@OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("date, id")
	private List<Visit> visits = new ArrayList<>();

	protected Pet() {
	}

	public Pet(Owner owner) {
		this.owner = owner;
	}

	public String getName() {
		return this.name;
	}

	public LocalDate getBirthDate() {
		return this.birthDate;
	}

	public PetType getType() {
		return this.type;
	}

	public Owner getOwner() {
		return this.owner;
	}

	public List<Visit> getVisits() {
		return this.visits;
	}

	public void update(String name, LocalDate birthDate, PetType type) {
		this.name = name;
		this.birthDate = birthDate;
		this.type = type;
	}

}
