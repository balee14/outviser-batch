package com.enliple.outviserbatch.schedule.point.service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.api.payment.service.ApiUsedChargeService;
import com.enliple.outviserbatch.schedule.point.mapper.SchedulePointMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchedulePointService {

	@Autowired
	private SchedulePointMapper schedulePointMapper;

	@Autowired
	private ApiUsedChargeService apiUsedChargeService;

	/**
	 * 해당 계정의 기간 만료 포인트 조회
	 * 
	 * @param acctRowid
	 * @return
	 * @throws Exception
	 */
	public List<DataMap> selectChargeListForExpiry(int acctRowid) throws Exception {

		List<DataMap> result = schedulePointMapper.selectChargeListForExpiry(acctRowid);
		if (result == null) {
			throw new CommonException("기간 만료 포인트 조회중 오류", acctRowid);
		}

		return result;
	}

	/*
	 * 트랜잭션 처리로 인해 해당 메서드로 구성
	 */
	public void removeExpiryPoint(int acctRowid,  List<DataMap> listPoint) throws Exception {
		for (DataMap point : listPoint) {
			this.insertUsedChargeForExpiry(point);
			this.updateChargeForExpiry(point);
			this.updateAcctMstForExpiry(point);
		}

		DataMap param = new DataMap();
		param.put("sessionUserRowId", acctRowid);
		param.put("reqAmount", 0);
		apiUsedChargeService.selectCheckAmount(param, false);
	}

	/**
	 * OV_USED_CHARGE 기간 만료 포인트 차감 이력 추가
	 * 
	 * @param dataMap
	 * @throws Exception
	 */
	private void insertUsedChargeForExpiry(DataMap dataMap) throws Exception {

		if (schedulePointMapper.insertUsedChargeForExpiry(dataMap) == 0) {
			throw new CommonException("기간 만료 포인트 차감 이력 추가중 오류", dataMap);
		}
	}

	/**
	 * OV_CHARGE 기간 만료 포인트 차감
	 * 
	 * @param dataMap
	 * @throws Exception
	 */
	private void updateChargeForExpiry(DataMap dataMap) throws Exception {

		if (schedulePointMapper.updateChargeForExpiry(dataMap) == 0) {
			throw new CommonException("기간 만료 포인트 차감중 오류", dataMap);
		}
	}

	/**
	 * OV_ACCT_MST 현재 포인트 차감
	 * 
	 * @param dataMap
	 * @throws Exception
	 */
	private void updateAcctMstForExpiry(DataMap dataMap) throws Exception {

		if (schedulePointMapper.updateAcctMstForExpiry(dataMap) == 0) {
			throw new CommonException("현재 포인트 차감중 오류", dataMap);
		}
	}
}
