package com.enliple.outviserbatch.outviser.api.action.service;

import com.enliple.outviserbatch.outviser.front.crm.mapper.CrmMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.util.CommonUtils;
import com.enliple.outviserbatch.common.util.MobtuneCrmUtil;
import com.enliple.outviserbatch.outviser.api.temp.service.ApiTempService;
import com.enliple.outviserbatch.outviser.front.exceptFilter.service.ExceptFilterService;
import com.enliple.outviserbatch.outviser.front.exe.campaign.service.ExeCampaignService;
import com.enliple.outviserbatch.outviser.front.exe.run.service.ExeRunService;
import com.enliple.outviserbatch.outviser.front.notiadmin.service.NotiadminService;
import com.enliple.outviserbatch.outviser.front.reg.campaign.service.RegCampaignService;
import com.enliple.outviserbatch.outviser.front.send.service.SendService;
import com.enliple.outviserbatch.outviser.front.template.service.TemplateService;

@Service
public class ActionService {

	@Autowired
	private ExeCampaignService exeCampaignService;

	@Autowired
	private TemplateService templateService;

	@Autowired
	private NotiadminService notiadminService;

	@Autowired
	private RegCampaignService regCampaignService;

	@Autowired
	private ApiTempService apiTempService;

	@Autowired
	private SendService sendService;

	@Autowired
	private ExeRunService exeRunService;

	@Autowired
	private ExceptFilterService exceptFilterService;

	@Autowired
	private CrmMapper crmMapper;

	public void insertRequestAction(DataMap param) throws Exception {

		if (param.containsKey("testReceiver")) {
			// 집행 처리
			requestExecutional(param);

		} else {

			if (param.getLong("exeRowid") > 0) {
				// 집행 수정
				exeCampaignService.selectExeModCheck(param);
				exeCampaignService.exeHisModType(param);
			} else {
				// 집행 생성
				exeCampaignService.insertExecutional(param);
			}

			if (param.getBoolean("executional")) {
				// 관리자에게 집행 알림 메시지 발송
				// notiadminService.sendMsgByExeStartToAdmin(param); -- 20220526 문제 없는 경우 메일 발송 주석처리 - js

				// 집행 처리
				requestExecutional(param);

				if (param.getString("regSendTimeType").equals("N")) {
					// 예산정보는 기존대로 처리
					param.remove("exeBudgetYn");
					param.remove("exeBudgetAmount");

					// 집행 상태만 스탑으로 변경
					if ("Y".equalsIgnoreCase(param.getString("nowSendYn"))) {
						param.put("exeStatus", "STOP");
					} else {
						param.put("exeStatus", "LIVE");
					}

					// 집행 수정
					exeCampaignService.exeHisModType(param);
				}

				if (param.getString("regSendTimeType").equals("API")) {
					// 예산정보는 기존대로 처리
					param.remove("exeBudgetYn");
					param.remove("exeBudgetAmount");

					// 집행 수정
					exeCampaignService.exeHisModType(param);
				}
			}
		}
	}

	/**
	 * 집행 처리
	 * 
	 * @param param
	 * @throws Exception
	 * @comment
	 */
	public void requestExecutional(DataMap param) throws Exception {

		String sendApiRowId = param.getString("sendApiRowId");

		// 테스트 발송 여부
		boolean boolTest = param.containsKey("testReceiver");

		if (boolTest) {
			param.put("templateRowid", param.get("tmpRowid"));
			param.put("tranDate", "");
			param.put("regSendTimeType", "N");
		} else {
			// 캠페인 상태 확인
			param = regCampaignService.checkCampaign(param, boolTest);
		}

		// 집행처리 템플릿 조회
		param = templateService.checkTemplate(param);

		/**
		 * 2022-05-12 임달형
		 * API 일 경우에만 값 설정
		 */
		if("API".equals(param.getString("regSendTimeType"))) {
			param.put("sendApiRowid", sendApiRowId);
		}

		/*
		 * nowSendYn
		 *  - 최초 집행시 컨트롤러에서 넘겨줌
		 *  - 값이 없는 경우(혹은 null) 예약발송 스케줄러에서 실행된 것임
		 *  
		 * 'Y' = 일반캠페인 && 즉시발송
		 * 'N' = 배송캠페인 || 예약발송
		 * null or '' = 예약발송 #스케줄러# 동작시
		 */
		String nowSendYn = param.getString("nowSendYn");
		param.put("nowSendYn", StringUtils.isBlank(nowSendYn) ? "Y" : nowSendYn);

		if ("Y".equalsIgnoreCase(param.getString("nowSendYn"))) {
			// MTS 키값을 위해 UUID 발급
			param.put("uuid", CommonUtils.getUUID());

			try {
				// 임시 테이블 생성
				apiTempService.createTempReqDataLogic(param);
				
				// 중복 수신 처리용 테이블 생성
				exceptFilterService.createTempFiterReqDataLogic(param);

				if (!boolTest) {
					// 집행 이력 생성
					exeRunService.insertExeRunHst(param);
				}

				// 발송
				DataMap result = sendService.sendProcess(param);

				if (!boolTest) {
					// 문제 없이 집행이 끝났으면 관리자에게 알림
					// notiadminService.sendMsgByExeEndToAdmin(param, result); -- 20220526 문제 없는 경우 메일 발송 주석처리 - js
				}
			} finally {
				// 임시 테이블 삭제
				if (param.containsKey("tempTableName")) {
					apiTempService.dropTempReqData(param);
				}
				
				// 중복 수신 테이블 삭제
				if (param.containsKey("tempName")) {
					exceptFilterService.dropFilterData(param);
				}
			}
		}

		// regSendTimeType : R (예약)
		if ("R".equalsIgnoreCase(param.getString("regSendTimeType"))) {

			if (param.getLong("crmCampNo") > 0) {
				String extraAmountCsvPattern  = crmMapper.selectCdpExtraAmountCsvPattern(param.getString("sessionAdverId"));
				MobtuneCrmUtil.saveCrmAddrByFile(param,extraAmountCsvPattern);
			}//emd of

			if ("Y".equalsIgnoreCase(param.getString("nowSendYn"))) {
				param.put("exeStatus", "STOP");
				exeCampaignService.exeHisModType(param);
			}
		}
	}
}
