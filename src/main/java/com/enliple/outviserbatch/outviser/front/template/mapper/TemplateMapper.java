package com.enliple.outviserbatch.outviser.front.template.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface TemplateMapper {
    
    DataMap selectExeTemplate(DataMap dataMap);
    
    List<DataMap> selectTemplateAttach(int tmpDtlRowid);
    
    List<DataMap> selectTemplateLink(int tmpDtlRowid);
    
    List<DataMap> selectExeTempVarList(DataMap dataMap);
    
    List<DataMap> selectExeTempGrpVarList(DataMap dataMap);
    
    List<DataMap> selectTmpListBySenderRowid(DataMap param);
    
    DataMap selectTemplate(DataMap param);
    
    int updateTemplateInspect(DataMap param);
    
}