package com.enliple.outviserbatch.schedule.cdp;

import com.enliple.outviserbatch.schedule.common.ScheduleTriggerImpl;
import org.springframework.stereotype.Component;

@Component("schedule_trigger_CdpTrigger")
public class ScheduleCdpTrigger extends ScheduleTriggerImpl {

	@Override
	protected void init() {
		super.jobName = "cdpScheduleJob";
		super.desc = "CDP 예약 백업 갱신";

		// 매일 새벽 4시 10분 에 실행
		super.expression = "0 10 4 * * ?";

// 임시로 10초 마다 누적
//		super.expression = "0/10 * 8-21 * * ?";
	}

}
