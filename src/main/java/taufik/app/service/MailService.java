/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.service;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taufik.app.web.model.Mail;

/**
 *
 * @author Randy
 */
@Service
@Transactional(readOnly = true, rollbackFor = { Exception.class })
public class MailService {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
        
    @Autowired
    private JavaMailSender javaMailSender;
    
    public void sendSimpleMail(Mail mail) {
        MimeMessagePreparator preparator = (MimeMessage mimeMessage) -> {
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(mail.getTo()));
            mimeMessage.setFrom(new InternetAddress(mail.getFrom()));
            mimeMessage.setSubject(mail.getSubject());
            mimeMessage.setText(mail.getBody());
        };

        try {
            this.javaMailSender.send(preparator);
            log.info("Success sending email to : {} with subject : {}", mail.getTo(), mail.getSubject());
        } catch (MailException ex) {
            log.error("Failed to send email : {}", ex.getMessage(), ex);
        }
    }
}
