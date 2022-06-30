package com.enliple.outviserbatch.schedule.crm.service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.common.util.DateUtils;
import com.enliple.outviserbatch.common.util.JsonUtils;
import com.enliple.outviserbatch.common.util.MobtuneCrmUtil;
import com.enliple.outviserbatch.outviser.front.crm.mapper.CrmMapper;
import com.enliple.outviserbatch.outviser.front.report.crm.mapper.ReportCrmMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

@Slf4j
@Service("schedule_crm_CrmService")
public class ScheduleCrmService {

	@Autowired
	private CrmMapper crmMapper;

	@Autowired
	private ReportCrmMapper reportCrmMapper;

	@Value("${mobtune.crm.api.url}")
	private String crmApi;

	/**
	 * 모비튠 CRM API - 채널 통계 상태 조회 API
	 */
	private String crmCheckBatch;

	private String crmGetReport;

	private void init() {
		if (StringUtils.isBlank(crmCheckBatch)) {
			crmCheckBatch = String.format("%s/api/v1/channels/stats", crmApi);
		}
		if (StringUtils.isBlank(crmGetReport)) {
			crmGetReport = String.format("%s/api/v1/channels/01/stats", crmApi);
		}
	}

	/**
	 * 모비튠 CRM 통계 리포트 요청
	 * 
	 * @throws Exception
	 * @throws CommonException
	 */
	public void createCrmReport() throws Exception {
		this.init();

		String checkDate = DateUtils.getCurrentDate("yyyyMMddHH");
		String startDate = DateUtils.addDate(checkDate, Calendar.HOUR, -1, "yyyyMMddHH");

		StringBuffer stb = new StringBuffer();
		stb.append("checkDate:").append(checkDate);
		stb.append(",").append("workDate:").append(startDate.substring(0, 8));
		stb.append(",").append("startDateH:").append(startDate);
		stb.append(",").append("endDateH:").append(startDate);

		DataMap dataMap = new DataMap();
		dataMap.put("status", "Start");
		dataMap.put("params", stb.toString());
		dataMap.put("startDateH", startDate);
		dataMap.put("endDateH", startDate);

		DataMap checkRun = crmMapper.checkRun(dataMap);

		if (checkRun.getString("runYn").equals("Y")) {
			// 동일한 파라미터로 실행한 이력이 있는 경우

			if (checkRun.getString("reworkYn").equals("Y")) {
				// 재작업이 Y이면, 생성데이터 삭제 후 재작업
				if (reportCrmMapper.deleteCrmOrgData(dataMap) < 0) {
					throw new CommonException("deleteCrmOrgData 처리중 오류 발생", dataMap);
				}

				if (reportCrmMapper.deleteReportDataH(dataMap) < 0) {
					throw new CommonException("deleteReportDataH 처리중 오류 발생", dataMap);
				}
			} else {
				// 재작업이 N이면, 생성완료 오류 처리
				throw new CommonException("CRM 통계정보가 이미 생성되어 있음", dataMap);
			}

		} else {
			// 동일한 파라미터로 실행한 이력이 없으면 로그 데이터 처리

			if (crmMapper.insertCrmLog(dataMap) <= 0) {
				throw new CommonException("insertCrmLog 처리중 오류 발생", dataMap);
			}

		}

		int requestTime = 1;
		DataMap mobtuneReqParam = null;
		DataMap mobtuneApiResData = null;

		// 실행 이력 생성
		while (requestTime <= 15) {
			// 요청종료시간이 있을경우 요청종료시간에 대해 CRM 배치 완료 체크, 요청종료시간이 없을경우 요청시작시간으로 CRM 배치 완료 체크
			mobtuneReqParam = new DataMap();
			mobtuneReqParam.put("yyyyMMddHH", checkDate);
			mobtuneApiResData = MobtuneCrmUtil.callMobtuneCrmApiMap(crmCheckBatch, mobtuneReqParam);

			DataMap crmCheckRst = null;
			boolean isSuccess = false;
			String rsMessage = "";

			// API 호출 성공 여부
			if (mobtuneApiResData.getString("rsCode").indexOf("S") >= 0) {
				crmCheckRst = mobtuneApiResData.getDataMap("rsData");

				// 모비튠 CRM 배치 종료 여부
				if (ObjectUtils.isNotEmpty(crmCheckRst) && crmCheckRst.getString("resultCode").toUpperCase().equals("S")) {
					mobtuneReqParam = new DataMap();
					mobtuneReqParam.put("startDt", startDate);
					mobtuneReqParam.put("endDt", startDate);
					mobtuneApiResData = MobtuneCrmUtil.callMobtuneCrmApiMap(crmGetReport, mobtuneReqParam);

					List<DataMap> reportData = null;

					crmCheckRst = mobtuneApiResData.getDataMap("rsData");
					if (ObjectUtils.isNotEmpty(crmCheckRst)) {
						reportData = JsonUtils.toArrayDataMap(crmCheckRst.getString("data"));
					}

					if (ObjectUtils.isNotEmpty(reportData)) {
						isSuccess = true;

						// CRM 원본 데이터 생성
						reportCrmMapper.insertCrmOrgData(reportData);

						// CRM 데이터 기준으로 시간 리포트 생성
						reportCrmMapper.insertReportDataH(dataMap);

						dataMap.put("crmResultMsg", String.format("%s건 생성완료", reportData.size()));

						log.warn("CRM 원본 데이터 저장 성공 -> {}", reportData.toString());
					} else {
						// 알람 체크 : 배치완료로 되어 있지만 실제 데이터가 없는 경우
						rsMessage = "CRM 호출 결과 없음";
					}
				} else {
					// 실행 이력 오류 처리
					if (requestTime >= 5) {
						// 알람 체크 : 10분 후에도 정상종료가 아닌경우 CRM 배치 확인요청이 필요하다. 이경우 친구톡 등의 방식으로 알림필요
						rsMessage = "CRM 배치 확인 요청 필요(CRM 배치 미완료)";
					} else {
						// 모비튠 CRM 배치 종료 체크(2분에 한번) -> 20220207 3분 으로 수정
						Thread.sleep(3 * 60 * 1000);
						requestTime++;
						continue;
					}
				}
			} else {
				rsMessage = "CRM API 호출에 실패하였습니다";
			}

			// CRM 로그 업데이트
			updateCrmLog(isSuccess, dataMap);

			if (! isSuccess) {
				throw new CommonException(rsMessage, dataMap);
			}

			break;
		}
	}

	private void updateCrmLog(boolean isSuccess, DataMap param) {

		DataMap dataMap = (DataMap) param.clone();
		dataMap.put("endYn", "Y");
		dataMap.put("checkYn", "N");
		dataMap.put("status", "End");
		dataMap.put("successYn", isSuccess ? "Y" : "N");
		dataMap.put("reworkYn", isSuccess ? "N" : "Y");
		if (! isSuccess) {
			dataMap.put("crmResultMsg", "오류");
		}

		crmMapper.updateCrmLog(dataMap);
	}
}
