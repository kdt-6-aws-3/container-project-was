package org.springframework.samples.petclinic.dto;

import java.util.List;

import org.springframework.samples.petclinic.entity.Owner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 보호자 API의 요청·응답 형식.
 */
public final class OwnerDto {

	private OwnerDto() {
	}

	/** 등록·수정 요청 */
	public record Request(@NotBlank @Size(max = 255) String name, @NotBlank @Size(max = 255) String address,
			@NotBlank @Pattern(regexp = "\\d{2,3}-\\d{3,4}-\\d{4}",
					message = "전화번호는 010-1234-5678 형식이어야 합니다") String phone) {
	}

	/** 목록 한 줄: 반려동물은 이름만 */
	public record Summary(Integer id, String name, String address, String phone, List<String> pets) {

		public static Summary from(Owner owner) {
			return new Summary(owner.getId(), owner.getName(), owner.getAddress(), owner.getPhone(),
					owner.getPets().stream().map(pet -> pet.getName()).toList());
		}

	}

	/** 상세: 반려동물과 진료 기록까지 */
	public record Response(Integer id, String name, String address, String phone, List<PetDto.Response> pets) {

		public static Response from(Owner owner) {
			return new Response(owner.getId(), owner.getName(), owner.getAddress(), owner.getPhone(),
					owner.getPets().stream().map(PetDto.Response::from).toList());
		}

	}

}
