package com.enliple.outviserbatch.schedule.cafe24.mapper;

import com.enliple.outviserbatch.common.data.DataMap;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ScheduleCafe24TokenReissuanceMapper {

	List<DataMap> selectTokenExpirationImminentList();

	void updateTokenExpirationImminent(DataMap dataMap);
	
}
