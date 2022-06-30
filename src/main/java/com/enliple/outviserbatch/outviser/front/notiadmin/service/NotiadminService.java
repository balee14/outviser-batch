package com.enliple.outviserbatch.outviser.front.notiadmin.service;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.service.notification.service.Notification;
import com.enliple.outviserbatch.common.util.MobtuneCrmUtil;
import com.enliple.outviserbatch.outviser.front.exe.campaign.service.ExeCampaignService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("front_notiadmin_NotiadminService")
public class NotiadminService {

	@Autowired
	private ExeCampaignService exeCampaignService;

	@Autowired
	private Notification notify;

	@Value("${spring.profiles.active}")
	private String activeServer;

	@Value("${mobtune.visor.api.url}")
	private String visorApi;

	/**
	 * 관리자에게 집행 알림 메시지 발송
	 * 
	 * @param param
	 * @throws Exception
	 */
	public void sendMsgByExeStartToAdmin(DataMap param) throws Exception {

		DataMap exeMap = exeCampaignService.selectExeInfoForAdmin(param);

		if (exeMap != null) {
			StringBuilder stb = new StringBuilder();

			stb.append(getDefaultSubject(0, param));
			stb.append(" ").append(param.getLong("exeRowid"));
			stb.append(" ").append(exeMap.getString("REG_SEND_TIME_TYPE_NM"));
			stb.append(" ").append("STOP".equalsIgnoreCase(param.getString("exeStatus")) ? "취소" : "집행");

			if (exeMap.getInt("REG_CRM_NO") == 0) {
				stb.append(" ").append(exeMap.getInt("ADDR_CNT")).append("건");
			} else if (StringUtils.isNotBlank(exeMap.getString("ADVER_ID"))) {
				try {
					// REG_CRM_NO가 0보다 크고, ADVER_ID가 있으면.. CRM 모수 조회
					String apiFullUrl = String.format("%s%s/campaigns/%s", visorApi, exeMap.getString("ADVER_ID"), exeMap.getString("REG_CRM_NO"));
					DataMap crmCampData = MobtuneCrmUtil.callMobtuneCrmApiMap(apiFullUrl).getDataMap("rsData");

					// 소수점 0 제거 (https://pythonq.com/so/java/531726)
					BigDecimal bd = new BigDecimal(crmCampData.getString("sendCnt"));
					stb.append(" ").append(bd.stripTrailingZeros().toPlainString()).append("건(예상)");
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}

			String subject = stb.toString();
			stb.setLength(0);

			// 메일 body 구성
			stb.append(String.format("* 캠페인명 : [%d] %s", param.getInt("campRowid"), exeMap.getString("REG_NAME")));

			if (StringUtils.isNotBlank(exeMap.getString("REG_MEMO"))) {
				stb.append("<br>");
				stb.append(String.format("* 메모 : %s", exeMap.getString("REG_MEMO")));
			}

			stb.append("<br>");
			stb.append(String.format("* 템플릿 : [%d] %s", exeMap.getInt("TEMPLATE_ROWID"), exeMap.getString("TMP_NAME")));

			if ("R".equals(exeMap.getString("REG_SEND_TIME_TYPE"))) {
				stb.append("<br>");
				stb.append(String.format("* 예약일시 : %s %s", exeMap.getString("REG_SEND_START_DATE"), exeMap.getString("REG_SEND_START_TIME")));
			}

			stb.append("<br>");
			if (exeMap.getInt("REG_CRM_NO") > 0) {
				stb.append(String.format("* CRM 연동 [%d]", exeMap.getInt("REG_CRM_NO")));
			} else {
				stb.append(String.format("* 주소록 : [%d] %s", exeMap.getInt("ADDR_GRP_ROWID"), exeMap.getString("ADDR_GRP_NM")));
			}

			stb.append("<br>");
			stb.append(String.format("* 발송 타입 : %s", exeMap.getString("TMP_DTL_TYPE_NM")));

			stb.append("<br>");
			stb.append(String.format("* 관리자 유무 : %s", param.getString("sessionSuperAdminYn")));

			DataMap dataMap = new DataMap();
			dataMap.put("subject", subject);
			dataMap.put("message", stb.toString());
			notify.alert(false, dataMap);
		}
	}

	/**
	 * 관리자에게 발송 결과 알림 메시지 발송
	 * 
	 * @param param
	 * @param result
	 */
	public void sendMsgByExeEndToAdmin(DataMap param, DataMap result) {

		DataMap rsData = result.getDataMap("rsData");

		// 메일 body 구성
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("* 성공 : %d건", rsData.getInt("succedCount")));
		sb.append("<br>");
		sb.append(String.format("* 제외 : %d건", rsData.getInt("exceptCount")));
		sb.append("<br>");
		sb.append(String.format("* 중복 수신 : %d건", rsData.getInt("exceptedCount")));
		sb.append("<br>");
		sb.append(String.format("* 실패 : %d건", rsData.getInt("failedCount")));
		sb.append("<br>");
		sb.append(rsData.getString("failedResult"));

		String msg = sb.toString();
		sb.setLength(0);

		int resultType = (result.getString("rsCode").indexOf("S") == 0) ? 0 : 1;

		sb.append(getDefaultSubject(resultType, param));
		sb.append(" ").append(param.getLong("exeRowid"));
		sb.append(" ").append("집행 완료");

		DataMap dataMap = new DataMap();

		dataMap.put("subject", String.format("%s(%d/%d/%d/%d)", sb.toString(), rsData.getInt("succedCount"), rsData.getInt("exceptCount"), rsData.getInt("exceptedCount"), rsData.getInt("failedCount")));

		dataMap.put("message", msg);
		notify.alert(false, dataMap);
	}

	private String getDefaultSubject(int type, DataMap param) {

		String system = "LIVE".equalsIgnoreCase(activeServer) ? "" : activeServer;
		String typeToMsg = (type == 0) ? "SUCCESS" : "FAILURE";

		String subject = String.format("%s[아이센드 알림_%s] ", system, typeToMsg);
		subject += param.getString("sessionBizName");
		subject += String.format("(%s,%s)", param.getString("sessionUserId"), param.getString("sessionUserRowId"));

		return subject;
	}
}
