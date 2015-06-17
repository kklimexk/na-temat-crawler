package main.java.pl.edu.agh.toik.crawler;

import main.java.pl.edu.agh.toik.database.NaTematCrawlerDB;
import main.java.pl.edu.agh.toik.database.model.Article;
import main.java.pl.edu.agh.toik.database.model.Comment;
import main.java.pl.edu.agh.toik.mail_notification.NaTematCrawlerMailNotification;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;

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

        //naTematCrawlerMailNotification.getMailNotificationService().sendMailNotification("YOUR_EMAIL", "NaTematCrawler started", "Crawler started at: " + new Date());

        try {

            Set<String> allArticlesLinks = crawlerService.getAllArticlesLinks();

            for (String articleLink : allArticlesLinks) {
                Document tmpDoc = Jsoup.connect(articleLink).timeout(TIMEOUT).get();
                System.out.println("URL: " + articleLink);
                System.out.println("Text length: " + tmpDoc.text().length());
                System.out.println("Html length: " + tmpDoc.html().length());
                System.out.println("Number of comments: " + crawlerService.getNumberOfCommentsForUrl(articleLink));
                System.out.println("Number of links: " + crawlerService.findUniqueLinks(tmpDoc.select("a[href^=" + url + "], a[href^=/]")).size());

                Article article = crawlerService.getArticleFromUrl(articleLink);
                List<Comment> commentsList = crawlerService.getCommentsForUrl(articleLink);

                if (article != null) {
                    naTematCrawlerDB.getArticleService().saveArticle(article);
                    System.out.println("Number of facebook shares: " + article.getFacebookShares());
                }

                naTematCrawlerDB.getCommentService().saveComments(commentsList);

                if (article != null && !commentsList.isEmpty())
                    naTematCrawlerDB.getArticleService().saveCommentsForArticle(article, commentsList);

                for (Comment comment : commentsList) {
                    Set<Comment> subCommentsList = crawlerService.getSubCommentsForCommentId(comment.getId());
                    System.out.println("For commentId: " + comment.getId());
                    System.out.println("Number of subComments: " + subCommentsList.size());
                    naTematCrawlerDB.getCommentService().saveSubCommentsForComment(comment, subCommentsList);
                }
            }
            //naTematCrawlerMailNotification.getMailNotificationService().sendMailNotification("YOUR_EMAIL", "NaTematCrawler finished", "Crawler stopped at: " + new Date());
        } catch (Exception e) {
            //naTematCrawlerMailNotification.getMailNotificationService().sendMailNotification("YOUR_EMAIL", "NaTematCrawler error", "Crawler error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
