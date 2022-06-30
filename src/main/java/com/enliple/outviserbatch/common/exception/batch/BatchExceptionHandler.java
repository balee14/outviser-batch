package com.enliple.outviserbatch.common.exception.batch;

import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enliple.outviserbatch.common.service.errorLog.service.CommonErrorLogService;

@Component
public class BatchExceptionHandler implements ExceptionHandler {

	@Autowired
	private CommonErrorLogService commonErrLogService;

	@Override
	public void handleException(RepeatContext context, Throwable throwable) throws Throwable {

		commonErrLogService.insertErrorLog(throwable);

		// JOB 실행 중지
		context.setTerminateOnly();
	}
}
