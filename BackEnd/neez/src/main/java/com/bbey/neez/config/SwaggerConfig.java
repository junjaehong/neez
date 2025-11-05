package com.bbey.neez.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;
import springfox.documentation.spring.web.plugins.WebFluxRequestHandlerProvider;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.bbey.neez"))
                .paths(PathSelectors.any())
                .build();
    }

    // Springfox 3.0.0 + Spring Boot 2.6+ NPE 패치
    @Bean
    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider || bean instanceof WebFluxRequestHandlerProvider) {
                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
                }
                return bean;
            }

            @SuppressWarnings("unchecked")
            private List<Object> getHandlerMappings(Object bean) {
                try {
                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                    field.setAccessible(true);
                    return (List<Object>) field.get(bean);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            private void customizeSpringfoxHandlerMappings(List<Object> mappings) {
                List<Object> copy = mappings.stream()
                        .filter(mapping -> {
                            try {
                                // handlerMapping.getPatternParser() == null 인 것만 남김
                                Field field = ReflectionUtils.findField(mapping.getClass(), "patternParser");
                                if (field == null) {
                                    return true;
                                }
                                field.setAccessible(true);
                                Object patternParser = field.get(mapping);
                                return patternParser == null;
                            } catch (IllegalAccessException e) {
                                return true;
                            }
                        })
                        .collect(Collectors.toList());

                mappings.clear();
                mappings.addAll(copy);
            }
        };
    }
}
