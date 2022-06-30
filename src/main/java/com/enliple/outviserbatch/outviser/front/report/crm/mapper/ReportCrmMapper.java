package com.enliple.outviserbatch.outviser.front.report.crm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface ReportCrmMapper {
    
    int insertCrmOrgData(List<DataMap> param);
    
    int insertReportDataH(DataMap param);
    
    int deleteCrmOrgData(DataMap param);
    
    int deleteReportDataH(DataMap param);
    
}
