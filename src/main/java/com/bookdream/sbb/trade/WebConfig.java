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
		// 주입받은 문자열 경로를 Path 객체로 변환합니다.
		Path path = Paths.get(uploadDir);

		// Path 객체를 운영체제에 맞는 올바른 URI 형식(file:///...)으로 변환합니다.
		String resourcePath = path.toUri().toString();

		// 웹 브라우저가 /static/image/.. 로 요청하면,
		// 위에서 변환된 실제 디스크 경로에서 파일을 찾아 제공하도록 설정합니다.
		registry.addResourceHandler("/static/image/**")
				.addResourceLocations(resourcePath);
	}
}
