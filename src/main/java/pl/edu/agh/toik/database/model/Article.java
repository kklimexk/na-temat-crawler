package main.java.pl.edu.agh.toik.database.model;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "articles")
public class Article {

    @Id
    private String urlId;
    private String author;
    private String title;
    @Column
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdDate;
    @Column(columnDefinition = "TEXT")
    private String text;
    @Column(columnDefinition = "TEXT")
    private String htmlText;
    private Integer facebookShares;

    @OneToMany(mappedBy = "article")
    private Set<Comment> comments;
    @ManyToOne
    @JoinColumn(name = "sectionId")
    private Section section;

    public Article() {
    }

    public Article(String urlId, String author, String title, DateTime createdDate, String text, String htmlText, Integer facebookShares) {
        this.urlId = urlId;
        this.author = author;
        this.title = title;
        this.createdDate = createdDate;
        this.text = text;
        this.htmlText = htmlText;
        this.facebookShares = facebookShares;
    }

    public String getUrlId() {
        return urlId;
    }

    public void setUrlId(String urlId) {
        this.urlId = urlId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHtmlText() {
        return htmlText;
    }

    public void setHtmlText(String htmlText) {
        this.htmlText = htmlText;
    }

    public Integer getFacebookShares() {
        return facebookShares;
    }

    public void setFacebookShares(Integer facebookShares) {
        this.facebookShares = facebookShares;
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }
}
