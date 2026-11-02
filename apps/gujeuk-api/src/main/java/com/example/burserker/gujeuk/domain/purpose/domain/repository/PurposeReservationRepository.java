package com.example.burserker.gujeuk.domain.purpose.domain.repository;

import com.example.burserker.gujeuk.domain.purpose.domain.PurposeReservation;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurposeReservationRepository extends JpaRepository<PurposeReservation, Long> {
    @EntityGraph(attributePaths = "purpose")
    List<PurposeReservation> findAllByPurposeOrganIdAndReservationDateOrderByStartTimeAscIdAsc(
            Long organId, LocalDate reservationDate);
}
