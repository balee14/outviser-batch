package com.enliple.outviserbatch.outviser.front.exe.chk.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.exe.chk.mapper.ExeChkMapper;

@Service
public class ExeChkService {

	@Autowired
	private ExeChkMapper exeChkMapper;

	/**
	 * @param param
	 * @throws Exception
	 * @comment
	 */
	public void insertExecutionalChk(DataMap param) throws Exception {
		int nRst = exeChkMapper.insertExecutionalChk(param);
		if (nRst <= 0) {
			throw new CommonException("ExeChkService > insertExecutionalChk : null", param);
		}
	}
}
