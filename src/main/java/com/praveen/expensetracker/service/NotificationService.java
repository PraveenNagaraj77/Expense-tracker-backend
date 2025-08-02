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

    // ⏰ Daily reminder to update income/expenses
    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Kolkata")
    public void sendDailyIncomeExpenseReminder() {
        log.info("Job Started: sendDailyIncomeExpenseReminder()");
        List<ProfileEntity> profiles = profileRepository.findAll();

        for (ProfileEntity profile : profiles) {
            if (profile.getEmail() == null || profile.getFullName() == null)
                continue;

            String subject = "Daily Expense Tracker Reminder";
            String body = "Hi " + profile.getFullName() + ",<br><br>" +
                    "Don't forget to update your income and expenses for today!<br>" +
                    "Click the link below to track your expenses:<br>" +
                    "<a href='" + frontendUrl + "'>Go to Expense Tracker</a><br><br>" +
                    "Regards,<br>Expense Tracker Team";

            try {
                emailService.sendEmail(profile.getEmail(), subject, body);
                log.info("Reminder email sent to {}", profile.getEmail());
            } catch (Exception e) {
                log.error("Failed to send reminder email to {}: {}", profile.getEmail(), e.getMessage());
            }
        }

        log.info("Job Ended: sendDailyIncomeExpenseReminder()");
    }

    // ✅ Daily summary email with income, expense, savings
    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Kolkata") // 10 minutes after the reminder
    public void sendDailyExpenseSummary() {
        log.info("Job Started: sendDailyExpenseSummary()");
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

            String subject = "Your Daily Expense Summary";
            String body = "Hi " + profile.getFullName() + ",<br><br>" +
                    "Here's your financial summary for today:<br>" +
                    "<ul>" +
                    "<li><strong>Income:</strong> ₹" + income.setScale(2, RoundingMode.HALF_UP) + "</li>" +
                    "<li><strong>Expenses:</strong> ₹" + expense.setScale(2, RoundingMode.HALF_UP) + "</li>" +
                    "<li><strong>Net Savings:</strong> ₹" + netSavings.setScale(2, RoundingMode.HALF_UP) + "</li>" +
                    "</ul>" +
                    "Click below to review or update your data:<br>" +
                    "<a href='" + frontendUrl + "'>Go to Expense Tracker</a><br><br>" +
                    "Regards,<br>Expense Tracker Team";

            try {
                emailService.sendEmail(profile.getEmail(), subject, body);
                log.info("Summary email sent to {}", profile.getEmail());
            } catch (Exception e) {
                log.error("Failed to send summary email to {}: {}", profile.getEmail(), e.getMessage());
            }
        }

        log.info("Job Ended: sendDailyExpenseSummary()");
    }
}
