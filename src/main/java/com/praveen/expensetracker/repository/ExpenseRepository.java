package com.praveen.expensetracker.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.praveen.expensetracker.entity.ExpenseEntity;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    // Fetch all expenses by profile in descending date order
    List<ExpenseEntity> findByProfileIdOrderByDateDesc(Long profileId);

    // Fetch top 5 recent expenses by profile
    List<ExpenseEntity> findTop5ByProfileIdOrderByDateDesc(Long profileId);

    // Get total expense amount for a profile
    @Query("SELECT SUM(e.amount) FROM ExpenseEntity e WHERE e.profile.id = :profileId")
    BigDecimal findTotalExpenseByProfileId(@Param("profileId") Long profileId);

    // Filter expenses by date range and keyword in name, with sorting
    List<ExpenseEntity> findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
        Long profileId,
        LocalDate startDate,
        LocalDate endDate,
        String keyword,
        Sort sort
    );

    // Filter expenses by date range only
    List<ExpenseEntity> findByProfileIdAndDateBetween(
        Long profileId,
        LocalDate startDate,
        LocalDate endDate
    );



    List<ExpenseEntity>  findByProfileIdAndDate(Long profileId,LocalDate date);




}
