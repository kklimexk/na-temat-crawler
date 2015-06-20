package main.java.pl.edu.agh.toik.app;

import main.java.pl.edu.agh.toik.crawler.*;
import main.java.pl.edu.agh.toik.database.PersistenceConfig;
import main.java.pl.edu.agh.toik.mail_notification.MailNotificationConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Import({PersistenceConfig.class, MailNotificationConfig.class})
@Configuration
@ComponentScan("main.java.pl.edu.agh.toik.crawler")
@EnableAsync
public class NaTematCrawlerConfig {

    @Bean
    ICrawlerService crawlerService() {
        return new NaTematCrawlerService();
    }

    @Bean
    ICrawlerSettings crawlerSettings() {
        return new NaTematCrawlerSettings();
    }

}
