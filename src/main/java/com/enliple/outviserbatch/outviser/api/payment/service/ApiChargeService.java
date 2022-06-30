package com.enliple.outviserbatch.outviser.api.payment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.outviser.api.payment.mapper.ApiChargeMapper;

@Service
public class ApiChargeService {
	
	@Autowired
	private ApiChargeMapper apiChargeMapper;

	/**
	 * 충전 내역 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public void insertCharge(DataMap param) throws Exception {
	    int rst = apiChargeMapper.insertCharge(param);
		//int rst = outviserDao.insert("enliple.ibot.outviser.api.payment.insertCharge", param);
		if(rst <= 0)
			throw new Exception("충전 처리중 오류가 발생하였습니다. - insertCharge");
	}
	
	
	
}