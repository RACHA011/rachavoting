package com.racha.rachavoting.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.racha.rachavoting.util.email.EmailDetails;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    public Boolean sendSimpleEmail(EmailDetails emailDetails) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            mailMessage.setFrom(sender);
            mailMessage.setTo(emailDetails.getRecipient());
            mailMessage.setText(emailDetails.getMsgBody());
            mailMessage.setSubject(emailDetails.getSubject());

            javaMailSender.send(mailMessage);
            return true;
        } catch (Exception e) {
            return false;
        }
        
    }

    // New method for HTML emails with plain text fallback
    public Boolean sendEmail(EmailDetails emailDetails) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(sender);
            helper.setTo(emailDetails.getRecipient());
            helper.setSubject(emailDetails.getSubject());
            
            if (emailDetails.isHtml() && emailDetails.getPlainTextContent() != null) {
                // Send multipart email with HTML and plain text
                helper.setText(emailDetails.getPlainTextContent(), emailDetails.getMsgBody());
            } else if (emailDetails.isHtml()) {
                // Send HTML only
                helper.setText(emailDetails.getMsgBody(), true);
            } else {
                // Send plain text only
                helper.setText(emailDetails.getMsgBody(), false);
            }
            
            javaMailSender.send(mimeMessage);
            log.info("Email sent successfully to {}", emailDetails.getRecipient());
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send email to {}", emailDetails.getRecipient(), e);
            return false;
        }
    }
}