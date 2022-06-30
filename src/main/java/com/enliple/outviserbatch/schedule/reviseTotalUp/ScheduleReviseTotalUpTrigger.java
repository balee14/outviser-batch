package com.enliple.outviserbatch.schedule.reviseTotalUp;


import com.enliple.outviserbatch.schedule.common.ScheduleTriggerImpl;
import org.springframework.stereotype.Component;

@Component( "schedule_trigger_ReviseTotalUpTrigger" )
public class ScheduleReviseTotalUpTrigger extends ScheduleTriggerImpl {
    @Override
    protected void init(){

        super.jobName = "reviseTotalUpScheduleJob";
        super.desc = "집계 데이터 보정"; // 집행 된지 4일 이상 지난 집행건의 누락 데이터 실패 처리

        // 매일 5시 13분
        super.expression = "0 13 5 * * ?";
    }
}
