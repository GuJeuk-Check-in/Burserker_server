package com.example.burserker.gujeuk.domain.purpose.domain.repository;

import com.example.burserker.gujeuk.domain.purpose.domain.PurposeReservation;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationStatus;
import com.example.burserker.gujeuk.domain.purpose.domain.enums.ReservationTime;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PurposeReservationRepository extends JpaRepository<PurposeReservation, Long> {
    @EntityGraph(attributePaths = {"purpose", "subPurpose"})
    List<PurposeReservation> findAllByPurposeOrganIdAndReservationDateOrderByStartTimeAsc(
            Long organId, LocalDate reservationDate);

    int countByPurposeOrganIdAndReservationDateAndStartTime(
            Long organId, LocalDate reservationDate, ReservationTime startTime);

    // 현재 예약 가능한 단위(RESERVATION 상위, SUB_INCLUDE 상위의 예약 가능 하위)에 걸린 예약만 센다
    @Query("SELECT count(r) FROM PurposeReservation r JOIN r.purpose p LEFT JOIN r.subPurpose s "
            + "WHERE p.organ.id = :organId AND r.reservationDate = :reservationDate AND r.startTime = :startTime "
            + "AND ((s IS NULL AND p.reservationStatus = :reservation) "
            + "OR (s IS NOT NULL AND p.reservationStatus = :subInclude AND s.reservationStatus = true))")
    int countReservableUnitReservations(
            @Param("organId") Long organId, @Param("reservationDate") LocalDate reservationDate,
            @Param("startTime") ReservationTime startTime,
            @Param("reservation") ReservationStatus reservation, @Param("subInclude") ReservationStatus subInclude);

    @Query("SELECT r.startTime FROM PurposeReservation r "
            + "WHERE r.purpose.id = :purposeId AND r.slotSubPurposeId = :slotSubPurposeId "
            + "AND r.reservationDate = :reservationDate")
    List<ReservationTime> findReservedTimes(
            @Param("purposeId") Long purposeId, @Param("slotSubPurposeId") Long slotSubPurposeId,
            @Param("reservationDate") LocalDate reservationDate);

    boolean existsByPurposeIdAndSlotSubPurposeIdAndReservationDateAndStartTime(
            Long purposeId, Long slotSubPurposeId, LocalDate reservationDate, ReservationTime startTime);

    Optional<PurposeReservation> findByIdAndPurposeOrganId(Long id, Long organId);

    @Modifying
    @Query("DELETE FROM PurposeReservation r WHERE r.purpose.id = :purposeId")
    void deleteAllByPurposeId(@Param("purposeId") Long purposeId);

    @Modifying
    @Query("DELETE FROM PurposeReservation r WHERE r.subPurpose.id = :subPurposeId")
    void deleteAllBySubPurposeId(@Param("subPurposeId") Long subPurposeId);

    // 상위 방문목적 단위 예약 중 기준일 이후 예약을 지운다 (예약 불가로 전환될 때)
    @Modifying
    @Query("DELETE FROM PurposeReservation r WHERE r.purpose.id = :purposeId AND r.subPurpose IS NULL "
            + "AND r.reservationDate >= :fromDate")
    void deletePurposeUnitReservationsFrom(
            @Param("purposeId") Long purposeId, @Param("fromDate") LocalDate fromDate);

    // 하위 방문목적 단위 예약 중 기준일 이후 예약을 지운다 (예약 불가로 전환될 때)
    @Modifying
    @Query("DELETE FROM PurposeReservation r WHERE r.subPurpose.id = :subPurposeId "
            + "AND r.reservationDate >= :fromDate")
    void deleteSubPurposeUnitReservationsFrom(
            @Param("subPurposeId") Long subPurposeId, @Param("fromDate") LocalDate fromDate);
}
