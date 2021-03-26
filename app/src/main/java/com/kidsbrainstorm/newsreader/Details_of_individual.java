package com.kidsbrainstorm.newsreader;

public class Details_of_individual {
    private String url;
    private String title;

    @Override
    public String toString() {
        return title;
    }

    public Details_of_individual(String url, String title) {
        this.url = url;
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
