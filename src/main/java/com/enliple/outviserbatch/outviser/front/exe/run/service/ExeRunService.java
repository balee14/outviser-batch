package com.enliple.outviserbatch.outviser.front.exe.run.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.exe.run.mapper.ExeRunMapper;

@Service
public class ExeRunService {

	@Autowired
	private ExeRunMapper exeErrorMapper;

	/**
	 * 집행 이력 생성
	 * 
	 * @param param
	 * @throws Exception
	 * @comment
	 */
	public void insertExeRunHst(DataMap param) throws Exception {
		int nRst = exeErrorMapper.insertExeRunHst(param);
		if (nRst <= 0) {
			throw new CommonException("집행 이력 생성중 오류가 발생하였습니다. - insertExeRunHst", param);
		}
	}

	/**
	 * 집행 이력 갱신
	 * 
	 * @param param
	 * @throws Exception
	 * @comment
	 */
	public void updateExeRunHst(DataMap param) throws Exception {
		int nRst = exeErrorMapper.updateExeRunHst(param);
		if (nRst <= 0) {
			throw new CommonException("집행 이력 갱신중 오류가 발생하였습니다. - updateExeRunHst", param);
		}
	}
}
