package main.java.pl.edu.agh.toik.gui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import main.java.pl.edu.agh.toik.app.NaTematCrawlerConfig;
import main.java.pl.edu.agh.toik.crawler.NaTematCrawler;
import main.java.pl.edu.agh.toik.database.NaTematCrawlerDB;
import main.java.pl.edu.agh.toik.database.model.Section;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ChartDBController implements Initializable {

    private NaTematCrawlerDB naTematCrawlerDB;

    private ObservableList<Section> sections;
    @FXML
    private BarChart<String, Integer> histogram;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ApplicationContext context = new AnnotationConfigApplicationContext(NaTematCrawlerConfig.class);
        NaTematCrawler naTematCrawler = context.getBean(NaTematCrawler.class);
        naTematCrawlerDB = naTematCrawler.getNaTematCrawlerDB();
        sections = FXCollections.observableArrayList((List<Section>) naTematCrawlerDB.getSectionService().findAllSections());
    }

    @FXML
    private void drawStatisticChart(ActionEvent event) throws IOException {
        histogram.getData().clear();
        List<Integer> linksInSection = new ArrayList<Integer>();
        for (Section sect : sections) {
            String sectionName = sect.getSectionName();
            linksInSection.add(naTematCrawlerDB.getArticleService().findArticlesBySectionName(sectionName).size());
        }
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        for (int i = 0; i < linksInSection.size(); i++) {
            series.getData().add(new XYChart.Data<>(sections.get(i).getSectionName(), linksInSection.get(i)));
        }
        histogram.getData().add(series);
    }
}
