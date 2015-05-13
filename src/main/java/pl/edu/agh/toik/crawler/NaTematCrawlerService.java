package main.java.pl.edu.agh.toik.crawler;

import main.java.pl.edu.agh.toik.database.model.*;
import main.java.pl.edu.agh.toik.util.JsonReader;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;

public class NaTematCrawlerService implements ICrawlerService {

    private final static int TIMEOUT = 10 * 1000;

    @Override
    public Set<Element> findUniqueLinks(Collection<Element> links) {

        Set<String> uniqueNameLinks = new HashSet<String>();
        Set<Element> uniqueLinks = new LinkedHashSet<Element>();

        for (Element link : links) {
            uniqueNameLinks.add(link.attr("href"));
            if (uniqueNameLinks.size() > uniqueLinks.size())
                uniqueLinks.add(link);
        }

        return uniqueLinks;
    }

    @Override
    public List<Comment> getCommentsForUrl(String url) throws IOException {

        List<Comment> comments = new ArrayList<Comment>();

        JSONObject json1 = JsonReader.readJsonFromUrl("http://graph.facebook.com/comments?id=" + url);
        JSONArray data = json1.getJSONArray("data");

        for (int i = 0; i < data.length(); ++i) {
            String commentId = data.getJSONObject(i).getString("id");
            String commentAuthor = data.getJSONObject(i).getJSONObject("from").getString("name");
            DateTime commentCreatedDate = DateTime.parse(data.getJSONObject(i).getString("created_time"));
            Integer commentLikeCounter = data.getJSONObject(i).getInt("like_count");
            String commentContent = data.getJSONObject(i).getString("message");

            Comment comment = new Comment(commentId, commentAuthor, commentCreatedDate, commentLikeCounter, commentContent);
            comments.add(comment);
        }

        return comments;
    }

    @Override
    public Set<Comment> getSubCommentsForCommentId(String commentId) throws IOException {

        Set<Comment> subComments = new LinkedHashSet<Comment>();

        JSONObject json = JsonReader.readJsonFromUrl("http://graph.facebook.com/" + commentId + "/comments");
        JSONArray data = json.getJSONArray("data");

        for (int i = 0; i < data.length(); ++i) {
            String subCommentId = data.getJSONObject(i).getString("id");
            String subCommentAuthor = data.getJSONObject(i).getJSONObject("from").getString("name");
            DateTime subCommentCreatedDate = DateTime.parse(data.getJSONObject(i).getString("created_time"));
            Integer subCommentLikeCounter = data.getJSONObject(i).getInt("like_count");
            String subCommentContent = data.getJSONObject(i).getString("message");

            Comment subComment = new Comment(subCommentId, subCommentAuthor, subCommentCreatedDate, subCommentLikeCounter, subCommentContent);
            subComments.add(subComment);
        }

        return subComments;
    }

    @Override
    public int getNumberOfCommentsForUrl(String url) throws IOException {
        JSONObject json1 = JsonReader.readJsonFromUrl("http://graph.facebook.com/comments?id=" + url);
        JSONArray data = json1.getJSONArray("data");
        return data.length();
    }

    @Override
    public Article getArticleFromUrl(String url) throws IOException{
        Document doc = Jsoup.connect(url).timeout(TIMEOUT).get();
        String author = doc.select("div.author-label").first().text();
        String dateStr = doc.select("span.date").first().attr("title");
        String date = dateStr.split("T")[0];
        String time = dateStr.split("T")[1];
        DateTime artDate = new DateTime(Integer.parseInt(date.split("-")[0]), Integer.parseInt(date.split("-")[1]), Integer.parseInt(date.split("-")[2]), Integer.parseInt(time.split(":")[0]), Integer.parseInt(time.split(":")[1]));
        return new Article(url, author, doc.title(), artDate, doc.text());
    }

}
