package com.enliple.outviserbatch.common.service.errorLog.service;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.common.service.errorLog.mapper.CommonErrorLogMapper;
import com.enliple.outviserbatch.common.service.notification.service.Notification;
import com.enliple.outviserbatch.common.util.JsonUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("commonErrorLogService")
public class CommonErrorLogService {

	@Value("${spring.profiles.active}")
	private String activeServer;

	@Autowired
	private CommonErrorLogMapper commonErrLogMapper;

	@Autowired
	private Notification notify;

	/**
	 * 
	 * @param Throwable
	 */
	public void insertErrorLog(Throwable t) {

		String logMsg = t.getMessage();

		log.error(logMsg, t);

		DataMap errDataMap = new DataMap();
		errDataMap.put("errStacktrace", getPrintStackTrace(t));
		errDataMap.put("errMessage", logMsg);

		if (t instanceof CommonException) {
			String dataToString = "";

			CommonException ce = (CommonException) t;
			Object param = ce.getParameter();

			if (param instanceof DataMap) {
				dataToString = JsonUtils.toString((DataMap) param);
			} else {
				try {
					dataToString = param.toString();
				} catch (Exception e) {}
			}

			errDataMap.put("errParam", dataToString);
		}

		// 공통 에러 로그
		commonErrLogMapper.insertCmErrorLog(errDataMap);

		// 알림
		DataMap dataMap = new DataMap();
		String system = "LIVE".equalsIgnoreCase(activeServer) ? "" : activeServer;
		dataMap.put("subject", String.format("%s[아이센드 알림_FAILURE] %s", system, logMsg));
		dataMap.put("message", errDataMap.getString("errStacktrace"));
		notify.alert(false, dataMap);
	}

	private String getPrintStackTrace(Throwable e) {

		StringWriter sWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(sWriter));

		return sWriter.toString();
	}
}
