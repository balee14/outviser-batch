package com.enliple.outviserbatch.outviser.front.report.crm.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.report.crm.mapper.ReportCrmMapper;

@Service
public class ReportCrmService {

	@Autowired
	private ReportCrmMapper reportCrmMapper;

	/**
	 * @param param
	 * @throws Exception
	 */
	public void selectRejectList(List<DataMap> param) throws Exception {
		int dataCnt = reportCrmMapper.insertCrmOrgData(param);
		if (dataCnt < 1) {
			throw new CommonException("ReportCrmService > selectRejectList : dataCnt < 1", param);
		}
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public void insertReportDataH(DataMap param) throws Exception {
		int dataCnt = reportCrmMapper.insertReportDataH(param);
		if (dataCnt < 1) {
			throw new CommonException("ReportCrmService > insertReportDataH : dataCnt < 1", param);
		}
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public void deleteCrmOrgData(DataMap param) throws Exception {
		int dataCnt = reportCrmMapper.deleteCrmOrgData(param);
		if (dataCnt < 1) {
			throw new CommonException("ReportCrmService > deleteCrmOrgData : dataCnt < 1", param);
		}
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public void deleteReportDataH(DataMap param) throws Exception {
		int dataCnt = reportCrmMapper.deleteReportDataH(param);
		if (dataCnt < 1) {
			throw new CommonException("ReportCrmService > deleteReportDataH : dataCnt < 1", param);
		}
	}
}