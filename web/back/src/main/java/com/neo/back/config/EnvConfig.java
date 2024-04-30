package com.neo.back.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@PropertySources({
	@PropertySource("classpath:env.properties") // env.properties 파일 소스 등록
})
public class EnvConfig implements WebMvcConfigurer {
}