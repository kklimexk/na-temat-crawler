package main.java.pl.edu.agh.toik.database.model;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "sections")
public class Section {

    @Id
    private String sectionName;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "section", cascade = CascadeType.ALL)
    private Set<Article> articles;

    public Section() {
    }

    public Section(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public Set<Article> getArticles() {
        return articles;
    }

    public void setArticles(Set<Article> articles) {
        this.articles = articles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Section section = (Section) o;

        return !(sectionName != null ? !sectionName.equals(section.sectionName) : section.sectionName != null);

    }

    @Override
    public int hashCode() {
        return sectionName != null ? sectionName.hashCode() : 0;
    }

}
