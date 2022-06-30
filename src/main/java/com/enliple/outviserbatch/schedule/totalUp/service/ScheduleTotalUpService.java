package com.enliple.outviserbatch.schedule.totalUp.service;


import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.schedule.totalUp.mapper.ScheduleTotalUpMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ScheduleTotalUpService {

    @Autowired
    private ScheduleTotalUpMapper scheduleTotalUpMapper;

    @Value("${totalUp.limit.cnt}")
    private int totalUpLimitCnt;

    public void totalUpProcess() throws Exception{

        DataMap param = new DataMap();
        param.put("limitCnt", totalUpLimitCnt );

        // start

        // 정산 대상 조회
        List<DataMap> totalUpTargetList = scheduleTotalUpMapper.selectTotalUpTargetList( param );
        scheduleTotalUpMapper.testInsert( "[TotalUp] Start SIZE : " + totalUpTargetList.size()  );

        for( DataMap totalUpTarget : totalUpTargetList ){
            // 정산 대상 집행 정보 조회
            List<DataMap> exeRunHstList = scheduleTotalUpMapper.selectExeRunHstList( totalUpTarget );

            // 집행 이력만큼 돌면서 건수 업데이트
            for( DataMap exeRunHst : exeRunHstList ){
                DataMap updateData = scheduleTotalUpMapper.selectUpdateData( exeRunHst );
                scheduleTotalUpMapper.updateExeRunHstForAddSendCnt( updateData );
                //scheduleTotalUpMapper.testInsert( "[TotalUp] Update : : " + exeRunHst.toString()  );
                System.out.println( "[TotalUp] Update : : " + exeRunHst.toString() );
            }
        }
        //scheduleTotalUpMapper.testInsert( "[TotalUp] End");
    }


}
