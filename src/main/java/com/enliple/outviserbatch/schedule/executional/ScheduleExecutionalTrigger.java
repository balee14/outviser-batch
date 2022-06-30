package com.enliple.outviserbatch.schedule.executional;

import org.springframework.stereotype.Component;

import com.enliple.outviserbatch.schedule.common.ScheduleTriggerImpl;

@Component("schedule_trigger_ExecutionalTrigger")
public class ScheduleExecutionalTrigger extends ScheduleTriggerImpl {

	@Override
	protected void init() {
		super.jobName = "executionalScheduleJob";
		super.desc = "캠페인 예약 집행";

		// 매일 8시 ~ 21시 사이 1분마다 실행
		super.expression = "0 0/1 8-21 * * ?";
	}

}
