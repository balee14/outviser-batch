package com.enliple.outviserbatch.outviser.api.payment.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface ApiChargeMapper {
    
    int insertCharge(DataMap param);
    
    
}