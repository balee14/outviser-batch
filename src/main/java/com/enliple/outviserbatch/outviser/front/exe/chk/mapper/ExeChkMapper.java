package com.enliple.outviserbatch.outviser.front.exe.chk.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface ExeChkMapper {
    
    int insertExecutionalChk(DataMap dataMap);
    
}
