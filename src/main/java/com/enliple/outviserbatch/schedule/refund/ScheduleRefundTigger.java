package com.enliple.outviserbatch.schedule.refund;


import com.enliple.outviserbatch.schedule.common.ScheduleTriggerImpl;
import org.springframework.stereotype.Component;

@Component( "schedule_trigger_RefundTrigger" )
public class ScheduleRefundTigger extends ScheduleTriggerImpl {

    @Override
    protected void init(){

        super.jobName = "refundScheduleJob";
        super.desc = "환불 자동 업데이트";

        // 매시 8분,38분 ( 당분간 새벽시간대 500건씩 )
        super.expression = "0 8/30 2-7 * * ?";
;    }
}
