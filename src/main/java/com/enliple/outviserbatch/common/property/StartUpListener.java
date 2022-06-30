package com.enliple.outviserbatch.common.property;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.enliple.outviserbatch.common.action.service.StartUpService;
import com.enliple.outviserbatch.common.statics.StaticCampCondScore;
import com.enliple.outviserbatch.common.statics.StaticCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StartUpListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent arg0) {

		try {
			ServletContext context = arg0.getServletContext();
			WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(context);

			StartUpService startUpService = wac.getBean(StartUpService.class);

			// 코드
			StaticCode.initCode(startUpService.selectCodeList());
			StaticCampCondScore.init(startUpService.selectCampCondScoreList());

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void contextDestroyed(ServletContextEvent arg0) {}
}