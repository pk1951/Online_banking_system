package com.truebank.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * Send simple text email
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    /**
     * Send HTML email
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String templateName, Context context) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo(to);
        helper.setSubject(subject);
        
        String htmlContent = templateEngine.process(templateName, context);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }

    /**
     * Send manager credentials email
     */
    @Async
    public void sendManagerCredentialsEmail(String to, String username, String password, String branchName) {
        String subject = "TRUE Bank - Your Manager Account Credentials";
        String text = "Dear Manager,\n\n"
                + "You have been assigned as the manager of " + branchName + " branch at TRUE Bank.\n\n"
                + "Your login credentials are:\n"
                + "Username: " + username + "\n"
                + "Password: " + password + "\n\n"
                + "Please change your password after your first login for security reasons.\n\n"
                + "Regards,\n"
                + "TRUE Bank Administration";
        
        sendSimpleEmail(to, subject, text);
    }

    /**
     * Send account creation confirmation email
     */
    @Async
    public void sendAccountCreationEmail(String to, String fullName, String accountNumber, String accountType) {
        String subject = "TRUE Bank - New Account Created";
        String text = "Dear " + fullName + ",\n\n"
                + "Congratulations! Your " + accountType + " account has been successfully created at TRUE Bank.\n\n"
                + "Your account number is: " + accountNumber + "\n\n"
                + "You can now access your account through our online banking portal.\n\n"
                + "Regards,\n"
                + "TRUE Bank Customer Service";
        
        sendSimpleEmail(to, subject, text);
    }

    /**
     * Send transaction notification email
     */
    @Async
    public void sendTransactionNotificationEmail(String to, String fullName, String accountNumber, 
                                                String transactionType, String amount, String balance) {
        String subject = "TRUE Bank - Transaction Notification";
        String text = "Dear " + fullName + ",\n\n"
                + "A " + transactionType + " transaction of Rs. " + amount + " has been processed on your account " + accountNumber + ".\n\n"
                + "Your current balance is: Rs. " + balance + "\n\n"
                + "If you did not authorize this transaction, please contact our customer service immediately.\n\n"
                + "Regards,\n"
                + "TRUE Bank Customer Service";
        
        sendSimpleEmail(to, subject, text);
    }

    /**
     * Send loan application confirmation email
     */
    @Async
    public void sendLoanApplicationEmail(String to, String fullName, String loanType, String amount) {
        String subject = "TRUE Bank - Loan Application Received";
        String text = "Dear " + fullName + ",\n\n"
                + "Your application for a " + loanType + " loan of Rs. " + amount + " has been received.\n\n"
                + "Your application is currently under review. We will notify you once a decision has been made.\n\n"
                + "Regards,\n"
                + "TRUE Bank Loan Department";
        
        sendSimpleEmail(to, subject, text);
    }

    /**
     * Send loan approval email
     */
    @Async
    public void sendLoanApprovalEmail(String to, String fullName, String loanType, String amount, String interestRate) {
        String subject = "TRUE Bank - Loan Approved";
        String text = "Dear " + fullName + ",\n\n"
                + "Congratulations! Your application for a " + loanType + " loan of Rs. " + amount + " has been approved.\n\n"
                + "Loan Details:\n"
                + "- Amount: Rs. " + amount + "\n"
                + "- Interest Rate: " + interestRate + "%\n\n"
                + "The loan amount will be disbursed to your account shortly.\n\n"
                + "Regards,\n"
                + "TRUE Bank Loan Department";
        
        sendSimpleEmail(to, subject, text);
    }

    /**
     * Send loan rejection email
     */
    @Async
    public void sendLoanRejectionEmail(String to, String fullName, String loanType, String amount, String reason) {
        String subject = "TRUE Bank - Loan Application Status";
        String text = "Dear " + fullName + ",\n\n"
                + "We regret to inform you that your application for a " + loanType + " loan of Rs. " + amount + " has not been approved.\n\n"
                + "Reason: " + reason + "\n\n"
                + "If you have any questions, please contact our loan department for more information.\n\n"
                + "Regards,\n"
                + "TRUE Bank Loan Department";
        
        sendSimpleEmail(to, subject, text);
    }
}
