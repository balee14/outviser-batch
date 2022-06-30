package com.enliple.outviserbatch.outviser.api.product.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.api.product.mapper.ApiProductMapper;

@Service
public class ApiProductService {

	@Autowired
	private ApiProductMapper apiProductMapper;

	/**
	 * 발송요청 계약 정보 확인
	 * 
	 * @param param
	 * @throws Exception
	 */
	public DataMap selectContractData(DataMap param) throws Exception {
		DataMap contractData = apiProductMapper.selectContractData(param);
		if (contractData == null) {
			throw new CommonException("계정에 등록된 상품 정보가 없습니다. - selectContractData", param);
		}
		return contractData;
	}
}
