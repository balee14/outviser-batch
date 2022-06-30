package com.enliple.outviserbatch.outviser.front.addr.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.addr.mapper.AddrMapper;

@Service
public class AddrService {

	@Autowired
	private AddrMapper addrMapper;

	/**
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<DataMap> selectExcelAddress(DataMap param) throws Exception {
		List<DataMap> resultMap = addrMapper.selectExcelAddress(param);
		if (resultMap == null) {
			throw new CommonException("AddrService > selectExcelAddress : resultMap = null", param);
		}
		return resultMap;
	}

	/** 2022.05.09 임달형
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<DataMap> selectSendApiAddress(DataMap param){
		List<DataMap> resultMap = addrMapper.selectSendApiAddress(param);
		if (resultMap == null) {
			throw new CommonException("AddrService > selectSendApiAddress : resultMap = null", param);
		}
		return resultMap;
	}
}