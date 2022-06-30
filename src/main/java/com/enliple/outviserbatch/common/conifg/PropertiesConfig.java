package com.enliple.outviserbatch.common.conifg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

/**
 * 프로퍼티 파일내 키에 대한 값을 가져오기 위한 클래스
 * <br> - 빈에 등록되어 있지 않는 클래스(ex. 유틸성)에서 프로퍼티를 사용하기 위함
 * <br> - com.enliple.outviserbatch.common.util.PropertiesUtils 를 통해 사용
 * 
 * @author jbnoh
 */
@Configuration
public class PropertiesConfig {

	@Autowired
	private Environment environment;

	@Bean
	public PropertiesFactoryBean config() throws Exception {

		PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		ClassPathResource classPathResource = new ClassPathResource("application.properties");

		propertiesFactoryBean.setLocation(classPathResource);
		return propertiesFactoryBean;
	}

	public String getValue(String key) {

		return environment.getProperty(key, "");
	}
}
