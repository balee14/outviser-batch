package com.enliple.outviserbatch.schedule.point;

import org.springframework.stereotype.Component;

import com.enliple.outviserbatch.schedule.common.ScheduleTriggerImpl;

@Component("schedule_trigger_PointTrigger")
public class SchedulePointTrigger extends ScheduleTriggerImpl {

	@Override
	protected void init() {
		super.jobName = "pointScheduleJob";
		super.desc = "기간 만료 포인트  스케쥴러";

		// 매일 0시에 실행
		super.expression = "0 0 0 * * ?";
	}

}
