package org.springframework.samples.petclinic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.entity.Vet;

public interface VetRepository extends JpaRepository<Vet, Integer> {

	List<Vet> findByHospitalIdOrderByIdAsc(int hospitalId);

}
