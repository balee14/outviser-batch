package com.enliple.outviserbatch.schedule.totalUp.mapper;

import com.enliple.outviserbatch.common.data.DataMap;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ScheduleTotalUpMapper {

    void testInsert ( String param );

    List<DataMap>selectTotalUpTargetList( DataMap dataMap );

    List<DataMap>selectExeRunHstList( DataMap dataMap );

    DataMap selectUpdateData( DataMap dataMap );

    void updateExeRunHstForAddSendCnt( DataMap dataMap );

}
