package com.example.burserker.gujeuk.domain.purpose.domain.repository;

import com.example.burserker.gujeuk.domain.purpose.domain.PurposeReservation;
import com.example.burserker.gujeuk.domain.purpose.enums.ReservationTime;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationStatusRepository extends Repository<PurposeReservation, Long> {
    @EntityGraph(attributePaths = "purpose")
    List<PurposeReservation> findAllByPurposeOrganIdAndReservationDateOrderByStartTimeAsc(
            Long organId, LocalDate reservationDate);

    long countByPurposeOrganIdAndReservationDateAndStartTimeAndPurposeReservationStatusTrue(
            Long organId, LocalDate reservationDate, ReservationTime startTime);

    @Query("select count(p) from Purpose p where p.organ.id = :organId and p.reservationStatus = true")
    long countReservationRequiredPurpose(@Param("organId") Long organId);
}
