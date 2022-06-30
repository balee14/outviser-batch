package com.enliple.outviserbatch.common.statics;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

import com.enliple.outviserbatch.common.action.service.StartUpService;
import com.enliple.outviserbatch.common.data.DataMap;

public class StaticCampCondScore {

	public static DataMap campCondScoreList = new DataMap();
	
	public static void init() throws Exception {
		ApplicationContext appContext = ContextLoader.getCurrentWebApplicationContext();
		StartUpService startUpService = (StartUpService)appContext.getBean(StartUpService.class);
		
		init(startUpService.selectCampCondScoreList());
	}

	public static void init(DataMap selectCampCondScoreList) throws Exception {
		campCondScoreList = selectCampCondScoreList;
	}
	
	public static DataMap getDataMap() {
		return campCondScoreList;
	}
}