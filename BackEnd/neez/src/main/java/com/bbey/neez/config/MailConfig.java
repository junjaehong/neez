package com.bbey.neez.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        // 최소 설정 – 나중에 spring.mail.* 설정으로 교체
        return new JavaMailSenderImpl();
    }
}
