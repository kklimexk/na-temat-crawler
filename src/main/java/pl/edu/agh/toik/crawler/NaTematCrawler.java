package main.java.pl.edu.agh.toik.crawler;

import main.java.pl.edu.agh.toik.database.NaTematCrawlerDB;
import main.java.pl.edu.agh.toik.database.model.Article;
import main.java.pl.edu.agh.toik.database.model.Comment;
import main.java.pl.edu.agh.toik.database.model.Section;
import main.java.pl.edu.agh.toik.mail_notification.NaTematCrawlerMailNotification;
import main.java.pl.edu.agh.toik.mail_notification.service.MailNotificationService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class NaTematCrawler implements ICrawler {

    private final static int TIMEOUT = 10 * 1000;

    private ICrawlerService crawlerService;
    private ICrawlerSettings crawlerSettings;

    @Autowired
    private NaTematCrawlerDB naTematCrawlerDB;

    @Autowired
    private NaTematCrawlerMailNotification naTematCrawlerMailNotification;

    @Autowired
    public NaTematCrawler(ICrawlerService crawlerService, ICrawlerSettings crawlerSettings) {
        this.crawlerService = crawlerService;
        this.crawlerSettings = crawlerSettings;
    }

    public ICrawlerSettings getCrawlerSettings() {
        return crawlerSettings;
    }

    public ICrawlerService getCrawlerService() {
        return crawlerService;
    }

    public NaTematCrawlerDB getNaTematCrawlerDB() {
        return naTematCrawlerDB;
    }

    public NaTematCrawlerMailNotification getNaTematCrawlerMailNotification() {
        return naTematCrawlerMailNotification;
    }

    @Override
    public void crawl(String url) {

        MailNotificationService mailNotificationService = naTematCrawlerMailNotification.getMailNotificationService();

        mailNotificationService.sendMailNotification("NaTematCrawler started", "Crawler started at: " + new Date());

        try {

            long startTime = System.currentTimeMillis();

            Integer allArticlesCrawled = 0;
            Integer allCommentsCrawled = 0;
            Integer allSubCommentsCrawled = 0;

            List<String> sectionNames = crawlerService.getAllSectionsList();

            Set<Section> sections = new LinkedHashSet<Section>();

            for (String sectionName : sectionNames) {
                sections.add(new Section(sectionName));
            }

            naTematCrawlerDB.getSectionService().saveSections(sections);

            for (Section section : sections) {

                String sectionName = section.getSectionName();

                System.out.println("Section: " + sectionName);
                List<LinkMap> linksInSection = crawlerService.getLinksFromSection(sectionName);

                for (LinkMap linkInSection : linksInSection) {

                    System.out.println(linkInSection.getName());
                    Collection<LinkMap> allArticlesLinks = crawlerService.getLinksFromMonth(linkInSection.getLink());

                    for (LinkMap link : allArticlesLinks) {

                        String articleLink = link.getLink();

                        Document tmpDoc = Jsoup.connect(articleLink).timeout(TIMEOUT).get();
                        System.out.println("URL: " + articleLink);
                        System.out.println("Text length: " + tmpDoc.text().length());
                        System.out.println("Html length: " + tmpDoc.html().length());
                        System.out.println("Number of comments: " + crawlerService.getNumberOfCommentsForUrl(articleLink));
                        System.out.println("Number of links: " + crawlerService.findUniqueLinks(tmpDoc.select("a[href^=" + url + "], a[href^=/]")).size());

                        Article article = crawlerService.getArticleFromUrl(articleLink);
                        allArticlesCrawled += 1;

                        mailNotificationService.sendCrawlerStatisticMailAsync(startTime, allArticlesCrawled, allCommentsCrawled, allSubCommentsCrawled);

                        List<Comment> commentsList = crawlerService.getCommentsForUrl(articleLink);
                        allCommentsCrawled += commentsList.size();

                        mailNotificationService.sendCrawlerStatisticMailAsync(startTime, allArticlesCrawled, allCommentsCrawled, allSubCommentsCrawled);

                        if (article != null) {
                            naTematCrawlerDB.getArticleService().saveArticleForSection(section, article);
                            mailNotificationService.sendCrawlerStatisticMailAsync(startTime, allArticlesCrawled, allCommentsCrawled, allSubCommentsCrawled);
                            System.out.println("Number of facebook shares: " + article.getFacebookShares());
                        }

                        naTematCrawlerDB.getCommentService().saveComments(commentsList);
                        mailNotificationService.sendCrawlerStatisticMailAsync(startTime, allArticlesCrawled, allCommentsCrawled, allSubCommentsCrawled);

                        if (article != null && !commentsList.isEmpty())
                            naTematCrawlerDB.getArticleService().saveCommentsForArticle(article, commentsList);

                        for (Comment comment : commentsList) {
                            Set<Comment> subCommentsList = crawlerService.getSubCommentsForCommentId(comment.getId());
                            allSubCommentsCrawled += subCommentsList.size();
                            mailNotificationService.sendCrawlerStatisticMailAsync(startTime, allArticlesCrawled, allCommentsCrawled, allSubCommentsCrawled);
                            System.out.println("For commentId: " + comment.getId());
                            System.out.println("Number of subComments: " + subCommentsList.size());
                            naTematCrawlerDB.getCommentService().saveSubCommentsForComment(comment, subCommentsList);
                        }
                    }
                }
            }

            long endTime = System.currentTimeMillis() - startTime;

            mailNotificationService.sendMailNotification("NaTematCrawler finished", "Crawler stopped at: " + new Date() +
                    "Crawling summary: " +
                    "End time: " + new Date() + " (" + endTime + "[ms])" + "\n" +
                    "All articles crawled: " + allArticlesCrawled + "\n" +
                    "All comments crawled: " + allCommentsCrawled + "\n" +
                    "All subcomments crawled: " + allSubCommentsCrawled + "\n");

        } catch (Exception e) {
            mailNotificationService.sendMailNotification("NaTematCrawler error", "Crawler error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
