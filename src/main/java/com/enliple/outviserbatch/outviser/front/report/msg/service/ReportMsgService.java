package com.enliple.outviserbatch.outviser.front.report.msg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.report.msg.mapper.ReportMsgMapper;

@Service
public class ReportMsgService {

	@Autowired
	private ReportMsgMapper reportMsgMapper;

	/**
	 * @param param
	 * @throws Exception
	 */
	public int insertMtsH(DataMap param) throws Exception {
		int dataCnt = reportMsgMapper.insertMtsH(param);
		if (dataCnt < 1) {
			throw new CommonException("ReportMsgService > insertMtsH : dataCnt < 1", param);
		}
		return dataCnt;
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public int insertMtsD(DataMap param) throws Exception {
		int dataCnt = reportMsgMapper.insertMtsD(param);
		if (dataCnt < 1) {
			throw new CommonException("ReportMsgService > insertMtsD : dataCnt < 1", param);
		}
		return dataCnt;
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public int insertMtsW(DataMap param) throws Exception {
		int dataCnt = reportMsgMapper.insertMtsW(param);
		if (dataCnt < 1) {
			throw new CommonException("ReportMsgService > insertMtsW : dataCnt < 1", param);
		}
		return dataCnt;
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public int insertMtsM(DataMap param) throws Exception {
		int dataCnt = reportMsgMapper.insertMtsM(param);
		if (dataCnt < 1) {
			throw new CommonException("ReportMsgService > insertMtsM : dataCnt < 1", param);
		}
		return dataCnt;
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public int insertMtsY(DataMap param) throws Exception {
		int dataCnt = reportMsgMapper.insertMtsY(param);
		if (dataCnt < 1) {
			throw new CommonException("ReportMsgService > insertMtsY : dataCnt < 1", param);
		}
		return dataCnt;
	}
}