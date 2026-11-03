package com.example.burserker.gujeuk.domain.purpose.domain.repository;

import com.example.burserker.gujeuk.domain.purpose.domain.PurposeReservation;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationTime;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurposeReservationRepository extends JpaRepository<PurposeReservation, Long> {
    @Query("SELECT r.startTime FROM PurposeReservation r "
            + "WHERE r.purpose.id = :purposeId AND r.reservationDate = :reservationDate")
    List<ReservationTime> findReservedTimes(
            @Param("purposeId") Long purposeId, @Param("reservationDate") LocalDate reservationDate);
}
