package com.bbey.neez.component;

import com.bbey.neez.DTO.NaverNewsItem;
import com.bbey.neez.DTO.NaverNewsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Component
public class NaverNewsClient {

    // 네이버 뉴스 검색 기본 URL
    private static final String SEARCH_URL = "https://openapi.naver.com/v1/search/news.json";

    private final RestTemplate restTemplate;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    public NaverNewsClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 키워드로 뉴스 검색
     * @param keyword 회사명 등
     * @param display 가져올 개수
     */
    public List<NaverNewsItem> searchNews(String keyword, int display) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(SEARCH_URL)
                .queryParam("query", keyword)
                .queryParam("display", display)
                .queryParam("sort", "date")
                .encode(StandardCharsets.UTF_8)   // 한글 인코딩
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<NaverNewsResponse> response =
                restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        entity,
                        NaverNewsResponse.class
                );

        if (response.getStatusCode().is2xxSuccessful()
                && response.getBody() != null
                && response.getBody().getItems() != null) {
            return response.getBody().getItems();
        }

        return Collections.emptyList();
    }

    // 기본 개수 5개짜리 오버로드
    public List<NaverNewsItem> searchNews(String keyword) {
        return searchNews(keyword, 5);
    }
}
