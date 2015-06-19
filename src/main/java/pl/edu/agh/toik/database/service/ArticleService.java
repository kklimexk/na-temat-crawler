package main.java.pl.edu.agh.toik.database.service;

import main.java.pl.edu.agh.toik.database.model.*;
import main.java.pl.edu.agh.toik.database.repository.ArticleRepository;
import main.java.pl.edu.agh.toik.database.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private CommentRepository commentRepository;

    public Iterable<Article> findAllArticles() {
        return articleRepository.findAll();
    }

    public Article findByUrlId(String urlId) {
        return articleRepository.findOne(urlId);
    }

    public void saveArticle(Article article) {
        articleRepository.save(article);
    }

    public void saveArticles(Iterable<Article> articles) {
        articleRepository.save(articles);
    }

    @Transactional
    public void saveCommentsForArticle(Article article, List<Comment> comments) {
        for (Comment comment : comments) {
            comment.setArticle(article);
            commentRepository.save(comment);
        }
    }

    @Transactional
    public void saveArticleForSection(Section section, Article article) {
        article.setSection(section);
        articleRepository.save(article);
    }

    @Transactional
    public void saveArticlesForSection(Section section, Iterable<Article> articles) {
        for (Article article : articles) {
            article.setSection(section);
            articleRepository.save(article);
        }
    }

    public List<Article> findArticlesBySection(Section section) {
        return articleRepository.findBySection(section);
    }

    public List<Article> findArticlesBySectionName(String sectionName) {
        return articleRepository.findBySectionName(sectionName);
    }

    /**
     *
     * @param date for example: '2015-05-20' or '2015-05' or '2015'
     */
    public List<Article> findArticlesByCreatedDate(String date) {
        return articleRepository.findByCreatedDate(date);
    }

    /**
     *
     * @param date for example: '2015-05-20' or '2015-05' or '2015'
     */
    public List<Article> findArticlesBySectionNameAndCreatedDate(String sectionName, String date) {
        return articleRepository.findBySectionNameAndCreatedDate(sectionName, date);
    }

    /**
     *
     * @param date1 for example: '2015-05-20' or '2015-05' or '2015'
     * @param date2 for example: '2015-05-20' or '2015-05' or '2015'
     */
    public List<Article> findArticlesBetweenTwoCreatedDates(String date1, String date2) {
        return articleRepository.findBetweenTwoCreatedDates(date1, date2);
    }

    /**
     *
     * @param date1 for example: '2015-05-20' or '2015-05' or '2015'
     * @param date2 for example: '2015-05-20' or '2015-05' or '2015'
     */
    public List<Article> findArticlesBySectionNameAndBetweenTwoCreatedDates(String sectionName, String date1, String date2) {
        return articleRepository.findBySectionNameAndBetweenTwoCreatedDates(sectionName, date1, date2);
    }

    public List<Article> findArticlesInFacebookSharePeriod(Integer from, Integer to) {
        return articleRepository.findInFacebookSharePeriod(from, to);
    }

}
