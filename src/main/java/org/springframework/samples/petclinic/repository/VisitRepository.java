package org.springframework.samples.petclinic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.entity.Visit;

public interface VisitRepository extends JpaRepository<Visit, Integer> {

	Optional<Visit> findByIdAndPetId(int id, int petId);

}
