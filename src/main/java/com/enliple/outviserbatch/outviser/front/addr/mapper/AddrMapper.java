package com.enliple.outviserbatch.outviser.front.addr.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface AddrMapper {
    
    List<DataMap> selectExcelAddress(DataMap dataMap);

    List<DataMap> selectSendApiAddress(DataMap dataMap);
    
}
