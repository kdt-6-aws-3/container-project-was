package org.springframework.samples.petclinic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.entity.Pet;

public interface PetRepository extends JpaRepository<Pet, Integer> {

	Optional<Pet> findByIdAndOwnerId(int id, int ownerId);

}
