package com.enliple.outviserbatch.common.action.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface StartupMapper {
    
    List<DataMap> selectCodeList();
    
    List<DataMap> selectCampCondScoreList();
    
    
}
