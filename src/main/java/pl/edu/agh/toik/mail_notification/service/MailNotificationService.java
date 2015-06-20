package main.java.pl.edu.agh.toik.mail_notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class MailNotificationService {

    @Autowired
    private MailSender mailSender;

    @Autowired
    private Environment env;

    public void sendMailNotification(String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(env.getRequiredProperty("mail.username"));
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

}
