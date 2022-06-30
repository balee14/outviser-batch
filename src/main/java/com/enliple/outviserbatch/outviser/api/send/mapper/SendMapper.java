package com.enliple.outviserbatch.outviser.api.send.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface SendMapper {

	DataMap selectSweetTrackerMstByRowId(DataMap dataMap);

	DataMap selectSweetTrackerCharge(DataMap dataMap);

	DataMap selectSweetTrackerMstByInvoiceNo(DataMap dataMap);

	List<DataMap> selectCampaignMstByCrmNo(DataMap dataMap);

	List<DataMap> selectSweetTrackerVarList(DataMap dataMap);

	int selectSweetTrackerLogByLevelCnt(DataMap dataMap);

	// int insertSweetTracker(DataMap dataMap);

	int insertSweetTrackerLog(DataMap dataMap);

	int insertSweetTrackerMst(DataMap dataMap);

	int insertSweetTrackerVarGrp(DataMap dataMap);

	int updateSweetTrackerMst(DataMap dataMap);
}
