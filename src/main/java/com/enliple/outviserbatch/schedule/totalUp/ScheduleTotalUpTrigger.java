package com.enliple.outviserbatch.schedule.totalUp;


import com.enliple.outviserbatch.schedule.common.ScheduleTriggerImpl;
import org.springframework.stereotype.Component;

@Component("schedule_trigger_TotalUpTrigger")
public class ScheduleTotalUpTrigger extends ScheduleTriggerImpl {
    @Override
    protected void init(){

        super.jobName = "totalUpScheduleJob";
        super.desc = "캠페인별 집계 업데이트";

        // 1시부터 5시까지 매시 5분
        super.expression = "0 5 1-5 * * ?";
    }

}
