package com.bbey.neez.config;

import com.bbey.neez.client.ClovaSpeechClient;
import com.bbey.neez.service.MeetingSpeechService;
import com.bbey.neez.service.MeetingSpeechServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MeetingServiceConfig {

  @Bean
  public MeetingSpeechService meetingSpeechService(ClovaSpeechClient clovaSpeechClient) {
    return new MeetingSpeechServiceImpl(clovaSpeechClient);
  }
}
