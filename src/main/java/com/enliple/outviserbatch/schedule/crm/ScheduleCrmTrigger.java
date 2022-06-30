package com.enliple.outviserbatch.schedule.crm;

import org.springframework.stereotype.Component;

import com.enliple.outviserbatch.schedule.common.ScheduleTriggerImpl;

@Component("schedule_trigger_CrmTrigger")
public class ScheduleCrmTrigger extends ScheduleTriggerImpl {

	@Override
	protected void init() {
		super.jobName = "crmScheduleJob";
		super.desc = "모비튠 CRM 통계 리포트";

		// 매시 20분 실행 -> 20220207 40분 실행 으로 수정
		super.expression = "0 45 * * * ?";
	}

}
