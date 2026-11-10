package org.springframework.samples.petclinic.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class WebMvcConfig implements WebMvcConfigurer {

	private final RequestLoggingInterceptor requestLoggingInterceptor;

	WebMvcConfig(RequestLoggingInterceptor requestLoggingInterceptor) {
		this.requestLoggingInterceptor = requestLoggingInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(this.requestLoggingInterceptor).addPathPatterns("/api/**");
	}

}
