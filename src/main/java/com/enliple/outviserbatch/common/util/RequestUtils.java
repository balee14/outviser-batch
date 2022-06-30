package com.enliple.outviserbatch.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;

import com.enliple.outviserbatch.common.data.DataMap;

public class RequestUtils {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static DataMap getParamMap(HttpServletRequest request) {

		DataMap paramMap = new DataMap();
		Enumeration keys = request.getParameterNames();

		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String[] val = request.getParameterValues(key);

			if (val == null)
				paramMap.put(key, val);
			else if (val.length == 1)
				paramMap.put(key, val[0]);
			else
				paramMap.put(key, new ArrayList(Arrays.asList(val)));
		}

		// Request Body 인 경우 처리 (JSON 형태만)
		if (MediaType.APPLICATION_JSON_VALUE.equals(request.getContentType())) {
			try {
				String body = IOUtils.toString(request.getInputStream());
				paramMap.putJsonString(body);
			} catch (Exception e) {}
		}

		return paramMap;
	}
}