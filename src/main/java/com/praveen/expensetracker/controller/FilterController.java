package com.praveen.expensetracker.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.praveen.expensetracker.dto.ExpenseDTO;
import com.praveen.expensetracker.dto.FilterDTO;
import com.praveen.expensetracker.dto.IncomeDTO;
import com.praveen.expensetracker.service.ExpenseService;
import com.praveen.expensetracker.service.IncomeService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<?> filterTransactions(@RequestBody FilterDTO filter) {
        LocalDate startDate = filter.getStartDate() != null ? filter.getStartDate() : LocalDate.MIN;
        LocalDate endDate = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();
        String keyword = filter.getKeyword() != null ? filter.getKeyword() : "";

        // Default sort
        String sortField = filter.getSortField() != null ? filter.getSortField() : "date";
        Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortField);

        Map<String, Object> result = new HashMap<>();

        String type = filter.getType();
        if ("income".equalsIgnoreCase(type)) {
            List<IncomeDTO> incomes = incomeService.filterIncomes(startDate, endDate, keyword, sort);
            result.put("incomes", incomes);
            return ResponseEntity.ok(incomes);
        } else if ("expense".equalsIgnoreCase(type)) {
            List<ExpenseDTO> expenses = expenseService.filterExpenses(startDate, endDate, keyword, sort);
            result.put("expenses", expenses);
            return ResponseEntity.ok(expenses);
        } else { // type is null or "all"
            List<IncomeDTO> incomes = incomeService.filterIncomes(startDate, endDate, keyword, sort);
            List<ExpenseDTO> expenses = expenseService.filterExpenses(startDate, endDate, keyword, sort);
            result.put("incomes", incomes);
            result.put("expenses", expenses);
        }

        return ResponseEntity.ok(result);
    }
}
