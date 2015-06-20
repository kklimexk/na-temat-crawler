package main.java.pl.edu.agh.toik.crawler;

public class LinkMap {

    private String name;
    private String link;

    public LinkMap(String name, String link) {
        this.name = name;
        this.link = link;
    }

    public String getName() {
        return this.name;
    }

    public String getLink() {
        return this.link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkMap linkMap = (LinkMap) o;

        if (name != null ? !name.equals(linkMap.name) : linkMap.name != null) return false;
        return !(link != null ? !link.equals(linkMap.link) : linkMap.link != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (link != null ? link.hashCode() : 0);
        return result;
    }
}
