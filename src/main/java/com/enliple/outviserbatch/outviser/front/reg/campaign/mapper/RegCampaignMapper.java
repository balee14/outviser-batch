package com.enliple.outviserbatch.outviser.front.reg.campaign.mapper;


import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface RegCampaignMapper {
    
    DataMap selectExeCampaign(DataMap dataMap);
    
    DataMap selectRequiredData(DataMap dataMap);
    
    int updateCampaignMstByTemplateRowid(DataMap param);
    
    DataMap selectCampaignMst(DataMap dataMap);
    
    List<DataMap> selectRequiredDataList(DataMap param);
}