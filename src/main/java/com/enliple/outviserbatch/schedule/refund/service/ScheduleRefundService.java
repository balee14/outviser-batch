package com.enliple.outviserbatch.schedule.refund.service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.schedule.refund.mapper.ScheduleRefundMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ScheduleRefundService {

    @Value("${refund.limit.cnt}")
    private int refundLimitCnt;

    @Autowired
    private ScheduleRefundMapper scheduleRefundMapper;

    @Autowired
    private ScheduleRefundProcess refundProcess;


    // 환불 대상 조회 및 환불 시작
    public void refundInit() throws Exception{

        DataMap param = new DataMap();
        param.put("limitCnt", refundLimitCnt );

        // 대상 조회
        List<DataMap> refundTargetList = scheduleRefundMapper.selectRefundTargetList( param );
        scheduleRefundMapper.testInsert( "[Refund] Start size : " + refundTargetList.size() );

        for( DataMap refundTarget : refundTargetList  ){
            try{
                refundProcess.refund( refundTarget );
            }catch ( Exception e ){
                scheduleRefundMapper.testInsert( "[Refund] refundProcess Exception : " + e.getMessage() + " refundTarget : " + refundTarget );
            }
        }
        scheduleRefundMapper.testInsert( "[Refund] End" );
    }

}
