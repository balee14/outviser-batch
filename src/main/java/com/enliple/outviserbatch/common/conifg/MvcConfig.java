package com.enliple.outviserbatch.common.conifg;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.enliple.outviserbatch.common.bind.AnnotationDataMapArgumentResolver;
import com.enliple.outviserbatch.common.interceptor.JwtBearerAuthInterceptor;
import com.enliple.outviserbatch.common.interceptor.ServletInterceptor;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

	@Autowired
	private ServletInterceptor servletInterceptor;

	@Autowired
	private JwtBearerAuthInterceptor jwtInterceptor;

	@Autowired
	private AnnotationDataMapArgumentResolver dataMapArgumentResolver;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {

		// 정적 웹 경로는 제외하기 위함
		String[] staticPaths = { "/css/**", "/fonts/**", "/plugin/**", "/scripts/**", "/uploads/**", "/favicon.ico"};

		/*
		 * 로그
		 */
		registry.addInterceptor(servletInterceptor)
				// .addPathPatterns("/**");
				.excludePathPatterns(staticPaths)
				.excludePathPatterns("/api/cafe24/**");

		/*
		 * JWT
		 */
		registry.addInterceptor(jwtInterceptor)
				.excludePathPatterns(staticPaths)
				.excludePathPatterns("/api/send/**")
				.excludePathPatterns("/api/cafe24/**");
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(dataMapArgumentResolver);
	}

}
