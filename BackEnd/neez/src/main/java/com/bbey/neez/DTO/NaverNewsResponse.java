package com.bbey.neez.DTO;

import java.util.List;

/**
 * 네이버 뉴스 검색 API 응답 전체를 받기 위한 DTO
 * 필요한 필드만 넣었음
 */
public class NaverNewsResponse {
    private String lastBuildDate;
    private int total;
    private int start;
    private int display;
    private List<NaverNewsItem> items;

    public String getLastBuildDate() {
        return lastBuildDate;
    }

    public void setLastBuildDate(String lastBuildDate) {
        this.lastBuildDate = lastBuildDate;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getDisplay() {
        return display;
    }

    public void setDisplay(int display) {
        this.display = display;
    }

    public List<NaverNewsItem> getItems() {
        return items;
    }

    public void setItems(List<NaverNewsItem> items) {
        this.items = items;
    }
}
