package com.enliple.outviserbatch.schedule.template;

import org.springframework.stereotype.Component;

import com.enliple.outviserbatch.schedule.common.ScheduleTriggerImpl;

@Component("schedule_trigger_AtalkInspectionTrigger")
public class ScheduleAtalkInspectionTrigger extends ScheduleTriggerImpl {

	@Override
	protected void init() {
		super.jobName = "atalkInspectionScheduleJob";
		super.desc = "템플릿 검수";

		// 매시 5분 실행
		super.expression = "0 5 * * * ?";
	}

}
