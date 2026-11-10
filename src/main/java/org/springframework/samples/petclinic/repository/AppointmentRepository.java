package org.springframework.samples.petclinic.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.entity.Appointment;
import org.springframework.samples.petclinic.entity.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

	@Query(value = """
			SELECT a FROM Appointment a JOIN FETCH a.hospital JOIN FETCH a.pet JOIN FETCH a.vet
			WHERE (:hospitalId IS NULL OR a.hospital.id = :hospitalId)
			  AND (:vetId IS NULL OR a.vet.id = :vetId)
			  AND (:petId IS NULL OR a.pet.id = :petId)
			  AND (:status IS NULL OR a.status = :status)
			ORDER BY a.appointmentAt, a.id""", countQuery = """
			SELECT COUNT(a) FROM Appointment a
			WHERE (:hospitalId IS NULL OR a.hospital.id = :hospitalId)
			  AND (:vetId IS NULL OR a.vet.id = :vetId)
			  AND (:petId IS NULL OR a.pet.id = :petId)
			  AND (:status IS NULL OR a.status = :status)""")
	Page<Appointment> search(@Param("hospitalId") Integer hospitalId, @Param("vetId") Integer vetId,
			@Param("petId") Integer petId, @Param("status") AppointmentStatus status, Pageable pageable);

	@Query("""
			SELECT a FROM Appointment a JOIN FETCH a.hospital JOIN FETCH a.pet JOIN FETCH a.vet
			WHERE a.vet.id = :vetId AND a.appointmentAt >= :from AND a.appointmentAt < :to
			ORDER BY a.appointmentAt, a.id""")
	List<Appointment> findVetSchedule(@Param("vetId") int vetId, @Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to);

	/** 취소되지 않은 예약 중 같은 수의사·같은 시각이 있는지 (excludeId: 수정 중인 예약 자신) */
	@Query("""
			SELECT COUNT(a) > 0 FROM Appointment a
			WHERE a.vet.id = :vetId AND a.appointmentAt = :appointmentAt
			  AND a.status <> org.springframework.samples.petclinic.entity.AppointmentStatus.CANCELLED
			  AND (:excludeId IS NULL OR a.id <> :excludeId)""")
	boolean isVetBooked(@Param("vetId") Integer vetId, @Param("appointmentAt") LocalDateTime appointmentAt,
			@Param("excludeId") Integer excludeId);

	/** 취소되지 않은 예약 중 같은 반려동물·같은 시각이 있는지 */
	@Query("""
			SELECT COUNT(a) > 0 FROM Appointment a
			WHERE a.pet.id = :petId AND a.appointmentAt = :appointmentAt
			  AND a.status <> org.springframework.samples.petclinic.entity.AppointmentStatus.CANCELLED
			  AND (:excludeId IS NULL OR a.id <> :excludeId)""")
	boolean isPetBooked(@Param("petId") Integer petId, @Param("appointmentAt") LocalDateTime appointmentAt,
			@Param("excludeId") Integer excludeId);

	@Modifying
	@Query("DELETE FROM Appointment a WHERE a.pet.id = :petId")
	void deleteByPetId(@Param("petId") int petId);

	@Modifying
	@Query("DELETE FROM Appointment a WHERE a.pet.id IN (SELECT p.id FROM Pet p WHERE p.owner.id = :ownerId)")
	void deleteByOwnerId(@Param("ownerId") int ownerId);

	@Modifying
	@Query("DELETE FROM Appointment a")
	int deleteAllAppointments();

}
