package com.enliple.outviserbatch.outviser.front.reject.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface RejectMapper {
    
    List<DataMap> selectRejectList(DataMap dataMap);
    
    
}
