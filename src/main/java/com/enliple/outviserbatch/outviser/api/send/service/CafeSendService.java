package com.enliple.outviserbatch.outviser.api.send.service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.util.CommonUtils;
import com.enliple.outviserbatch.outviser.api.temp.service.ApiTempService;
import com.enliple.outviserbatch.outviser.front.exe.run.service.ExeRunService;
import com.enliple.outviserbatch.outviser.front.reg.campaign.service.RegCampaignService;
import com.enliple.outviserbatch.outviser.front.send.service.SendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CafeSendService {

	@Autowired
	private SendService sendService;

	@Autowired
	private RegCampaignService regCampaignService;

	@Autowired
	private ApiTempService apiTempService;

	@Autowired
	private ExeRunService exeRunService;

	public DataMap requestSendCafe(DataMap param) {

		DataMap result = new DataMap();
		result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "S0000");

		param.put("regCampType", "0");

		try {
			List<DataMap> requiredDatas = regCampaignService.selectRequiredDataList(param);

			if (requiredDatas.size() == 0) {
				result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "F0007");
			}

			List<DataMap> rsDatas = new ArrayList<DataMap>();

			for (DataMap requiredData : requiredDatas) {
				DataMap rsData = null;
				String exeStatus = requiredData.getString("exeStatus");
				String atlkInspectionStat = requiredData.getString("tmpDtlAtlkInspectionStatus");
				String regName = requiredData.getString("regName");

				if ("LIVE".equalsIgnoreCase(exeStatus) && "APR".equalsIgnoreCase(atlkInspectionStat)) {
					param.put("exeRowid", requiredData.get("exeRowid"));
					param.put("campRowid", requiredData.get("campRowid"));
					param.put("crmCampNo", requiredData.get("crmCampNo"));
					param.put("templateRowid", requiredData.get("templateRowid"));
					param.put("tmpDtlRowid", requiredData.get("tmpDtlRowid"));
					param.put("tranDate", requiredData.get("tranDate"));
					param.put("iVisorCampName", regName);
					param.put("regOverlapSendYn", requiredData.get("regOverlapSendYn"));
					param.put("regOverlapSendTerm", requiredData.get("regOverlapSendTerm"));
					param.put("regCampType", requiredData.get("regCampType"));
					param.put("uuid", CommonUtils.getUUID());

					try {
						// 임시 테이블 생성
						apiTempService.createTempReqDataLogic(param);

						// 집행 이력 생성
						exeRunService.insertExeRunHst(param);

						rsData = sendService.sendProcess(param).getDataMap("rsData");
						rsDatas.add(rsData);
					} catch (Exception e) {
						throw e;
					} finally {
						// 임시 테이블 삭제
						if (param.containsKey("tempTableName")) {
							apiTempService.dropTempReqData(param);
						}
					}

				} else {
					// 중지된 캠페인이면 실패 처리 안함
					if ("STOP".equalsIgnoreCase(exeStatus)) {
						continue;
					}

					DataMap failedResult = new DataMap();
					failedResult.put("errorRow", 0);
					if ("DELETE".equalsIgnoreCase(exeStatus)) {
						failedResult.put("errorMsg", "발송 요청 캠페인이 삭제된 캠페인입니다.");
					} else if (! "APR".equalsIgnoreCase(atlkInspectionStat)) {
						failedResult.put("errorMsg", "발송 요청 캠페인의 템플릿이 승인되지 않았습니다.");
					} else {
						failedResult.put("errorMsg", "알 수 없는 오류");
					}

					rsData = new DataMap();
					rsData.put("failedCount", sendService.convertReqAuto(param).size());
					rsData.put("exceptCount", 0);
					rsData.put("succedCount", 0);
					rsData.put("iVisorCampName", regName);
					rsData.put("failedResult", failedResult);
					rsDatas.add(rsData);
				}
			}

			// 한건이 넘는경우는 별도로 셋팅하자
			if (rsDatas.size() > 1) {
				int succedCnt = 0;
				int exceptCnt = 0;
				int failedCnt = 0;
				for (DataMap rsData : rsDatas) {
					if (rsData.getLong("succedCount") > 0)
						succedCnt++;
					else if (rsData.getLong("exceptCount") > 0)
						exceptCnt++;
					else if (rsData.getLong("failedCount") > 0)
						failedCnt++;
				}

				if (succedCnt > 0 && (exceptCnt + failedCnt) > 0)
					result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "S0031");
				else if (succedCnt > 0 && (exceptCnt + failedCnt) == 0)
					result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "S0030");
				else if (succedCnt == 0 && (exceptCnt + failedCnt) > 0)
					result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "F0122");
				else if (succedCnt == 0 && exceptCnt == 0 && failedCnt > 0)
					result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "E0000");
				else
					result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "E0000");
			}
			// result.put("rsData", rsDatas); CRM 변경 완료 되면 주석제거 하고 아래 0번 넣는거 제거 하자
			if (rsDatas.size() > 0)
				result.put("rsData", rsDatas.get(0));
		} catch (Exception e) {
			result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "E0000");
			result.put("rsMsg", e.getMessage());
		}

		return result;
	}
}
