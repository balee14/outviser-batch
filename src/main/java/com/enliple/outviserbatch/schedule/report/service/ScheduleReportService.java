package com.enliple.outviserbatch.schedule.report.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.common.util.DateUtils;
import com.enliple.outviserbatch.outviser.front.batch.service.BatchService;
import com.enliple.outviserbatch.outviser.front.report.msg.service.ReportMsgService;
import com.enliple.outviserbatch.schedule.report.mapper.ScheduleReportMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ScheduleReportService {

	@Autowired
	private BatchService batchService;

	@Autowired
	private ReportMsgService reportMsgService;

	@Autowired
	private ScheduleReportMapper scheduleReportMapper;

	/**
	 * MTS 기간별 발송 리포트 처리
	 * 
	 * @throws Exception
	 * @throws CommonException
	 */
	public void reportMts() throws Exception {

		String workDate = DateUtils.getCurrentDate("yyyy-MM-dd");
		String preDate = DateUtils.addDate(workDate, -1);

		StringBuffer stb = new StringBuffer();
		stb.append("preDate:").append(preDate);
		stb.append(",").append("workDate:").append(workDate);

		DataMap dataMap = new DataMap();
		dataMap.put("batchId", "reportMts");

		/**
		 * OV_BATCH_MST 테이블내 데이터는 고정임
		 */
		List<DataMap> batchList = batchService.selectBatch(dataMap);

		for (DataMap batch : batchList) {
			// dataMap 초기화
			dataMap.clear();

			dataMap.put("batchRowid", batch.getLong("batchRowid"));
			dataMap.put("preDate", preDate);
			dataMap.put("status", "Start");
			dataMap.put("successYn", "N");
			dataMap.put("endYn", "N");
			dataMap.put("params", stb.toString());

			if (batchService.runCheckDaily(dataMap) > 0) {
				log.warn("이미 실행된 이력이 존재 -> {}", batch.getString("batchName"));
				continue;
			}

			// start log
			if (batchService.insertLog(dataMap) > 0) {
				int insertCnt = -1;
				int orderedSeq = batch.getInt("orderedSeq");

				if (1 == orderedSeq) {
					// 시간 데이터 생성
					insertCnt = reportMsgService.insertMtsH(dataMap);
				} else if (2 == orderedSeq) {
					// 일 데이터 생성
					insertCnt = reportMsgService.insertMtsD(dataMap);
				} else if (3 == orderedSeq) {
					// 주 데이터 생성
					dataMap.put("yearMonthWeek", this.selectWeek(dataMap));
					insertCnt = reportMsgService.insertMtsW(dataMap);
				} else if (4 == orderedSeq) {
					// 월 데이터 생성
					insertCnt = reportMsgService.insertMtsM(dataMap);
				} else if (5 == orderedSeq) {
					// 년 데이터 생성
					insertCnt = reportMsgService.insertMtsY(dataMap);
				} else {
					log.warn("Unknown sequence number -> int: {} / String: {}", orderedSeq,
							batch.getString("orderedSeq"));
				}

				if (insertCnt != -1) {
					dataMap.put("status", "End");
					dataMap.put("successYn", "Y");
					dataMap.put("endYn", "Y");
					dataMap.put("resultMsg", String.format("배치 실행 완료 : %s건", insertCnt));

					log.info("MTS 리포트 데이터 저장 성공 -> {}", dataMap.toString());
				}
			}

			// end log
			if (batchService.updateLog(dataMap) < 1) {
				throw new CommonException("ScheduleReportService > reportMts : updateLog failure", dataMap);
			}
		}
	}

	private String selectWeek(DataMap param) throws Exception {
		String dataStr = scheduleReportMapper.selectWeek(param);
		if (StringUtils.isBlank(dataStr)) {
			throw new CommonException("ScheduleReportService > selectWeek : dataStr null", param);
		}

		return dataStr;
	}
}
