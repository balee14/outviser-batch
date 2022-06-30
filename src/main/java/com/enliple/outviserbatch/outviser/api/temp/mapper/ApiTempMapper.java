package com.enliple.outviserbatch.outviser.api.temp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface ApiTempMapper {
    
    void createTempReqData(DataMap dataMap);
    
    void insertTempReqData(DataMap dataMap);
    
    List<DataMap> selectTempReqData(DataMap dataMap);
    
    void dropTempReqData(DataMap dataMap);
    
    
    
}
