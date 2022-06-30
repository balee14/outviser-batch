package com.enliple.outviserbatch.schedule.cdp.mapper;

import com.enliple.outviserbatch.common.data.DataMap;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ScheduleCdpMapper {

	void insertLog(DataMap dataMap);

	List<DataMap> selectReserveCampaignList();
}
