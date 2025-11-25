package com.bbey.neez;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NeezApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeezApplication.class, args);
	}

}
