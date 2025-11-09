package com.bbey.neez.component;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class NaverNewsClient {

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Map<String, Object>> searchNews(String query, int display) {
        URI uri = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com/v1/search/news.json")
                .queryParam("query", query)
                .queryParam("display", display)
                .queryParam("sort", "date")
                .build(true)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> resp = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
        Map body = resp.getBody();
        if (body == null) return Arrays.asList();
        return (List<Map<String, Object>>) body.get("items");
    }
}
