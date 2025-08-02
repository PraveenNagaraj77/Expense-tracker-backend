package com.praveen.expensetracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


import org.springframework.stereotype.Service;

import com.praveen.expensetracker.dto.ExpenseDTO;
import com.praveen.expensetracker.entity.CategoryEntity;
import com.praveen.expensetracker.entity.ExpenseEntity;
import com.praveen.expensetracker.entity.ProfileEntity;
import com.praveen.expensetracker.repository.CategoryRepository;
import com.praveen.expensetracker.repository.ExpenseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;

    // Add a new Expense
    public ExpenseDTO addExpense(ExpenseDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();

        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));

        ExpenseEntity newExpense = toEntity(dto, profile, category);
        ExpenseEntity savedExpense = expenseRepository.save(newExpense);

        return toDto(savedExpense);
    }

    // Retreive all the Expenses for the current month or based on start date and
    // end datae

    public List<ExpenseDTO> getCurrentMonthExpenseForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);

        return list.stream().map(this::toDto).toList();

    }

    // Delete Expense by ID for current user
    public void deleteExpense(long expenseId) {
        ProfileEntity profile = profileService.getCurrentProfile();

        ExpenseEntity entity = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found "));

        // Ensure the expense belongs to the current user
        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("You are not authorized to delete this expense.");
        }

        expenseRepository.delete(entity);
    }


    //Get latest 5 expenses for current User
    public List<ExpenseDTO> getLatest5ExpensesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list =  expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDto).toList();
        
    }

    //Get total expenses for current User
    public BigDecimal getTotalExpenseForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total =  expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    //Filter Expsenses
    public List<ExpenseDTO> filterExpenses(LocalDate startDate,LocalDate endDate,String keyword,org.springframework.data.domain.Sort sort){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list =  expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);
        return list.stream().map(this::toDto).toList();
    }

    //Notifications
    public List<ExpenseDTO> getExpensesForUserOnDate(Long profileId, LocalDate date){
        List<ExpenseEntity> list =  expenseRepository.findByProfileIdAndDate(profileId, date);
        return list.stream().map(this::toDto).toList();
    }


    public BigDecimal getExpensesTotalForUserOnDate(Long profileId, LocalDate date) {
    List<ExpenseDTO> expenses = getExpensesForUserOnDate(profileId, date);
    return expenses.stream()
            .map(ExpenseDTO::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}









    // helper methods

    private ExpenseEntity toEntity(ExpenseDTO dto, ProfileEntity profile, CategoryEntity category) {
        return ExpenseEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private ExpenseDTO toDto(ExpenseEntity entity) {
        return ExpenseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : "N/A")
                .amount(entity.getAmount())
                .date(entity.getDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

}
