package com.praveen.expensetracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.praveen.expensetracker.dto.ExpenseDTO;
import com.praveen.expensetracker.dto.IncomeDTO;
import com.praveen.expensetracker.entity.CategoryEntity;
import com.praveen.expensetracker.entity.ExpenseEntity;
import com.praveen.expensetracker.entity.IncomeEntity;
import com.praveen.expensetracker.entity.ProfileEntity;
import com.praveen.expensetracker.repository.CategoryRepository;
import com.praveen.expensetracker.repository.IncomeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncomeService {
    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;

    public IncomeDTO addIncome(IncomeDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();

        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));

        IncomeEntity newIncome = toEntity(dto, profile, category);
        IncomeEntity savedIncome = incomeRepository.save(newIncome);

        return toDto(savedIncome);
    }

    // Retrieve all the INCOMES for the current month
    public List<IncomeDTO> getCurrentMonthIncomeForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

        List<IncomeEntity> list = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);

        return list.stream().map(this::toDto).toList();
    }

    // Delete Income by ID for current user
    public void deleteIncome(long incomeId) {
        ProfileEntity profile = profileService.getCurrentProfile();

        IncomeEntity entity = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new RuntimeException("Income not found"));

        // Ensure the income belongs to the current user
        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("You are not authorized to delete this income.");
        }

        incomeRepository.delete(entity);
    }



    //Get latest 5 incomes for current User
    public List<IncomeDTO> getLatest5IncomesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list =  incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDto).toList();
        
    }

    //Get total income for current User
    public BigDecimal getTotalIncomeForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total =  incomeRepository.findTotalIncomeByProfileId(profile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }


    //Filter Incomes
    public List<IncomeDTO> filterIncomes(LocalDate startDate,LocalDate endDate,String keyword,org.springframework.data.domain.Sort sort){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list =  incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);
        return list.stream().map(this::toDto).toList();
    }


    public BigDecimal getTotalIncomeByProfileId(Long profileId) {
    BigDecimal total = incomeRepository.findTotalIncomeByProfileId(profileId);
    return total != null ? total : BigDecimal.ZERO;
}


    
    // helper methods

    private IncomeEntity toEntity(IncomeDTO dto, ProfileEntity profile, CategoryEntity category) {
        return IncomeEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private IncomeDTO toDto(IncomeEntity entity) {
        return IncomeDTO.builder()
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
