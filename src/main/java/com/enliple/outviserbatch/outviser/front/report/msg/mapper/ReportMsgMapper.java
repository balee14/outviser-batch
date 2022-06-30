package com.enliple.outviserbatch.outviser.front.report.msg.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface ReportMsgMapper {
    
    public int insertMtsH(DataMap param);
    
    public int insertMtsD(DataMap param);
    
    public int insertMtsW(DataMap param);
    
    public int insertMtsM(DataMap param);
    
    public int insertMtsY(DataMap param);
    
    
}
