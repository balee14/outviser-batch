package com.enliple.outviserbatch.outviser.front.exe.campaign.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface ExeCampaignMapper {

	int insertExecutional(DataMap dataMap);

	int updateExecutional(DataMap dataMap);

	int insertExecutionalHis(DataMap dataMap);

	boolean selectExeModCheck(DataMap dataMap);

	DataMap selectExeInfoForAdmin(DataMap dataMap);
}