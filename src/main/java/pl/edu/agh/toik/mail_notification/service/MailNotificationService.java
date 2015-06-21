package main.java.pl.edu.agh.toik.mail_notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

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

    @Deprecated
    @Async
    public void sendCrawlerStatisticMailAsync(long startTime, Integer allArticlesCrawled, Integer allCommentsCrawled, Integer allSubCommentsCrawled) {
        long time = System.currentTimeMillis() - startTime;

        Integer frequency = Integer.valueOf(env.getRequiredProperty("mail.time"));

        if (time % frequency >= 0 && time % frequency <= 400) {
            sendMailNotification(
                    "NaTematCrawler statistic",
                    "Time: " + new Date() + " (" + time + "[ms])" + "\n" +
                    "All articles crawled: " + allArticlesCrawled + "\n" +
                    "All comments crawled: " + allCommentsCrawled + "\n" +
                    "All subcomments crawled: " + allSubCommentsCrawled + "\n"
            );
        }

    }

}
