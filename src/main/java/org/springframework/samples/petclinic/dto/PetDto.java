package org.springframework.samples.petclinic.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.samples.petclinic.entity.Pet;
import org.springframework.samples.petclinic.entity.PetType;
import org.springframework.samples.petclinic.entity.Visit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

/**
 * 반려동물·진료 기록·반려동물 종류 API의 요청·응답 형식.
 */
public final class PetDto {

	private static final String REQUIRED = "입력이 필요합니다";

	private PetDto() {
	}

	/** 반려동물 등록·수정 요청 */
	public record Request(@NotBlank(message = REQUIRED) @Size(max = 255) String name,
			@NotNull(message = REQUIRED) @PastOrPresent(message = "잘못된 날짜입니다") LocalDate birthDate,
			@NotNull(message = REQUIRED) Integer typeId) {
	}

	public record Response(Integer id, String name, LocalDate birthDate, TypeResponse type,
			List<VisitResponse> visits) {

		public static Response from(Pet pet) {
			return new Response(pet.getId(), pet.getName(), pet.getBirthDate(), TypeResponse.from(pet.getType()),
					pet.getVisits().stream().map(VisitResponse::from).toList());
		}

	}

	public record TypeResponse(Integer id, String name) {

		public static TypeResponse from(PetType type) {
			return new TypeResponse(type.getId(), type.getName());
		}

	}

	/** 진료 기록 등록·수정 요청 */
	public record VisitRequest(
			@NotNull @PastOrPresent(message = "진료 날짜는 오늘 이후일 수 없습니다") LocalDate date,
			@NotBlank @Size(max = 255) String description, @Size(max = 255) String prescription) {
	}

	public record VisitResponse(Integer id, LocalDate date, String description, String prescription) {

		public static VisitResponse from(Visit visit) {
			return new VisitResponse(visit.getId(), visit.getDate(), visit.getDescription(), visit.getPrescription());
		}

	}

}
