package com.enliple.outviserbatch.schedule.report;

import com.enliple.outviserbatch.schedule.common.ScheduleTriggerImpl;

//@Component("schedule_trigger_ReportTrigger")
public class ScheduleReportTrigger extends ScheduleTriggerImpl {

	@Override
	protected void init() {
		super.jobName = "reportScheduleJob";
		super.desc = "MTS 기간별 발송 리포트";

		// 매일 01시에 실행
		super.expression = "0 0 01 * * ?";
	}

}
