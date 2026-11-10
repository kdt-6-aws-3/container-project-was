package org.springframework.samples.petclinic.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.dto.OwnerDto;
import org.springframework.samples.petclinic.dto.PageResponse;
import org.springframework.samples.petclinic.service.OwnerService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/** 보호자 */
@RestController
@RequestMapping("/api/owners")
class OwnerController {

	private final OwnerService ownerService;

	OwnerController(OwnerService ownerService) {
		this.ownerService = ownerService;
	}

	@GetMapping
	PageResponse<OwnerDto.Summary> findOwners(@RequestParam(defaultValue = "") String name,
			@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "5") int size) {
		return this.ownerService.findOwners(name, page, size);
	}

	@GetMapping("/{ownerId}")
	OwnerDto.Response showOwner(@PathVariable int ownerId) {
		return this.ownerService.getOwner(ownerId);
	}

	@PostMapping
	ResponseEntity<OwnerDto.Response> createOwner(@Valid @RequestBody OwnerDto.Request request) {
		OwnerDto.Response created = this.ownerService.createOwner(request);
		return ResponseEntity.status(HttpStatus.CREATED)
			.header(HttpHeaders.LOCATION, "/api/owners/" + created.id())
			.body(created);
	}

	@PutMapping("/{ownerId}")
	OwnerDto.Response updateOwner(@PathVariable int ownerId, @Valid @RequestBody OwnerDto.Request request) {
		return this.ownerService.updateOwner(ownerId, request);
	}

	@DeleteMapping("/{ownerId}")
	ResponseEntity<Void> deleteOwner(@PathVariable int ownerId) {
		this.ownerService.deleteOwner(ownerId);
		return ResponseEntity.noContent().build();
	}

}
