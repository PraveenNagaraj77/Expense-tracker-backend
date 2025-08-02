package com.praveen.expensetracker.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.praveen.expensetracker.dto.ExpenseDTO;
import com.praveen.expensetracker.dto.IncomeDTO;
import com.praveen.expensetracker.dto.RecentTransactionDTO;
import com.praveen.expensetracker.entity.ProfileEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;

    public Map<String, Object> getDashboardData() {
        ProfileEntity profile = profileService.getCurrentProfile();
        Map<String, Object> returnValue = new LinkedHashMap<>();

        // Fetch data
        List<IncomeDTO> latestIncomes = incomeService.getLatest5IncomesForCurrentUser();
        List<ExpenseDTO> latestExpenses = expenseService.getLatest5ExpensesForCurrentUser();
        BigDecimal totalIncome = incomeService.getTotalIncomeForCurrentUser();
        BigDecimal totalExpense = expenseService.getTotalExpenseForCurrentUser();

        // Fallback to 0 if null
        BigDecimal safeIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        BigDecimal safeExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;
        BigDecimal totalBalance = safeIncome.subtract(safeExpense);

        // Map IncomeDTO to RecentTransactionDTO
        Stream<RecentTransactionDTO> incomeStream = latestIncomes.stream().map(income -> RecentTransactionDTO.builder()
                .id(income.getId())
                .profileId(profile.getId())
                .icon(income.getIcon())
                .name(income.getName())
                .amount(income.getAmount())
                .date(income.getDate())
                .createdAt(income.getCreatedAt())
                .updatedAt(income.getUpdatedAt())
                .type("income")
                .build());

        // Map ExpenseDTO to RecentTransactionDTO
        Stream<RecentTransactionDTO> expenseStream = latestExpenses.stream().map(expense -> RecentTransactionDTO.builder()
                .id(expense.getId())
                .profileId(profile.getId())
                .icon(expense.getIcon())
                .name(expense.getName())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .type("expense")
                .build());

        // Merge, sort, and collect
        List<RecentTransactionDTO> recentTransactions = Stream.concat(incomeStream, expenseStream)
                .sorted(Comparator.comparing(RecentTransactionDTO::getDate).reversed())
                .collect(Collectors.toList());

        // Final output
        returnValue.put("totalIncome", safeIncome);
        returnValue.put("totalExpense", safeExpense);
        returnValue.put("totalBalance", totalBalance); // ✅ Added
        returnValue.put("recent5Incomes", latestIncomes);
        returnValue.put("recent5Expenses", latestExpenses);
        returnValue.put("recentTransactions", recentTransactions);

        return returnValue;
    }
}
