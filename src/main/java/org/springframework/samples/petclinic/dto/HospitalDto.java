package org.springframework.samples.petclinic.dto;

import java.util.List;

import org.springframework.samples.petclinic.entity.Hospital;
import org.springframework.samples.petclinic.entity.Specialty;
import org.springframework.samples.petclinic.entity.Vet;

/**
 * 병원·수의사 API의 응답 형식(조회 전용).
 */
public final class HospitalDto {

	private HospitalDto() {
	}

	public record Response(Integer id, String name, String address, String phone) {

		public static Response from(Hospital hospital) {
			return new Response(hospital.getId(), hospital.getName(), hospital.getAddress(), hospital.getPhone());
		}

	}

	public record VetResponse(Integer id, String name, Response hospital, List<SpecialtyResponse> specialties) {

		public static VetResponse from(Vet vet) {
			return new VetResponse(vet.getId(), vet.getName(), Response.from(vet.getHospital()),
					vet.getSpecialties().stream().map(SpecialtyResponse::from).toList());
		}

	}

	public record SpecialtyResponse(Integer id, String name) {

		public static SpecialtyResponse from(Specialty specialty) {
			return new SpecialtyResponse(specialty.getId(), specialty.getName());
		}

	}

}
