package com.bbey.neez.service.Company;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DartFinancialClient {

    @Value("${dart.api-key}")
    private String dartApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * DART 단일회사 전체 재무제표
     * @param corpCode 회사코드
     * @param year     사업연도 (YYYY)
     * @param reprtCode 보고서 코드 (11011=사업)
     */
    public JsonNode getFinancialStatements(String corpCode, String year, String reprtCode) throws Exception {
        String url = "https://opendart.fss.or.kr/api/fnlttSinglAcntAll.json"
                + "?crtfc_key=" + dartApiKey
                + "&corp_code=" + corpCode
                + "&bsns_year=" + year
                + "&reprt_code=" + reprtCode;

        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        return objectMapper.readTree(resp.getBody());
    }
}
