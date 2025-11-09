package com.bbey.neez.DTO;

/**
 * 네이버 뉴스 검색 API 의 각 item 구조
 * https://developers.naver.com/docs/serviceapi/search/news/news.md 참고
 */
public class NaverNewsItem {
    private String title;
    private String originallink;
    private String link;
    private String description;
    private String pubDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginallink() {
        return originallink;
    }

    public void setOriginallink(String originallink) {
        this.originallink = originallink;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }
}
