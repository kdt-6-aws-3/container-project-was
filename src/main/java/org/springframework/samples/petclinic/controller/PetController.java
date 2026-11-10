package org.springframework.samples.petclinic.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.dto.PetDto;
import org.springframework.samples.petclinic.service.PetService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/** 반려동물, 반려동물의 진료 기록, 반려동물 종류 */
@RestController
@RequestMapping("/api")
class PetController {

	private static final String PETS = "/owners/{ownerId}/pets";

	private static final String VISITS = PETS + "/{petId}/visits";

	private final PetService petService;

	PetController(PetService petService) {
		this.petService = petService;
	}

	@GetMapping("/pettypes")
	List<PetDto.TypeResponse> listPetTypes() {
		return this.petService.listPetTypes();
	}

	@GetMapping(PETS + "/{petId}")
	PetDto.Response showPet(@PathVariable int ownerId, @PathVariable int petId) {
		return this.petService.getPet(ownerId, petId);
	}

	@PostMapping(PETS)
	ResponseEntity<PetDto.Response> createPet(@PathVariable int ownerId, @Valid @RequestBody PetDto.Request request) {
		PetDto.Response created = this.petService.createPet(ownerId, request);
		return ResponseEntity.status(HttpStatus.CREATED)
			.header(HttpHeaders.LOCATION, "/api/owners/" + ownerId + "/pets/" + created.id())
			.body(created);
	}

	@PutMapping(PETS + "/{petId}")
	PetDto.Response updatePet(@PathVariable int ownerId, @PathVariable int petId,
			@Valid @RequestBody PetDto.Request request) {
		return this.petService.updatePet(ownerId, petId, request);
	}

	@DeleteMapping(PETS + "/{petId}")
	ResponseEntity<Void> deletePet(@PathVariable int ownerId, @PathVariable int petId) {
		this.petService.deletePet(ownerId, petId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping(VISITS)
	List<PetDto.VisitResponse> listVisits(@PathVariable int ownerId, @PathVariable int petId) {
		return this.petService.listVisits(ownerId, petId);
	}

	@PostMapping(VISITS)
	ResponseEntity<PetDto.VisitResponse> createVisit(@PathVariable int ownerId, @PathVariable int petId,
			@Valid @RequestBody PetDto.VisitRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(this.petService.createVisit(ownerId, petId, request));
	}

	@PutMapping(VISITS + "/{visitId}")
	PetDto.VisitResponse updateVisit(@PathVariable int ownerId, @PathVariable int petId, @PathVariable int visitId,
			@Valid @RequestBody PetDto.VisitRequest request) {
		return this.petService.updateVisit(ownerId, petId, visitId, request);
	}

	@DeleteMapping(VISITS + "/{visitId}")
	ResponseEntity<Void> deleteVisit(@PathVariable int ownerId, @PathVariable int petId, @PathVariable int visitId) {
		this.petService.deleteVisit(ownerId, petId, visitId);
		return ResponseEntity.noContent().build();
	}

}
