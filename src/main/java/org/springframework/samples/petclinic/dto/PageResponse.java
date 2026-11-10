package org.springframework.samples.petclinic.dto;

import java.util.List;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * 목록 응답 공통 형식. page는 1부터 센다.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

	private static final int MAX_PAGE_SIZE = 100;

	/** 1부터 세는 page와 1~100으로 제한한 size로 조회 조건을 만든다. */
	public static @NonNull PageRequest request(int page, int size, @NonNull Sort sort) {
		return PageRequest.of(Math.max(page, 1) - 1, Math.clamp(size, 1, MAX_PAGE_SIZE), sort);
	}

	public static <S, T> PageResponse<T> from(Page<S> page, Function<S, T> mapper) {
		return new PageResponse<>(page.getContent().stream().map(mapper).toList(), page.getNumber() + 1,
				page.getSize(), page.getTotalElements(), page.getTotalPages());
	}

}
