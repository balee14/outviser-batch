package com.enliple.outviserbatch.schedule.point.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface SchedulePointMapper {

	public int insertUsedChargeForExpiry(DataMap param);

	public int updateChargeForExpiry(DataMap param);

	public int updateAcctMstForExpiry(DataMap param);

	public List<DataMap> selectChargeListForExpiry(int accRowid);
}
