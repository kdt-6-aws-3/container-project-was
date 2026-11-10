package org.springframework.samples.petclinic.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.samples.petclinic.dto.HospitalDto;
import org.springframework.samples.petclinic.dto.PageResponse;
import org.springframework.samples.petclinic.exception.ResourceNotFoundException;
import org.springframework.samples.petclinic.repository.HospitalRepository;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 병원과 소속 수의사 조회.
 */
@Service
@Transactional(readOnly = true)
public class HospitalService {

	private final HospitalRepository hospitals;

	private final VetRepository vets;

	public HospitalService(HospitalRepository hospitals, VetRepository vets) {
		this.hospitals = hospitals;
		this.vets = vets;
	}

	public List<HospitalDto.Response> listHospitals() {
		return this.hospitals.findAllByOrderByIdAsc().stream().map(HospitalDto.Response::from).toList();
	}

	public HospitalDto.Response getHospital(int hospitalId) {
		return HospitalDto.Response.from(this.hospitals.findById(hospitalId)
			.orElseThrow(() -> new ResourceNotFoundException("Hospital " + hospitalId + " not found")));
	}

	public List<HospitalDto.VetResponse> listVets(int hospitalId) {
		if (!this.hospitals.existsById(hospitalId)) {
			throw new ResourceNotFoundException("Hospital " + hospitalId + " not found");
		}
		return this.vets.findByHospitalIdOrderByIdAsc(hospitalId).stream().map(HospitalDto.VetResponse::from).toList();
	}

	public PageResponse<HospitalDto.VetResponse> listAllVets(int page, int size) {
		return PageResponse.from(this.vets.findAll(PageResponse.request(page, size, Sort.by("id"))),
				HospitalDto.VetResponse::from);
	}

}
