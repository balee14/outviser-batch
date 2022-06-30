package com.enliple.outviserbatch.outviser.front.crm.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface CrmMapper {
    
    DataMap checkRun(DataMap param);
    
    int insertCrmLog(DataMap param);
    
    int updateCrmLog(DataMap param);

    /**
     * 22.04.28 김대현
     * CDP 조건수에 따른,
     * 광고주별 추가금액 정보(CSV Pattern)를
     * OV_CDP_EXTRA_AMOUNT 테이블에서 추출
     *
     * @param adverId 광고주ID
     * @return CSV Pattern Data
     */
    String selectCdpExtraAmountCsvPattern(String adverId);
    
}
