package main.java.pl.edu.agh.toik.crawler;

import main.java.pl.edu.agh.toik.database.model.*;
import main.java.pl.edu.agh.toik.util.JsonReader;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class NaTematCrawlerService implements ICrawlerService {

    private final static int TIMEOUT = 60 * 1000;

    @Autowired
    private Environment env;

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
    public List<Comment> getCommentsForUrl(String url) {

        List<Comment> comments = new ArrayList<Comment>();

        try {

            JSONObject json1 = JsonReader.readJsonFromUrl("https://graph.facebook.com/comments?id=" + url);
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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return comments;
    }

    @Override
    public Set<Comment> getSubCommentsForCommentId(String commentId) {

        Set<Comment> subComments = new LinkedHashSet<Comment>();

        try {

            JSONObject json = JsonReader.readJsonFromUrl("https://graph.facebook.com/" + commentId + "/comments" + "?access_token=" + env.getRequiredProperty("facebook.token"));
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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return subComments;
    }

    @Override
    public int getNumberOfCommentsForUrl(String url) {

        try {
            JSONObject json1 = JsonReader.readJsonFromUrl("https://graph.facebook.com/comments?id=" + url);
            JSONArray data = json1.getJSONArray("data");
            return data.length();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public int getNumberOfFacebookSharesForArticle(String articleUrl) {
        JSONObject shareJson = null;

        try {
            JSONObject json = JsonReader.readJsonFromUrl("https://graph.facebook.com/" + articleUrl + "?access_token=" + env.getRequiredProperty("facebook.token"));
            shareJson = json.getJSONObject("share");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (shareJson != null && shareJson.has("share_count"))
            return shareJson.getInt("share_count");

        return 0;
    }

    @Override
    public Article getArticleFromUrl(String url) throws IOException {
        if (url.equals("http://natemat.pl/") ||
                url.matches("http://natemat.pl/c/.*") ||
                url.matches("http://natemat.pl/t/.*") ||
                url.matches("http://natemat.pl/info/.*") ||
                url.matches("http://natemat.pl/posts-map/.*"))
            return null;
        Document doc = Jsoup.connect(url).timeout(TIMEOUT).get();

        //removed unnecessary html element (figure-container)
        doc.select("div.figure-container").remove();

        String author = doc.select("div.author-label").first().text();
        String title = doc.select("div.article-title").first().text();
        String dateStr = doc.select("span.date").first().attr("title");
        String date = dateStr.split("T")[0];
        String time = dateStr.split("T")[1];
        DateTime artDate = new DateTime(Integer.parseInt(date.split("-")[0]), Integer.parseInt(date.split("-")[1]), Integer.parseInt(date.split("-")[2]), Integer.parseInt(time.split(":")[0]), Integer.parseInt(time.split(":")[1]));
        String articleContent = doc.select("div.article-body").first().text();
        String articleHtmlContent = doc.select("div.article-body").first().html();
        Integer numberOfFacebookShares = getNumberOfFacebookSharesForArticle(url);

        return new Article(url, author, title, artDate, articleContent, articleHtmlContent, numberOfFacebookShares);
    }

    @Override
    public Set<String> getAllBlogsLinks() throws IOException{
        String url = "http://natemat.pl/blogs/";
        Document doc = Jsoup.connect(url).timeout(TIMEOUT).get();

        Set<Element> links = this.findUniqueLinks(doc.select("a.blog-name"));
        Set<String> allLinks = new HashSet<String>();

        for (Element link : links) {
            String subUrl = link.attr("abs:href");
            Document tmpDoc = Jsoup.connect(subUrl).timeout(TIMEOUT).get();
            Set<Element> tmpLinks = this.findUniqueLinks(tmpDoc.select("h3 a[href]"));
            for(Element subLink : tmpLinks){
                String sub2Url = subLink.attr("abs:href");
                allLinks.add(sub2Url);
            }
        }
        return allLinks;
    }

    @Override
    public Set<String> getAllArticlesLinks() throws IOException{
        String url = "http://natemat.pl/posts-map/";
        Document doc = Jsoup.connect(url).timeout(TIMEOUT).get();

        Set<Element> links = this.findUniqueLinks(doc.select("div#main a[href]"));
        Set<String> allLinks = new HashSet<String>();

        for (Element link : links) {
            String subUrl = link.attr("abs:href");
            Document tmpDoc = Jsoup.connect(subUrl).timeout(TIMEOUT).get();
            Set<Element> tmpLinks = this.findUniqueLinks(tmpDoc.select("a.pg_page"));
            if(!tmpLinks.isEmpty()) {
                for (Element subLink : tmpLinks) {
                    String sub2Url = subLink.attr("abs:href");
                    Document tmpDoc2 = Jsoup.connect(sub2Url).timeout(TIMEOUT).get();
                    Set<Element> tmp2Links = this.findUniqueLinks(tmpDoc2.select("div#main ul a[href]"));
                    for (Element sub2Link : tmp2Links) {
                        String sub3Url = sub2Link.attr("abs:href");
                        allLinks.add(sub3Url);
                    }
                }
            }else{
                tmpLinks = this.findUniqueLinks(tmpDoc.select("div#main ul a[href]"));
                for (Element subLink : tmpLinks) {
                    String sub2Url = subLink.attr("abs:href");
                    allLinks.add(sub2Url);
                }
            }
        }
        return allLinks;
    }

    @Override
    public List<String> getAllSectionsList() throws IOException{
        String url = "http://natemat.pl/posts-map/";
        Document doc = Jsoup.connect(url).timeout(TIMEOUT).get();

        Elements links = doc.select("div#main h2");
        List<String> sections = new ArrayList<String>();

        for(Element element : links){
            sections.add(element.text());
        }
        return sections;
    }

    @Override
    public List<LinkMap> getLinksFromSection(String section) throws IOException{
        String url = "http://natemat.pl/posts-map/";
        Document doc = Jsoup.connect(url).timeout(TIMEOUT).get();

        Element element = doc.select("div#main").first();
        List<LinkMap> links = new ArrayList<LinkMap>();

        boolean save = false;
        for (Element child : element.children()){
            if (child.tag().toString().equals("h2")){
                if(child.text().equals(section)){
                    save = true;
                }else if (save) break;
            }else if (child.tag().toString().equals("a") && save) {
                LinkMap elem = new LinkMap(child.text(), child.attr("abs:href"));
                links.add(elem);
            }
        }

        return links;
    }

    @Override
    public Collection<LinkMap> getLinksFromMonth(String url) throws IOException{
        Document doc = Jsoup.connect(url).timeout(TIMEOUT).get();

        Set<LinkMap> allLinks = new LinkedHashSet<LinkMap>();

        Set<Element> links = this.findUniqueLinks(doc.select("div#main a.pg_page"));
        if(!links.isEmpty()) {
            for (Element subLink : links) {
                String subUrl = subLink.attr("abs:href");
                Document tmpDoc = Jsoup.connect(subUrl).timeout(TIMEOUT).get();
                Set<Element> tmp2Links = this.findUniqueLinks(tmpDoc.select("div#main ul a[href]"));
                for (Element sub2Link : tmp2Links) {
                    LinkMap sub2Url = new LinkMap(sub2Link.text(), sub2Link.attr("abs:href"));
                    allLinks.add(sub2Url);
                }
            }
        }else{
            links = this.findUniqueLinks(doc.select("div#main ul a[href]"));
            for (Element subLink : links) {
                LinkMap subUrl = new LinkMap(subLink.text(), subLink.attr("abs:href"));
                allLinks.add(subUrl);
            }
        }

        return allLinks;
    }

    @Override
    public String getLinkFromName(String name, Collection<LinkMap> list){
        for (LinkMap element : list){
            if (element.getName().equals(name))
                return element.getLink();
        }
        return null;
    }

    @Override
    public List<String> getNames(Collection<LinkMap> list){
        List<String> stringList = new ArrayList<String>();

        for (LinkMap element : list){
            stringList.add(element.getName());
        }
        return stringList;
    }
}
