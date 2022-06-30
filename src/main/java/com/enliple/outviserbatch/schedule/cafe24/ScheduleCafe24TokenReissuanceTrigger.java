package com.enliple.outviserbatch.schedule.cafe24;

import com.enliple.outviserbatch.schedule.common.ScheduleTriggerImpl;
import org.springframework.stereotype.Component;

@Component("schedule_trigger_cafe24TokenReissuanceTrigger")
public class ScheduleCafe24TokenReissuanceTrigger extends ScheduleTriggerImpl {

	@Override
	protected void init() {
		super.jobName = "cafe24TokenReissuanceScheduleJob";
		super.desc = "CAFE24 토큰 재발급 스케줄러";

		// 매일 새벽 2시에 실행
		super.expression = "0 0 2 * * ?";
	}

}
