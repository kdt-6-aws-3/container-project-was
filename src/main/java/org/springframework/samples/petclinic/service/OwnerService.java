package org.springframework.samples.petclinic.service;

import org.springframework.data.domain.Sort;
import org.springframework.samples.petclinic.dto.OwnerDto;
import org.springframework.samples.petclinic.dto.PageResponse;
import org.springframework.samples.petclinic.entity.Owner;
import org.springframework.samples.petclinic.exception.ResourceNotFoundException;
import org.springframework.samples.petclinic.repository.AppointmentRepository;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OwnerService {

	private final OwnerRepository owners;

	private final AppointmentRepository appointments;

	public OwnerService(OwnerRepository owners, AppointmentRepository appointments) {
		this.owners = owners;
		this.appointments = appointments;
	}

	/** 이름이 name으로 시작하는 보호자 목록 (대소문자 무시) */
	public PageResponse<OwnerDto.Summary> findOwners(String name, int page, int size) {
		return PageResponse.from(this.owners.findByNameStartingWithIgnoreCase(name.strip(),
				PageResponse.request(page, size, Sort.by("id"))), OwnerDto.Summary::from);
	}

	public OwnerDto.Response getOwner(int ownerId) {
		return OwnerDto.Response.from(findOwner(ownerId));
	}

	@Transactional
	public OwnerDto.Response createOwner(OwnerDto.Request request) {
		Owner owner = new Owner();
		owner.update(request.name(), request.address(), request.phone());
		return OwnerDto.Response.from(this.owners.save(owner));
	}

	@Transactional
	public OwnerDto.Response updateOwner(int ownerId, OwnerDto.Request request) {
		Owner owner = findOwner(ownerId);
		owner.update(request.name(), request.address(), request.phone());
		return OwnerDto.Response.from(owner);
	}

	/** 예약을 먼저 지운 뒤 보호자를 지운다. 반려동물·진료 기록은 엔티티 cascade로 함께 지워진다. */
	@Transactional
	public void deleteOwner(int ownerId) {
		Owner owner = findOwner(ownerId);
		this.appointments.deleteByOwnerId(ownerId);
		this.owners.delete(owner);
	}

	private Owner findOwner(int ownerId) {
		return this.owners.findById(ownerId)
			.orElseThrow(() -> new ResourceNotFoundException("Owner " + ownerId + " not found"));
	}

}
