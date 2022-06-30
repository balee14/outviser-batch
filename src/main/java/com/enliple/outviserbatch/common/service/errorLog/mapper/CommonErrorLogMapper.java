package com.enliple.outviserbatch.common.service.errorLog.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface CommonErrorLogMapper {
    
    int insertCmErrorLog(DataMap param);
}
