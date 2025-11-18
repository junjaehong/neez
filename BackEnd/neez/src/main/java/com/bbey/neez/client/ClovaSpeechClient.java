package com.bbey.neez.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ClovaSpeechClient {

    private final WebClient webClient;
    private final String language;
    private final String completion;

    public ClovaSpeechClient(
            WebClient.Builder builder,
            @Value("${naver.clova.speech.invoke-url}") String invokeUrl,
            @Value("${naver.clova.speech.secret}") String secret,
            @Value("${naver.clova.speech.language:ko-KR}") String language,
            @Value("${naver.clova.speech.completion:sync}") String completion
    ) {
        if (!StringUtils.hasText(invokeUrl)) {
            throw new IllegalStateException("naver.clova.speech.invoke-url ??媛) ?ㅼ젙?섏뼱 ?덉? ?딆뒿?덈떎.");
        }
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("naver.clova.speech.secret ??媛) ?ㅼ젙?섏뼱 ?덉? ?딆뒿?덈떎.");
        }

        log.info("Clova invokeUrl={}", invokeUrl);
        log.info("Clova secret prefix={}", secret.substring(0, Math.min(6, secret.length())));

        this.webClient = builder
                // ?? https://clovaspeech-gw.ncloud.com/external/v1/{appId}/{invokeKey}
                .baseUrl(invokeUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json; charset=UTF-8")
                .defaultHeader("X-CLOVASPEECH-API-KEY", secret)
                .build();

        this.language = language;
        this.completion = completion;
    }

    /**
     * 업로드된 음성 byte[] 를 CLOVA Speech /recognizer/upload 로 전송하여 STT 수행.
     */
    public ClovaResult recognize(byte[] audioBytes) {
        return recognize(audioBytes, null);
    }

    /**
     * languageOverride 가 존재하면 해당 언어로 STT 를 수행한다.
     */
    public ClovaResult recognize(byte[] audioBytes, String languageOverride) {
        String effectiveLanguage = StringUtils.hasText(languageOverride) ? languageOverride : this.language;
        return recognizeInternal(audioBytes, effectiveLanguage);
    }

    private ClovaResult recognizeInternal(byte[] audioBytes, String effectiveLanguage) {
        if (audioBytes == null || audioBytes.length == 0) {
            throw new IllegalArgumentException("audioBytes 가 비어 있습니다.");
        }

        try {
            // 1) params JSON (공식 스펙에 맞게 최소 필수만 구성)
            String paramsJson = buildParamsJson(effectiveLanguage);

            // 2) multipart/form-data 구성
            MultiValueMap<String, Object> multipart = new LinkedMultiValueMap<>();

            // media 파트
            ByteArrayResource media = new ByteArrayResource(audioBytes) {
                @Override
                public String getFilename() {
                    return "audio.wav"; // 확장자만 맞춰주면 됨
                }
            };
            multipart.add("media", media);

            // params 파트: application/json 으로 보낸다
            HttpHeaders paramsHeaders = new HttpHeaders();
            paramsHeaders.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
            org.springframework.http.HttpEntity<String> paramsEntity =
                    new org.springframework.http.HttpEntity<>(paramsJson, paramsHeaders);
            multipart.add("params", paramsEntity);

            // 3) 호출
            ClovaApiResponse response = webClient.post()
                    .uri("/recognizer/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(multipart))
                    .retrieve()
                    .bodyToMono(ClovaApiResponse.class)
                    .block();

            if (response == null) {
                throw new IllegalStateException("Clova Speech 응답이 비어 있습니다.");
            }

            if (!"COMPLETED".equalsIgnoreCase(response.getResult())) {
                log.error("Clova Speech 비정상 응답: result={}, message={}",
                        response.getResult(), response.getMessage());
                throw new IllegalStateException("Clova Speech 비정상 응답: " +
                        response.getResult() + " - " + response.getMessage());
            }

            String textResult = Optional.ofNullable(response.getText()).orElse("");

            List<SpeakerSegment> segments = Optional.ofNullable(response.getSegments())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(seg -> new SpeakerSegment(
                            seg.getText(),
                            extractSpeakerLabel(seg),
                            seg.getStart(),
                            seg.getEnd()
                    ))
                    .collect(Collectors.toList());

            return new ClovaResult(textResult, segments);

        } catch (WebClientResponseException e) {
            String body = e.getResponseBodyAsString(StandardCharsets.UTF_8);
            log.error("Clova Speech HTTP {} Error Body: {}", e.getRawStatusCode(), body);
            throw new IllegalStateException(
                    "Clova Speech HTTP 오류: " + e.getRawStatusCode() + " " + e.getStatusText(),
                    e
            );
        } catch (Exception e) {
            log.error("Clova Speech 호출 중 예기치 못한 오류", e);
            throw new IllegalStateException("Clova Speech 호출 실패: " + e.getMessage(), e);
        }
    }

    private String buildParamsJson(String language) {
        return "{"
                + "\"language\":\"" + language + "\","
                + "\"completion\":\"" + completion + "\""
                + "}";
    }

    /**
     * seg ?덉뿉??speaker label???곷떦??戮묐뒗??
     * 紐?戮묒쑝硫?null.
     */
    private Integer extractSpeakerLabel(ClovaApiResponse.Segment seg) {
        try {
            if (seg.getSpeaker() != null && seg.getSpeaker().getLabel() != null) {
                return Integer.parseInt(seg.getSpeaker().getLabel());
            }
            if (seg.getDiarization() != null && seg.getDiarization().getLabel() != null) {
                return Integer.parseInt(seg.getDiarization().getLabel());
            }
        } catch (NumberFormatException ignore) {
        }
        return null;
    }

    // ==== ?몃????쒕퉬?ㅼ뿉????DTO ====

    public static class ClovaResult {
        private final String text;
        private final List<SpeakerSegment> segments;

        public ClovaResult(String text, List<SpeakerSegment> segments) {
            this.text = text;
            this.segments = segments;
        }

        public String getText() {
            return text;
        }

        public List<SpeakerSegment> getSegments() {
            return segments;
        }
    }

    public static class SpeakerSegment {
        private final String text;
        private final Integer speakerLabel;
        private final Long start;
        private final Long end;

        public SpeakerSegment(String text, Integer speakerLabel, Long start, Long end) {
            this.text = text;
            this.speakerLabel = speakerLabel;
            this.start = start;
            this.end = end;
        }

        public String getText() {
            return text;
        }

        public Integer getSpeakerLabel() {
            return speakerLabel;
        }

        public Long getStart() {
            return start;
        }

        public Long getEnd() {
            return end;
        }
    }

    // ==== CLOVA ?묐떟 留ㅽ븨 (?ㅼ젣 JSON ?ㅽ럺??留욎땄) ====

    /**
     * /recognizer/upload ?묐떟 ?덉떆 ?ㅽ럺??留욎텣 POJO
     *
     * {
     *   "result": "COMPLETED",
     *   "message": "Succeeded",
     *   "token": "...",
     *   "segments": [ { "start":0,"end":1110,"text":"...", ... } ],
     *   "text": "?꾩껜 ?띿뒪??,
     *   ...
     * }
     */
    public static class ClovaApiResponse {
        private String result;
        private String message;
        private String text;
        private List<Segment> segments;

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public List<Segment> getSegments() {
            return segments;
        }

        public void setSegments(List<Segment> segments) {
            this.segments = segments;
        }

        // ---- segments ----
        public static class Segment {
            private Long start;
            private Long end;
            private String text;
            private Diarization diarization;
            private Speaker speaker;

            public Long getStart() {
                return start;
            }

            public void setStart(Long start) {
                this.start = start;
            }

            public Long getEnd() {
                return end;
            }

            public void setEnd(Long end) {
                this.end = end;
            }

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }

            public Diarization getDiarization() {
                return diarization;
            }

            public void setDiarization(Diarization diarization) {
                this.diarization = diarization;
            }

            public Speaker getSpeaker() {
                return speaker;
            }

            public void setSpeaker(Speaker speaker) {
                this.speaker = speaker;
            }
        }

        public static class Diarization {
            private String label;

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }
        }

        public static class Speaker {
            private String label;
            private String name;

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }
}


