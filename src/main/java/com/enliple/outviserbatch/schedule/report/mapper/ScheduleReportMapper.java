package com.enliple.outviserbatch.schedule.report.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface ScheduleReportMapper {

	public String selectWeek(DataMap param);
	
}
