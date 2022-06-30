package com.enliple.outviserbatch.schedule.template.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.enumeration.Type;
import com.enliple.outviserbatch.common.util.JsonUtils;
import com.enliple.outviserbatch.common.util.MtsUtils;
import com.enliple.outviserbatch.outviser.front.reg.campaign.service.RegCampaignService;
import com.enliple.outviserbatch.outviser.front.reg.template.service.RegTemplateService;
import com.enliple.outviserbatch.outviser.front.template.service.TemplateService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ScheduleAtalkInspectionService {

	@Autowired
	private TemplateService templateService;

	@Autowired
	private RegCampaignService regCampaignService;

	@Autowired
	private RegTemplateService regTemplateService;

	@Value("${common.mts.api}")
	private String mtsApi;

	/**
	 * @param data
	 * @throws Exception
	 */
	public DataMap inspection(DataMap data) throws Exception {

		DataMap result = new DataMap();

		String msg = "";
		String atlkSenderKey = data.getString("atlkSenderKey");

		// MTS API CALL
		DataMap call = selectByMtsCall(atlkSenderKey, data.getString("tmpDtlCode"));

		if (call == null || call.isEmpty()) {
			result.put("eCnt", 1);
			msg = String.format("result is null / tmpDtlRowid: %s", data.getInt("tmpDtlRowid"));
			log.error(msg);
			return result;
		}

		String newInspection = call.getString("inspectionStatus");
		if ("REQ".equals(newInspection)) {
			// 상태값이 검수요청이면 패스
			result.put("pCnt", 1);
			return result;
		}

		data.put("tmpDtlAtlkInspectionStatus", newInspection);

		msg = String.format("검수 상태 DB 업데이트 {} -> 템플릿명: %s / 검수결과: %s", data.getString("tmpName"), newInspection);

		// 검수 결과 업데이트
		boolean useSuccess = templateService.updateTemplateInspect(data) > 0 ? true : false;

		if (useSuccess) {
			// 템플릿 상태변경시 캠패인 상태 변경
			String templateStatus = data.getString("tmpDtlAtlkInspectionStatus");
			String regStatus = "CAMPAIGN_TEMP_READY";

			if ("APR".equals(templateStatus)) {
				regStatus = "CAMPAIGN_EXE_READY";
			} else if ("REJ".equals(templateStatus)) {
				regStatus = "CAMPAIGN_TEMP_REJ";
			} else if ("REQ".equals(templateStatus)) {
				regStatus = "CAMPAIGN_TEMP_ING";
			} else if ("REG".equals(templateStatus)) {
				regStatus = "CAMPAIGN_TEMP_READY";
			}

			if (!"APR".equals(templateStatus)) {
				data.put("TEMPLATE_ROWID", data.getInt("tmpRowid"));
				data.put("REG_STATUS", regStatus);
				regCampaignService.updateCampaignMstByTemplateRowid(data);
			} else {
				data.put("TEMPLATE_ROWID", data.getInt("tmpRowid"));
				List<DataMap> list = regTemplateService.selectTmpGrpByTmpRowid(data);
				for ( DataMap map : list ){
					data.put("ROWID",map.getInt("CAMP_ROWID"));
					data.put("REG_STATUS", regStatus);
					regCampaignService.updateCampaignMstByTemplateRowid(data);
				}
			}

			result.put("sCnt", 1);
			log.warn(msg, "성공");
		} else {
			result.put("eCnt", 1);
			log.error(msg, "실패");
		}

		return result;
	}

	private DataMap selectByMtsCall(String senderKey, String templateCode) throws Exception {

		DataMap result = null;

		StringBuilder apiUrl = new StringBuilder();
		apiUrl.append(mtsApi);
		apiUrl.append(Type.MTSApiUri.SELECT_TEMPLATE.getValue());

		DataMap apiParam = new DataMap();
		apiParam.put("senderKey", senderKey);
		apiParam.put("templateCode", templateCode);

		DataMap rsTemp = MtsUtils.callMtsApi(apiUrl.toString(), apiParam);

		String rsCode = rsTemp.getString("rsCode");
		if (StringUtils.isNotBlank(rsCode) && rsCode.indexOf("S") == 0) {
			result = JsonUtils.toDataMap(rsTemp.getString("rsData"));
		}

		return result;
	}
}
