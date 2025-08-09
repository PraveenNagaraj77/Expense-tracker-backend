package com.praveen.expensetracker.controller;

import com.praveen.expensetracker.dto.ExpenseDTO;
import com.praveen.expensetracker.dto.IncomeDTO;
import com.praveen.expensetracker.service.ExcelService;
import com.praveen.expensetracker.service.ExpenseService;
import com.praveen.expensetracker.service.IncomeService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService excelService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    @GetMapping("/download/income")
    public void downloadIncomeExcel(HttpServletResponse response) throws IOException {
        List<IncomeDTO> incomes = incomeService.getCurrentMonthIncomeForCurrentUser(); // Replace with correct method if needed

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=incomes.xlsx");

        var excelStream = excelService.writeIncomeToExcel(incomes);
        excelStream.transferTo(response.getOutputStream());
    }

    @GetMapping("/download/expense")
    public void downloadExpenseExcel(HttpServletResponse response) throws IOException {
        List<ExpenseDTO> expenses = expenseService.getCurrentMonthExpenseForCurrentUser(); // Replace with correct method if needed

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=expenses.xlsx");

        var excelStream = excelService.writeExpenseToExcel(expenses);
        excelStream.transferTo(response.getOutputStream());
    }
}
