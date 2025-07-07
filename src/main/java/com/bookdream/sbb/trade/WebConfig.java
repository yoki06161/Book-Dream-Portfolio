package com.bookdream.sbb.trade;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final String uploadPath = "file:C:/Users/박재성/Desktop/bookdream_images/";

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/image/**")
				.addResourceLocations(uploadPath);
	}
}
