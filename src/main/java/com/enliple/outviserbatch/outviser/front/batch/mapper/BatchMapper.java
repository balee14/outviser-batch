package com.enliple.outviserbatch.outviser.front.batch.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface BatchMapper {
    
    public List<DataMap> selectBatch(DataMap param);
    
    public long runCheckDaily(DataMap param);
    
    public int insertLog(DataMap param);
    
    public int updateLog(DataMap param);
    
    
}
