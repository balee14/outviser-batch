package com.enliple.outviserbatch.outviser.front.reject.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.reject.mapper.RejectMapper;

@Service
public class RejectService {

	@Autowired
	private RejectMapper rejectMapper;

	/**
	 * @param param
	 * @throws Exception
	 */
	public List<DataMap> selectRejectList(DataMap param) throws Exception {
		List<DataMap> dataMap = rejectMapper.selectRejectList(param);
		if (dataMap == null) {
			throw new CommonException("RejectService > selectRejectList : dataMap == null", param);
		}
		return dataMap;
	}
}