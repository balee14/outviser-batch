package com.enliple.outviserbatch.outviser.front.shortenUrl.inflow.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface ShortenUrlInflowMapper {
    
    int insertShortenUrlInflowLog(List<DataMap> dataMapList);
    
}
