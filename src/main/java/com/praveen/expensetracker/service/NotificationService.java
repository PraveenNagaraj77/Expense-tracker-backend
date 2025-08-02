package com.praveen.expensetracker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.praveen.expensetracker.entity.ProfileEntity;
import com.praveen.expensetracker.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    @Value("${expense.tracker.frontend.url}")
    private String frontendUrl;

    // ✅ Combined daily email with reminder + income, expense, savings summary
@Scheduled(cron = "0 0 23 * * *", zone = "Asia/Kolkata") // Every day at 11:00 PM
public void sendDailyReminderAndSummaryEmail() {
    log.info("Job Started: sendDailyReminderAndSummaryEmail()");
    List<ProfileEntity> profiles = profileRepository.findAll();

    for (ProfileEntity profile : profiles) {
        if (profile.getEmail() == null || profile.getFullName() == null)
            continue;

        LocalDate today = LocalDate.now();
        BigDecimal income = incomeService.getTotalIncomeByProfileId(profile.getId());
        BigDecimal expense = expenseService.getExpensesTotalForUserOnDate(profile.getId(), today);

        income = income != null ? income : BigDecimal.ZERO;
        expense = expense != null ? expense : BigDecimal.ZERO;

        BigDecimal netSavings = income.subtract(expense);

        String subject = "Daily Update: Track & Review Your Finances";
        String body = "Hi " + profile.getFullName() + ",<br><br>" +
                "Just a friendly reminder to update your income and expenses for today!<br><br>" +
                "Here's your financial snapshot for <strong>" + today + "</strong>:<br>" +
                "<ul>" +
                "<li><strong>Income:</strong> ₹" + income.setScale(2, RoundingMode.HALF_UP) + "</li>" +
                "<li><strong>Expenses:</strong> ₹" + expense.setScale(2, RoundingMode.HALF_UP) + "</li>" +
                "<li><strong>Net Savings:</strong> ₹" + netSavings.setScale(2, RoundingMode.HALF_UP) + "</li>" +
                "</ul>" +
                "Stay on track by updating your records here:<br>" +
                "<a href='" + frontendUrl + "'>Go to Expense Tracker</a><br><br>" +
                "Regards,<br>Expense Tracker Team";

        try {
            emailService.sendEmail(profile.getEmail(), subject, body);
            log.info("Combined email sent to {}", profile.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", profile.getEmail(), e.getMessage());
        }
    }

    log.info("Job Ended: sendDailyReminderAndSummaryEmail()");
}
}
