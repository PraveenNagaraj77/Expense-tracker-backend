package com.praveen.expensetracker.controller;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.praveen.expensetracker.dto.ExpenseDTO;
import com.praveen.expensetracker.dto.IncomeDTO;
import com.praveen.expensetracker.entity.ProfileEntity;
import com.praveen.expensetracker.service.EmailService;
import com.praveen.expensetracker.service.ExcelService;
import com.praveen.expensetracker.service.ExpenseService;
import com.praveen.expensetracker.service.IncomeService;
import com.praveen.expensetracker.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final ExcelService excelService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final EmailService emailService;
    private final ProfileService profileService;

    @GetMapping("/income-excel")
    public ResponseEntity<Void> emailIncomeExcel() {
        try {
            ProfileEntity profile = profileService.getCurrentProfile();
            String recipientEmail = profile.getEmail();

            List<IncomeDTO> incomeList = incomeService.getCurrentMonthIncomeForCurrentUser();
            ByteArrayInputStream excelStream = excelService.writeIncomeToExcel(incomeList);

            byte[] attachmentBytes = excelStream.readAllBytes();

            emailService.sendEmailWithAttachment(
                    recipientEmail,
                    "Your Monthly Income Report",
                    "Attached is your income report for this month.",
                    attachmentBytes,
                    "income_report.xlsx");

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/expense-excel")
    public ResponseEntity<Void> emailExpenseExcel() {
        try {
            ProfileEntity profile = profileService.getCurrentProfile();
            String recipientEmail = profile.getEmail();

            List<ExpenseDTO> expenseList = expenseService.getCurrentMonthExpenseForCurrentUser(); // ✅ FIXED
            ByteArrayInputStream excelStream = excelService.writeExpenseToExcel(expenseList);

            byte[] attachmentBytes = excelStream.readAllBytes();

            emailService.sendEmailWithAttachment(
                    recipientEmail,
                    "Your Monthly Expense Report",
                    "Attached is your expense report for this month.",
                    attachmentBytes,
                    "expense_report.xlsx");

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
