package com.praveen.expensetracker.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.praveen.expensetracker.entity.IncomeEntity;

public interface IncomeRepository extends JpaRepository<IncomeEntity, Long> {

    // Fetch all incomes for a profile, ordered by date descending
    List<IncomeEntity> findByProfileIdOrderByDateDesc(Long profileId);

    // Fetch top 5 recent incomes for a profile
    List<IncomeEntity> findTop5ByProfileIdOrderByDateDesc(Long profileId);

    // ✅ FIX: Rename method to match purpose: findTotalIncomeByProfileId
    @Query("SELECT SUM(i.amount) FROM IncomeEntity i WHERE i.profile.id = :profileId")
    BigDecimal findTotalIncomeByProfileId(@Param("profileId") Long profileId);

    // Fetch incomes by date range + keyword search
    List<IncomeEntity> findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
        Long profileId,
        LocalDate startDate,
        LocalDate endDate,
        String keyword,
        Sort sort
    );

    // Fetch incomes by date range
    List<IncomeEntity> findByProfileIdAndDateBetween(
        Long profileId,
        LocalDate startDate,
        LocalDate endDate
    );


    List<IncomeEntity> findByProfileIdAndDate(Long profileId, LocalDate date);

}
