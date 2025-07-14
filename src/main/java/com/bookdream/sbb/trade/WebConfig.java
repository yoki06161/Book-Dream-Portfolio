package com.bookdream.sbb.trade;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${file.upload-dir}") // 이 어노테이션 추가
	private String uploadDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// /static/image/ 로 시작하는 모든 요청을
		// 실제 파일 시스템의 uploadDir 경로에서 찾도록 매핑합니다.
		registry.addResourceHandler("/static/image/**")
				.addResourceLocations("file:" + uploadDir);
	}
}
