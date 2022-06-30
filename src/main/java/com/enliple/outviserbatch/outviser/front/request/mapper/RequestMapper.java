package com.enliple.outviserbatch.outviser.front.request.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface RequestMapper {
    
    int insertRequestSendData(List<DataMap> dataMap);
    
    int insertSendFailData(List<DataMap> dataMap);
    
    
}
