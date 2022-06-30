package com.enliple.outviserbatch.common.conifg;

import javax.servlet.ServletContextListener;

import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.enliple.outviserbatch.common.property.StartUpListener;

@Configuration
public class ServletConfig {

	@Bean
	public ServletListenerRegistrationBean<ServletContextListener> servletListener() {

		ServletListenerRegistrationBean<ServletContextListener> servletListenerRegiBean = new ServletListenerRegistrationBean<>();
		servletListenerRegiBean.setListener(new StartUpListener());
		return servletListenerRegiBean;
	}
}
