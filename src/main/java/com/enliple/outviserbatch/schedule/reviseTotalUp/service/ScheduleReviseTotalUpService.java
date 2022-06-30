package com.enliple.outviserbatch.schedule.reviseTotalUp.service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.schedule.reviseTotalUp.mapper.ScheduleReviseTotalUpMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleReviseTotalUpService {

    @Autowired
    private ScheduleReviseTotalUpMapper scheduleReviseTotalUpMapper;

    @Value("${revise.totalUp.limit.cnt}")
    private int limitCnt;

    // 집계 데이터 보정
    public void reviseTotalUpProcess() throws  Exception{
        DataMap param = new DataMap();
        param.put("limitCnt", limitCnt );

        // 집계 데이터 보정 대상 목록 조회
        List<DataMap> targetList = scheduleReviseTotalUpMapper.selectReviseTotalUpTargetList( param );
        scheduleReviseTotalUpMapper.testInsert( "[Revise] Start size : " + targetList.size() );

        for ( DataMap target : targetList ){
            // 집계 데이터 업데이트
            try{
                scheduleReviseTotalUpMapper.updateReviseTotalUp( target );
            }catch ( Exception e){
                scheduleReviseTotalUpMapper.testInsert( "[Revise] ReviseUpdate Exception : " + e.getMessage() + " ReviseTarget : " + target );
            }
            // scheduleReviseTotalUpMapper.testInsert( "[Revise] Update : " + target.toString() );
            System.out.println( "[Revise] Update : " + target.toString() );
        }
        scheduleReviseTotalUpMapper.testInsert( "[Revise] End" );
    }
}
