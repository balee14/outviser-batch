package com.enliple.outviserbatch.schedule.reviseTotalUp.mapper;

import com.enliple.outviserbatch.common.data.DataMap;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ScheduleReviseTotalUpMapper {

    void testInsert ( String param );

    List<DataMap> selectReviseTotalUpTargetList(DataMap dataMap );

    void updateReviseTotalUp( DataMap dataMap );


}
