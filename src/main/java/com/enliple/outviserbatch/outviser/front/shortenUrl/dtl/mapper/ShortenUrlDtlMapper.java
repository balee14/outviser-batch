package com.enliple.outviserbatch.outviser.front.shortenUrl.dtl.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface ShortenUrlDtlMapper {
    
    int insertShortenUrlDtlList(Object[] dataObjects);
    
    int selectShortenUrlDtlCount(DataMap dataMap);
    
    int insertBulkShortenUrlDtlList(List<DataMap> dataMapList);
    
    int insertShortenUrlDtl(DataMap dataMap);
    
    DataMap selectShortenUrl(DataMap dataMap);
    
    int updateShortenUrlForInflowCnt(DataMap dataMap);
    
}
