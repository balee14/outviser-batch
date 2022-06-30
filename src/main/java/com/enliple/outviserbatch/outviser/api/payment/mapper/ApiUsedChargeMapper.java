package com.enliple.outviserbatch.outviser.api.payment.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface ApiUsedChargeMapper {
    
    DataMap selectCheckAmount(DataMap dataMap);
    
    List<DataMap> selectChargeData(DataMap dataMap);
    
    int insertUsedCharge(List<DataMap> dataMap);
    
    int updateChargeData(List<DataMap> dataMap);
    
    
    
    
}