package org.springframework.samples.petclinic.controller;

import java.util.List;

import org.springframework.samples.petclinic.dto.HospitalDto;
import org.springframework.samples.petclinic.dto.PageResponse;
import org.springframework.samples.petclinic.service.HospitalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 병원과 수의사 (조회 전용) */
@RestController
@RequestMapping("/api")
class HospitalController {

	private final HospitalService hospitalService;

	HospitalController(HospitalService hospitalService) {
		this.hospitalService = hospitalService;
	}

	@GetMapping("/hospitals")
	List<HospitalDto.Response> listHospitals() {
		return this.hospitalService.listHospitals();
	}

	@GetMapping("/hospitals/{hospitalId}")
	HospitalDto.Response showHospital(@PathVariable int hospitalId) {
		return this.hospitalService.getHospital(hospitalId);
	}

	@GetMapping("/hospitals/{hospitalId}/vets")
	List<HospitalDto.VetResponse> listHospitalVets(@PathVariable int hospitalId) {
		return this.hospitalService.listVets(hospitalId);
	}

	@GetMapping("/vets")
	PageResponse<HospitalDto.VetResponse> listVets(@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int size) {
		return this.hospitalService.listAllVets(page, size);
	}

}
