package com.enliple.outviserbatch.outviser.front.reg.template.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.reg.template.mapper.RegTemplateMapper;

@Service
public class RegTemplateService {

	@Autowired
	private RegTemplateMapper regTemplateMapper;

	/**
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<DataMap> selectTmpGrpByTmpRowid(DataMap param) throws Exception {
		List<DataMap> dataMap = regTemplateMapper.selectTmpGrpByTmpRowid(param);
		if (dataMap == null) {
			throw new CommonException("RegTemplateService > selectTmpGrpByTmpRowid : dataMap == null", param);
		}
		return dataMap;
	}
}