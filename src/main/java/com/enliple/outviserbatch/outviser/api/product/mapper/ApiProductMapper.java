package com.enliple.outviserbatch.outviser.api.product.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface ApiProductMapper {
    
    DataMap selectContractData(DataMap dataMap);
    
    
    
    
}
