package com.enliple.outviserbatch.outviser.front.reg.template.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface RegTemplateMapper {
    
    List<DataMap> selectTmpGrpByTmpRowid(DataMap param);
    
    DataMap selectTmpGrpByTmpUseTypeCode(DataMap param);
}