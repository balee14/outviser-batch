package com.enliple.outviserbatch.common.util;

import com.enliple.outviserbatch.common.conifg.PropertiesConfig;

public class PropertiesUtils {

	private final static String beanName = "propertiesConfig";

	public static String getValue(String key) {
		PropertiesConfig config = (PropertiesConfig) BeanUtils.getBean(beanName);
		return config.getValue(key);
	}

	public static int getInt(String key) {
		int value = 0;

		try {
			value = Integer.parseInt(getValue(key));
		} catch (Exception e) {}

		return value;
	}
}
