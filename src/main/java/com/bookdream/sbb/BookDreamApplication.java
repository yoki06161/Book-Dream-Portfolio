package com.bookdream.sbb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BookDreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookDreamApplication.class, args);
	}


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
