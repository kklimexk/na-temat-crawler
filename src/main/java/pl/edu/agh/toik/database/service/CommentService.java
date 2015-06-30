package main.java.pl.edu.agh.toik.database.service;

import main.java.pl.edu.agh.toik.database.model.Article;
import main.java.pl.edu.agh.toik.database.model.Comment;
import main.java.pl.edu.agh.toik.database.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public Iterable<Comment> findAllComments() {
        return commentRepository.findAll();
    }

    public Comment findById(String id) {
        return commentRepository.findOne(id);
    }

    public void saveComment(Comment comment) {
        commentRepository.save(comment);
    }

    public void saveComments(Iterable<Comment> comments) {
        commentRepository.save(comments);
    }

    @Transactional
    public void saveSubCommentsForComment(Article article, Comment comment, Set<Comment> subComments) {
        for (Comment subComment : subComments) {
            subComment.setArticle(article);
            subComment.setComment(comment);
            commentRepository.save(subComment);
        }
    }

    /**
     *
     * @param date for example: '2015-05-20' or '2015-05' or '2015'
     */
    public List<Comment> findCommentsByCreatedDate(String date) {
        return commentRepository.findByCreatedDate(date);
    }

    /**
     *
     * @param date1 for example: '2015-05-20' or '2015-05' or '2015'
     * @param date2 for example: '2015-05-20' or '2015-05' or '2015'
     */
    public List<Comment> findArticlesBetweenTwoCreatedDates(String date1, String date2) {
        return commentRepository.findBetweenTwoCreatedDates(date1, date2);
    }

    public List<Comment> findCommentsForArticle(Article article) {
        return commentRepository.findByArticle(article);
    }

    public List<Comment> findCommentsForArticleUrl(String url) {
        return commentRepository.findByArticleUrl(url);
    }

    public List<Comment> findCommentsInLikeCounterPeriod(Integer from, Integer to) {
        return commentRepository.findInLikeCounterPeriod(from, to);
    }

    public List<Comment> findSubCommentsForComment(Comment comment) {
        return commentRepository.findSubCommentsByComment(comment);
    }

}
