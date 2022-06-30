package com.enliple.outviserbatch.common.util;

import org.springframework.context.ApplicationContext;

import com.enliple.outviserbatch.common.context.ApplicationContextProvider;

public class BeanUtils {

	public static Object getBean(String bean) {

		ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
		return ctx.getBean(bean);
	}
}
