package com.enliple.outviserbatch.outviser.front.exe.run.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface ExeRunMapper {
    
    int insertExeRunHst(DataMap dataMap);
    
    int updateExeRunHst(DataMap dataMap);
    
}
