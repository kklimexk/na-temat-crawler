package main.java.pl.edu.agh.toik.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import main.java.pl.edu.agh.toik.app.NaTematCrawlerConfig;
import main.java.pl.edu.agh.toik.crawler.ICrawlerService;
import main.java.pl.edu.agh.toik.crawler.NaTematCrawler;
import main.java.pl.edu.agh.toik.crawler.NaTematCrawlerService;
import main.java.pl.edu.agh.toik.crawler.LinkMap;
import main.java.pl.edu.agh.toik.database.NaTematCrawlerDB;
import main.java.pl.edu.agh.toik.database.model.Article;
import main.java.pl.edu.agh.toik.database.model.Section;
import main.java.pl.edu.agh.toik.database.service.SectionService;
import main.java.pl.edu.agh.toik.mail_notification.NaTematCrawlerMailNotification;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Controller implements Initializable {

    private ICrawlerService crawler;
    //komponent bazodanowy
    private NaTematCrawlerDB naTematCrawlerDB;
    //komponent notyfikujacy
    private NaTematCrawlerMailNotification naTematCrawlerMailNotification;
    private SectionService sectionService;

    private ObservableList<String> sections;
    private ObservableList<String> months;
    private ObservableList<String> articles;
    private String section;
    private Article article;
    private List<LinkMap> monthsL;
    private Collection<LinkMap> articlesL;

    private ToggleGroup group;
    private RadioButton chk;
    @FXML
    private RadioButton downloadButton;
    @FXML
    private RadioButton readButton;

    @FXML
    private ComboBox<String> sectionCombo;

    @FXML
    private ComboBox<String> monthCombo;

    @FXML
    private  ComboBox<String> articleCombo;

    @FXML
    private TextArea dataArea;

    @FXML
    private TextArea textArea;

    @FXML
    private BarChart<String, Integer> histogram;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ApplicationContext context = new AnnotationConfigApplicationContext(NaTematCrawlerConfig.class);
        NaTematCrawler naTematCrawler = context.getBean(NaTematCrawler.class);
        crawler = naTematCrawler.getCrawlerService();
        naTematCrawlerDB = naTematCrawler.getNaTematCrawlerDB();
        naTematCrawlerMailNotification = naTematCrawler.getNaTematCrawlerMailNotification();
        sectionService = naTematCrawlerDB.getSectionService();

        this.group = new ToggleGroup();
        downloadButton.setToggleGroup(this.group);
        readButton.setToggleGroup(this.group);
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {
                chk = (RadioButton) t1.getToggleGroup().getSelectedToggle(); // Cast object to radio button
            }
        });
    }

    @FXML
    private void downloadAction(ActionEvent event){
        try {
            this.sections = FXCollections.observableArrayList(this.crawler.getAllSectionsList());
        }catch (IOException e){
            e.printStackTrace();
        }

        this.sectionCombo.setValue(null);
        this.monthCombo.getSelectionModel().clearSelection();
        this.monthCombo.setValue(null);
        this.articleCombo.getSelectionModel().clearSelection();
        this.articleCombo.setValue(null);
        this.dataArea.setText("");
        this.textArea.setText("");
        this.sectionCombo.setItems(this.sections);
        this.histogram.getData().clear();
    }

    @FXML
    private  void readAction(ActionEvent event){
        List<String> sects = new ArrayList<String>();
        Iterable<Section> sections1 = this.sectionService.findAllSections();
        for (Section item: sections1){
            sects.add(item.getSectionName());
        }

        this.sections = FXCollections.observableArrayList(sects);
        for (Section sect: this.sectionService.findAllSections()){
            this.sections.add(sect.getSectionName());
        }

        this.sectionCombo.setValue(null);
        this.monthCombo.getSelectionModel().clearSelection();
        this.monthCombo.setValue(null);
        this.articleCombo.getSelectionModel().clearSelection();
        this.articleCombo.setValue(null);
        this.dataArea.setText("");
        this.textArea.setText("");
        this.sectionCombo.setItems(this.sections);
        this.histogram.getData().clear();
    }

    @FXML
    private void getFromSection(ActionEvent event){
        if (chk.getText().equals("Pobierz artykuły")) {
            try {
                if (this.sectionCombo.getSelectionModel().getSelectedItem() != null) {
                    this.section = this.sectionCombo.getSelectionModel().getSelectedItem().toString();
                    this.monthsL = this.crawler.getLinksFromSection(this.section);
                    this.months = FXCollections.observableArrayList(this.crawler.getNames(this.monthsL));
                }else{
                    this.sectionCombo.getSelectionModel().clearSelection();
                    this.sectionCombo.setValue(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.monthCombo.setItems(this.months);

        }else if (chk.getText().equals("Wczytaj artykuły")){
            if (this.sectionCombo.getSelectionModel().getSelectedItem() != null) {
                this.section = this.sectionCombo.getSelectionModel().getSelectedItem().toString();
                List<Article> articleList = naTematCrawlerDB.getArticleService().findArticlesBySectionName(this.section);
                List<LinkMap> artList = new ArrayList<LinkMap>();
                for (Article art : articleList){
                    LinkMap link = new LinkMap(art.getTitle(), art.getUrlId());
                    artList.add(link);
                }
                this.articleCombo.setItems(FXCollections.observableArrayList(this.crawler.getNames(artList)));
                this.articlesL = artList;
            }else {
                this.sectionCombo.getSelectionModel().clearSelection();
                this.sectionCombo.setValue(null);
            }
        }
    }

    @FXML
    private void getFromMonth(ActionEvent event){
        if (chk.getText().equals("Pobierz artykuły")) {
            try {
                if (this.monthCombo.getSelectionModel().getSelectedItem() != null) {
                    this.articlesL = this.crawler.getLinksFromMonth(this.crawler.getLinkFromName(this.monthCombo.getSelectionModel().getSelectedItem().toString(), this.monthsL));
                    this.articles = FXCollections.observableArrayList(this.crawler.getNames(this.articlesL));
                }else{
                    this.monthCombo.getSelectionModel().clearSelection();
                    this.monthCombo.setValue(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.articleCombo.setItems(this.articles);
        }
    }

    @FXML
    private  void getFromArticle(ActionEvent event){
        if (chk.getText().equals("Pobierz artykuły")) {
            try{
                this.article = this.crawler.getArticleFromUrl(this.crawler.getLinkFromName(this.articleCombo.getSelectionModel().getSelectedItem().toString(), this.articlesL));
            }catch (IOException e){
                e.printStackTrace();
            }
        }else if (chk.getText().equals("Wczytaj artykuły")){
            this.article = this.naTematCrawlerDB.getArticleService().findByUrlId(this.crawler.getLinkFromName(this.articleCombo.getSelectionModel().getSelectedItem().toString(), this.articlesL));
        }

        String data = "";
        data += "Tytuł: ";
        data += this.article.getTitle() + "\n";
        data += "Autor: " + this.article.getAuthor() + "\n";
        data += "Url: " + this.article.getUrlId() + "\n";
        data += "Date: " + this.article.getCreatedDate().getYear() + ":"
                + this.article.getCreatedDate().getMonthOfYear() + ":"
                + this.article.getCreatedDate().getDayOfMonth() + "\n";
        data += "Number of Facebook shares: " + this.article.getFacebookShares() + "\n";
        this.dataArea.setText(data);

        String text = this.article.getText();
        StringBuilder sb = new StringBuilder(text);
        int i = 0;
        while ((i = sb.indexOf(" ", i + 120)) != -1) {
            sb.replace(i, i + 1, "\n");
        }
        this.textArea.setText(sb.toString());
    }

    @FXML
    private void chartAction(ActionEvent event){
        if (chk.getText().equals("Pobierz artykuły")) {
            try {
                if (this.sectionCombo.getSelectionModel().getSelectedItem() != null) {
                    List<Integer> linksInMonth = new ArrayList<Integer>();
                    for(LinkMap month : this.monthsL){
                        linksInMonth.add(this.crawler.getLinksFromMonth(month.getLink()).size());
                    }
                    XYChart.Series<String, Integer> series = new XYChart.Series<>();
                    for (int i = 0; i < linksInMonth.size(); i++){
                        series.getData().add(new XYChart.Data<>(this.crawler.getNames(monthsL).get(i), linksInMonth.get(i)));
                    }
                    this.histogram.getData().add(series);
                }else{
                    this.histogram.getData().clear();
                    List<Integer> linksInSection = new ArrayList<Integer>();
                    for(String sect : this.sections) {
                        linksInSection.add(this.crawler.getLinksFromSection(sect).size());
                    }
                    XYChart.Series<String, Integer> series = new XYChart.Series<>();
                    for (int i = 0; i < linksInSection.size(); i++){
                        series.getData().add(new XYChart.Data<>(this.sections.get(i), linksInSection.get(i)));
                    }
                    this.histogram.getData().add(series);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (chk.getText().equals("Wczytaj artykuły")) {
            this.histogram.getData().clear();
            List<Integer> linksInSection = new ArrayList<Integer>();
            for(String sect : this.sections) {
                linksInSection.add(this.naTematCrawlerDB.getArticleService().findArticlesBySectionName(sect).size());
            }
            XYChart.Series<String, Integer> series = new XYChart.Series<>();
            for (int i = 0; i < linksInSection.size(); i++){
                series.getData().add(new XYChart.Data<>(this.sections.get(i), linksInSection.get(i)));
            }
            this.histogram.getData().add(series);
        }
    }

    @FXML
    private void actualizeAction(ActionEvent event){
        try {
            List<String> list = this.crawler.getAllSectionsList();
            Iterable<Section> sects = new ArrayList<Section>();
            for(String l: list){
                Section sect = new Section(l);
                this.sectionService.saveSection(sect);
                this.monthsL = this.crawler.getLinksFromSection(l);
                for (String link : this.crawler.getNames(this.monthsL)) {
                    this.articlesL = this.crawler.getLinksFromMonth(this.crawler.getLinkFromName(link, this.monthsL));
                    for (String n: this.crawler.getNames(this.articlesL)){
                        Article art = this.crawler.getArticleFromUrl(this.crawler.getLinkFromName(n, this.articlesL));
                        art.setSection(sect);
                        this.naTematCrawlerDB.getArticleService().saveArticle(art);
                    }
                }
                System.out.println("section");
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
