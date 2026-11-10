package org.springframework.samples.petclinic.service;

import java.util.List;

import org.springframework.samples.petclinic.dto.PetDto;
import org.springframework.samples.petclinic.entity.Owner;
import org.springframework.samples.petclinic.entity.Pet;
import org.springframework.samples.petclinic.entity.PetType;
import org.springframework.samples.petclinic.entity.Visit;
import org.springframework.samples.petclinic.exception.ApiValidationException;
import org.springframework.samples.petclinic.exception.ResourceNotFoundException;
import org.springframework.samples.petclinic.repository.AppointmentRepository;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.PetTypeRepository;
import org.springframework.samples.petclinic.repository.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 반려동물, 반려동물의 진료 기록, 반려동물 종류.
 */
@Service
@Transactional(readOnly = true)
public class PetService {

	private final OwnerRepository owners;

	private final PetRepository pets;

	private final VisitRepository visits;

	private final PetTypeRepository types;

	private final AppointmentRepository appointments;

	public PetService(OwnerRepository owners, PetRepository pets, VisitRepository visits, PetTypeRepository types,
			AppointmentRepository appointments) {
		this.owners = owners;
		this.pets = pets;
		this.visits = visits;
		this.types = types;
		this.appointments = appointments;
	}

	public List<PetDto.TypeResponse> listPetTypes() {
		return this.types.findAllByOrderByNameAsc().stream().map(PetDto.TypeResponse::from).toList();
	}

	public PetDto.Response getPet(int ownerId, int petId) {
		return PetDto.Response.from(findPet(ownerId, petId));
	}

	@Transactional
	public PetDto.Response createPet(int ownerId, PetDto.Request request) {
		Owner owner = this.owners.findById(ownerId)
			.orElseThrow(() -> new ResourceNotFoundException("Owner " + ownerId + " not found"));
		Pet pet = new Pet(owner);
		pet.update(request.name(), request.birthDate(), findType(request.typeId()));
		return PetDto.Response.from(this.pets.save(pet));
	}

	@Transactional
	public PetDto.Response updatePet(int ownerId, int petId, PetDto.Request request) {
		Pet pet = findPet(ownerId, petId);
		pet.update(request.name(), request.birthDate(), findType(request.typeId()));
		return PetDto.Response.from(pet);
	}

	/** 예약을 먼저 지운 뒤 반려동물을 지운다. 진료 기록은 엔티티 cascade로 함께 지워진다. */
	@Transactional
	public void deletePet(int ownerId, int petId) {
		Pet pet = findPet(ownerId, petId);
		this.appointments.deleteByPetId(petId);
		this.pets.delete(pet);
	}

	public List<PetDto.VisitResponse> listVisits(int ownerId, int petId) {
		return findPet(ownerId, petId).getVisits().stream().map(PetDto.VisitResponse::from).toList();
	}

	@Transactional
	public PetDto.VisitResponse createVisit(int ownerId, int petId, PetDto.VisitRequest request) {
		Visit visit = new Visit(findPet(ownerId, petId));
		apply(request, visit);
		return PetDto.VisitResponse.from(this.visits.save(visit));
	}

	@Transactional
	public PetDto.VisitResponse updateVisit(int ownerId, int petId, int visitId, PetDto.VisitRequest request) {
		Visit visit = findVisit(ownerId, petId, visitId);
		apply(request, visit);
		return PetDto.VisitResponse.from(visit);
	}

	@Transactional
	public void deleteVisit(int ownerId, int petId, int visitId) {
		this.visits.delete(findVisit(ownerId, petId, visitId));
	}

	/** 처방이 빈칸이면 저장하지 않는다(null). */
	private static void apply(PetDto.VisitRequest request, Visit visit) {
		String prescription = request.prescription();
		visit.update(request.date(), request.description(),
				(prescription == null || prescription.isBlank()) ? null : prescription.strip());
	}

	private PetType findType(int typeId) {
		return this.types.findById(typeId)
			.orElseThrow(() -> new ApiValidationException("typeId", "notFound", "찾을 수 없습니다"));
	}

	private Pet findPet(int ownerId, int petId) {
		if (!this.owners.existsById(ownerId)) {
			throw new ResourceNotFoundException("Owner " + ownerId + " not found");
		}
		return this.pets.findByIdAndOwnerId(petId, ownerId)
			.orElseThrow(() -> new ResourceNotFoundException("Pet " + petId + " not found for owner " + ownerId));
	}

	private Visit findVisit(int ownerId, int petId, int visitId) {
		findPet(ownerId, petId);
		return this.visits.findByIdAndPetId(visitId, petId)
			.orElseThrow(() -> new ResourceNotFoundException("Visit " + visitId + " not found for pet " + petId));
	}

}
