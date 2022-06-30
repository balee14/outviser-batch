package com.enliple.outviserbatch.outviser.front.send.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.enumeration.Type;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.common.util.CommonUtils;
import com.enliple.outviserbatch.common.util.JsonUtils;
import com.enliple.outviserbatch.common.util.MtsUtils;
import com.enliple.outviserbatch.outviser.api.payment.service.ApiUsedChargeService;
import com.enliple.outviserbatch.outviser.api.product.service.ApiProductService;
import com.enliple.outviserbatch.outviser.api.send.service.SweetTrackerSendService;
import com.enliple.outviserbatch.outviser.api.temp.service.ApiTempService;
import com.enliple.outviserbatch.outviser.front.addr.service.AddrService;
import com.enliple.outviserbatch.outviser.front.crm.service.CrmService;
import com.enliple.outviserbatch.outviser.front.exceptFilter.service.ExceptFilterService;
import com.enliple.outviserbatch.outviser.front.exe.run.service.ExeRunService;
import com.enliple.outviserbatch.outviser.front.mnwise.service.MnwiseService;
import com.enliple.outviserbatch.outviser.front.mts.service.MtsService;
import com.enliple.outviserbatch.outviser.front.reject.service.RejectService;
import com.enliple.outviserbatch.outviser.front.request.service.RequestService;
import com.enliple.outviserbatch.outviser.front.template.service.TemplateService;
import com.enliple.outviserbatch.outviser.front.urlshortener.service.UrlShortenerV2Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SendService {

	@Autowired
	private MtsService mtsService;

	@Autowired
	private MnwiseService mnwiseService;

	@Autowired
	private AddrService addrService;

	@Autowired
	private RejectService rejectService;

	@Autowired
	private TemplateService templateService;

	@Autowired
	private RequestService requestService;

	@Autowired
	private CrmService crmService;

	@Autowired
	private UrlShortenerV2Service urlShortenerV2Service;

	@Autowired
	private ApiUsedChargeService apiUsedChargeService;

	@Autowired
	private ApiProductService apiProductService;

	@Autowired
	private ExeRunService exeRunService;

	@Autowired
	private ApiTempService apiTempService;
	
	@Autowired
	private ExceptFilterService exceptFilterService;

	@Autowired
	private SweetTrackerSendService sweetTrackerService;

	@Value("${common.mts.api}")
	private String mtsApi;

	@Value("${spring.profiles.active}")
	private String activeServer;
	/**
	 * 발송 요청 모든 항목에 대해서 발송 처리를 하자
	 * 
	 * @param param
	 * @throws Exception
	 */
	public DataMap sendProcess(DataMap param) throws Exception {
		DataMap result = new DataMap();

		Long crmCampNo = param.getLong("crmCampNo");
		String regCategory = param.getString("regCategory").trim();

		/*
		 * CRM 정보 확인
		 * 단, 카페24캠페인은 캠페인 검증을 하지 않는다
		 */
		if (crmCampNo > 0 && !"CAFE".equalsIgnoreCase(regCategory)) {
			param = crmService.requestCampaignCrm(param);
		}

		// 템플릿 정보 가져오기
		DataMap templateData = templateService.selectExeTemplate(param);
		param.put("tranSenderKey", templateData.get("atlkSenderKey"));
		param.put("tmpDtlRowid", templateData.get("tmpDtlRowid"));
		param.put("tranTmplCd", templateData.get("tmpDtlCode"));
		param.put("tmpDtlAttachYn", templateData.get("tmpDtlAttachYn"));
		param.put("emailSenderName", templateData.get("emailSenderName"));
		param.put("emailSenderAddress", templateData.get("emailSenderAddress"));
		param.put("tmpUseTypeCode", templateData.get("tmpUseTypeCode"));
		param.put("tmpDtlType", templateData.getString("tmpDtlType"));
		param.put("tranCallBack", templateData.get("tranCallBack"));

		// 알림톡일 경우 템플릿 재검수
		if ("ATL".equals(templateData.getString("tmpDtlType"))) {
			DataMap rsMtsCall = this.selectByMtsCall(templateData.getString("atlkSenderKey"), templateData.getString("tmpDtlCode"));
			String newInspection = rsMtsCall.getString("inspectionStatus");

			if ("REJ".equals(newInspection)) {
				throw new CommonException(String.format("템플릿이 반려 되었습니다. 반려사유 : %s", rsMtsCall.get("comments")), param);
			} else if (!"APR".equals(newInspection)) {
				throw new CommonException("승인된 템플릿이 아닙니다.", param);
			}
		}
		// 변수 목록 가져오기
		List<DataMap> varList = templateService.selectExeTempVarList(param);

		// 그룹 변수 목록 가져오기
		List<DataMap> grpVarList = templateService.selectExeTempGrpVarList(param);

		// 요청 타입 별 분기처리 : 자동발송, 캠페인연동 발송, 엑셀 업로드 발송, 테스트 발송
		List<DataMap> reqDatas = new ArrayList<DataMap>();

		// 테스트 발송
		String testReceiver = param.getString("testReceiver");
		boolean boolTest = StringUtils.isNotBlank(testReceiver);

		// 자동 발송 구분 : 없으면 false 있으면 보내준 값
		boolean crmSend = param.getBoolean("crmSend");

		int totCount = 0;
		int exceptedCount = 0;

		if (boolTest) {
			/* 테스트 */
			param.put("exeRunHstRowid", 0);
			param.put("campRowid", 0);

			if (StringUtils.isNotBlank(param.getString("tmpUseTypeCode"))) {
				param.put("regCampType", 1);
			} else {
				param.put("regCampType", 0);
			}

			DataMap testInfo = new DataMap();
			String receiverKey = "phoneNo";
			if (testReceiver.contains("@")) {
				receiverKey = "email";
			}
			testInfo.put(receiverKey, testReceiver);

			List<DataMap> valueList = JsonUtils.toArrayDataMap(param.getString("valueList"));
			for (DataMap testVar : valueList) {
				testInfo.put(testVar.getString("name"), testVar.getString("value"));
			}
			reqDatas.add(testInfo);
		} else if (crmCampNo > 0) {
			if (crmSend) {
				/* CDP 실시간 발송 */
				reqDatas = this.convertReqAuto(param);
			} else if ("CAFE".equalsIgnoreCase(regCategory)) {
				/* CAFE24 실시간 발송 */
				reqDatas = convertReqAutoCafe24(param);
			} else {
				/* CDP 즉시 발송 */
				DataMap crmList = crmService.getCrmAddress(param);
				reqDatas = crmList.getArrayDataMap("rsData");

				List<DataMap> reqCrmIntersectList = crmList.getArrayDataMap("intersectList");
				exceptFilterService.insertIntersectFailList(param, reqCrmIntersectList);
				exceptedCount = reqCrmIntersectList.size();

				totCount = reqDatas.size() + exceptedCount;
			}
		} else if ("1".equals(param.getString("regCampType"))) {
			/* 배송 캠페인 */
			reqDatas.add(param);
		} else {
			/* 일반 캠페인 */
			if("API".equals(param.getString("regSendTimeType"))) {
				reqDatas = addrService.selectSendApiAddress(param);
			}else{
				reqDatas = addrService.selectExcelAddress(param);
			}
			totCount = reqDatas.size();

			// 중복 수신 검증
			List<DataMap> reqAddressIntersectList = exceptFilterService.exceptFilterAddr(param, reqDatas);
			exceptedCount = reqDatas.size() - reqAddressIntersectList.size();

			// 중복 수신 제외 데이터로 교체
			reqDatas = reqAddressIntersectList;
		}

		if (reqDatas.size() == 0) {
			throw new CommonException("발송 대상이 없습니다. - requestSendNew", param);
		}

		// 실시간 / 대량 분기 처리를 위한 사전 체크 : 실시간 발송 요청건이면서 건수가 1건이면 기본 에이전트 사용
		if ((param.getString("crmRealtimeYn").equals("Y") && reqDatas.size() == 1) || boolTest || "API".equals(param.getString("regSendTimeType"))) {
			param.put("mtsTableNo", ""); // 기본 에이전트는 숫자처리 안되어 있으니 공백으로
			param.put("emailTrTypeCd", "1");
		} else {
			param.put("mtsTableNo", "_BATCH_1");
			param.put("emailTrTypeCd", "9");
		}

		List<DataMap> dupExeList = new ArrayList<DataMap>();
		List<DataMap> rejectList = new ArrayList<DataMap>();

		if (!boolTest) {
			// 수신 거부 제외 대상
			rejectList = rejectService.selectRejectList(param);

			// 발송 요청 대상의 집행 이력 조회 : 집행 이력 기준으로 발송 제외 처리를 하기 위함
			// 2021-09-02 : 트랜잭션이 시작하기 전에 임시테이블 생성 -> 요청 대상 인서트 -> 이력 조회 -> 정상적으로 다 실행이 되면
			// 마지막에 임시테이블 삭제
			dupExeList = apiTempService.tempReqDataLogic(param, reqDatas);
		}

		List<DataMap> sendDatas = new ArrayList<DataMap>(); // 발송 요청 데이터
		List<DataMap> failDatas = new ArrayList<DataMap>(); // 발송 요청 실패 데이터
		List<DataMap> failDetails = new ArrayList<DataMap>(); // 발송 요청 실패 상세 데이터
		List<DataMap> exceptDatas = new ArrayList<DataMap>(); // 발송 제외 데이터
		String errorMsg = "";

		// 단축URL 마스터 정보 조회
		DataMap urlSetting = urlShortenerV2Service.getUrlSettings(param.getInt("exeRunHstRowid"),
				templateData.getInt("tmpDtlRowid"));

		// 2022-03-07 임달형 :: 친구톡 이미지 일 경우 이미지 업로드 사전에 하기. n:n => 1:n
		if (templateData.getString("tmpDtlType").equals("FTL")) {
			mtsService.insertFtalkFile(param);
		}

		long rowNum = 1;
		for (DataMap reqData : reqDatas) {
			boolean boolSend = true;
			boolean boolExcept = false;
			errorMsg = "";

			if (reqData.containsKey("이메일"))
				reqData.put("email", reqData.get("이메일"));

			// 발송 타입에 따라 핸드폰 번호, 이메일 주소가 없는 경우 실패처리
			if (templateData.getString("tmpDtlType").equals("EML")) {
				if (reqData.containsKey("email") && reqData.getString("email").equals("")) {
					errorMsg = "비정상 이메일 주소";
					boolSend = false;
				}
			} else {
				if (reqData.getString("phoneNo").equals("")) {
					errorMsg = "비정상 핸드폰 번호";
					boolSend = false;
				}
			}

			// 수신거부 제외 처리
			if (templateData.getString("tmpDtlType").equals("EML")) {
				Optional<DataMap> reject = rejectList.stream()
						.filter(data -> data.get("REJ_RECEIVER").equals(reqData.getString("email"))).findFirst();
				if (reject.isPresent()) {
					errorMsg = reject.get().getString("rejectType");
					boolSend = false;
					boolExcept = true;
				}
			} else {
				Optional<DataMap> reject = rejectList.stream()
						.filter(data -> data.get("REJ_RECEIVER").equals(reqData.getString("phoneNo"))).findFirst();
				if (reject.isPresent()) {
					errorMsg = reject.get().getString("rejectType");
					boolSend = false;
					boolExcept = true;
				}
			}

			reqData.put("tmpDtlRowid", templateData.get("tmpDtlRowid"));

			String tranSubject = templateData.getString("tmpDtlSubject");
			String tranMsg = templateData.getString("tmpDtlContent");
			String tranReplaceSubject = templateData.getString("tmpFailmsgSubject");
			String tranReplaceMsg = templateData.getString("tmpFailmsgContent");
			String tmpDtlAtlkTitle = templateData.getString("tmpDtlAtlkTitle");

			if (boolSend) {
				// 배송 그룹 변수 처리
				if (param.getString("regCampType").equals("1") && param.containsKey("tmpUseTypeCode")
						&& !param.getString("tmpUseTypeCode").equals("")) {
					String tmpGrpVarName = "";
					String tmpGrpVarContent = "";
					int forIdx = 0;

					for (DataMap grpVar : grpVarList) {
						tmpGrpVarName = grpVar.getString("tmpGrpVarName");
						if (reqData.getString(grpVar.getString("tmpGrpVarDtlName")) != null
								&& !reqData.getString(grpVar.getString("tmpGrpVarDtlName")).equals("")) {
							tmpGrpVarContent += "- " + grpVar.getString("tmpGrpVarDtlTitle") + " : "
									+ reqData.getString(grpVar.getString("tmpGrpVarDtlName"));
							if (forIdx < grpVarList.size())
								tmpGrpVarContent += "\n";

							// [20210729] 배송 그룹변수 송장번호 미노출 수정 - js
						} else if ("운송장번호".equals(grpVar.getString("tmpGrpVarDtlName"))) {
							String[] strArr = new String[] { "운송장 번호", "송장번호", "송장 번호" };
							int index = 0;
							for (String str : strArr) {
								if (reqData.containsKey(str) == true && index == 0) {
									tmpGrpVarContent += "- " + str + " : " + reqData.getString(str);
									index++;
									if (forIdx < grpVarList.size())
										tmpGrpVarContent += "\n";
								}
							}
						}
					} // for

					tranMsg = tranMsg.replace("#{" + tmpGrpVarName + "}", tmpGrpVarContent);
					tmpDtlAtlkTitle= tmpDtlAtlkTitle.replace("#{" + tmpGrpVarName + "}", tmpGrpVarContent);

					if (!templateData.get("tmpFailmsgSendtype").equals("N")) {
						tranReplaceMsg = tranReplaceMsg.replace("#{" + tmpGrpVarName + "}", tmpGrpVarContent);
					}
				}



				for (DataMap var : varList) {
					// CRM, Excel은 varKey로 되지만 고객사 페이지에 심어진 아이와 테스트, 배송추적은 varName으로 들어온다.
					String getKey = "";
					if (crmSend || boolTest || param.getString("regCampType").equals("1")){
						getKey = "varName";
					} else if ("CAFE".equalsIgnoreCase(regCategory)) {
						getKey = "VAR_KEY";
					} else {
						getKey = "varKey";
					}

					if (!reqData.containsKey(var.getString(getKey)) && var.getString("varFixedVal").isEmpty())
						boolSend = false;
					else {
						if (var.get("varFixedVal") == null || var.getString("varFixedVal").equals("")) {
							if (reqData.getString(var.getString(getKey)) != null
									&& !reqData.getString(var.getString(getKey)).equals("")) {
								String varValue = "";
								if ("payAmount|prdtPrice|orderPrice|chargePrice|결제금액|충전금액"
										.indexOf(var.getString("varName")) >= 0)
									varValue = CommonUtils.comma(reqData.getString(var.getString(getKey)));
								else if ("가입일".indexOf(var.getString("varName")) >= 0)
									varValue = reqData.getString(var.getString(getKey)).replaceAll("\\.0", "");
								else
									varValue = reqData.getString(var.getString(getKey));
								tranSubject = tranSubject.replace("#{" + var.getString("varName") + "}", varValue);
								tranMsg = tranMsg.replace("#{" + var.getString("varName") + "}", varValue);
								tmpDtlAtlkTitle = tmpDtlAtlkTitle.replace("#{" + var.getString("varName") + "}", varValue);

								if (!templateData.get("tmpFailmsgSendtype").equals("N")) {
									String varRepValue = "";
									if ("payAmount|prdtPrice|orderPrice|chargePrice|결제금액|충전금액"
											.indexOf(var.getString("varName")) >= 0)
										varRepValue = CommonUtils.comma(reqData.getString(var.getString(getKey)));
									else if ("가입일".indexOf(var.getString("varName")) >= 0)
										varRepValue = reqData.getString(var.getString(getKey)).replaceAll("\\.0", "");
									else
										varRepValue = reqData.getString(var.getString(getKey));
									tranReplaceSubject = tranReplaceSubject
											.replace("#{" + var.getString("varName") + "}", varRepValue);
									tranReplaceMsg = tranReplaceMsg.replace("#{" + var.getString("varName") + "}",
											varRepValue);
								}
							}
						} else {
							String varValue = var.getString("varFixedVal");
							tranSubject = tranSubject.replace("#{" + var.getString("varName") + "}", varValue);
							tranMsg = tranMsg.replace("#{" + var.getString("varName") + "}", varValue);
							tmpDtlAtlkTitle = tmpDtlAtlkTitle.replace("#{" + var.getString("varName") + "}", varValue);

							if (!templateData.get("tmpFailmsgSendtype").equals("N")) {
								String varRepValue = var.getString("varFixedVal");
								tranReplaceSubject = tranReplaceSubject.replace("#{" + var.getString("varName") + "}",
										varRepValue);
								tranReplaceMsg = tranReplaceMsg.replace("#{" + var.getString("varName") + "}",
										varRepValue);
							}
						}
					}
				}

				// 카카오톡 실패시 대체 문자가 설정되어 있으면
				if ("N".equals(templateData.get("tmpFailmsgSendtype")) == false) {
					reqData.put("tranReplaceType", templateData.getString("tmpFailmsgSendtype"));
					reqData.put("tranReplaceSubject", tranReplaceSubject);
					reqData.put("tranReplaceMessage", tranReplaceMsg);
					reqData.put("tranRepCallBack", templateData.get("tmpFailmsgCallback"));
				} else {
					reqData.put("tranReplaceType", "");
					reqData.put("tranReplaceSubject", "");
					reqData.put("tranReplaceMessage", "");
					reqData.put("tranRepCallBack", "");
				}

				reqData.put("sendMessage", tranMsg);

				// 단축 URL이 있으면 urlGrpKey 생성
				if (urlSetting.getBoolean("isExistLink") || urlSetting.getBoolean("isExistButton") || urlSetting.getBoolean("isExistImgLink")) {
					reqData.put("urlGrpKey", CommonUtils.getUUID());
				}

				// 메시지 단축링크 변환
				urlShortenerV2Service.replaceShortenUrlFromBody(urlSetting, param.getInt("exeRunHstRowid"), reqData);
				tranMsg = reqData.getString("sendMessage");

				// 버튼 단축링크 변환
				if (templateData.getString("tmpDtlType").equals("ATL")
						|| templateData.getString("tmpDtlType").equals("FTL")) {
					List<DataMap> btnList = urlShortenerV2Service.replaceShortenUrlFromButton(urlSetting,
							param.getInt("exeRunHstRowid"), reqData);
					ObjectMapper objectMapper = new ObjectMapper();
					String btnJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(btnList);
					btnJson = btnJson.replaceAll("(\r\n|\r|\n|\n\r|[|])", "");
					reqData.put("tranButton", btnJson.replaceAll("&quot;", "\""));
				}
				
				// 이미지 url 단축링크 변환
				String imgLink = urlShortenerV2Service.replaceShortenUrlFromImgLink(urlSetting, param.getInt("exeRunHstRowid"), reqData);
				reqData.put("imgLink", imgLink);

				if (!reqData.containsKey("urlGrpKey"))
					reqData.put("urlGrpKey", "");

				if (tranMsg.indexOf("#{") >= 0 && errorMsg.isEmpty()){
					String tranVar = tranMsg.substring(tranMsg.indexOf("#{") + 2);
					tranVar = tranVar.substring(0, tranVar.indexOf("}"));
					errorMsg = "템플릿에 등록된 변수 " + tranVar + "에 대한 치환 변수가 누락되었거나 변수명이 다릅니다.";
					boolSend = false;
				}//end if

				if(tmpDtlAtlkTitle.indexOf("#{") >= 0 && errorMsg.isEmpty() ) {
					String tranVar = tmpDtlAtlkTitle.substring(tmpDtlAtlkTitle.indexOf("#{") + 2);
					errorMsg = "템플릿에 등록된 변수 " + tranVar + "에 대한 치환 변수가 누락되었거나 변수명이 다릅니다.(알림톡 텍스트 강조형 타이틀)";
					boolSend = false;
				}//end if
			}

			reqData.put("tranCallBack", templateData.get("tranCallBack"));
			reqData.put("sendSubject", tranSubject);
			reqData.put("campRowid", param.get("campRowid"));
			reqData.put("tranSenderKey", templateData.get("atlkSenderKey"));
			reqData.put("tranTmplCd", templateData.get("tmpDtlCode"));
			reqData.put("tmpDtlRowid", templateData.get("tmpDtlRowid"));
			reqData.put("tranDate", param.get("tranDate"));
			reqData.put("uuid", param.get("uuid"));
			reqData.put("sessionUserRowId", param.get("sessionUserRowId"));
			reqData.put("exeRunHstRowid", param.get("exeRunHstRowid"));
			reqData.put("sessionAdverId", param.get("sessionAdverId"));
			reqData.put("reqSiteUserId", reqData.containsKey("userId") ? reqData.getString("userId") : null);
			reqData.put("tranEmail", reqData.getString("email"));
			reqData.put("sendPhoneNo", reqData.getString("phoneNo"));

			if (reqData.containsKey("이름"))
				reqData.put("name", reqData.get("이름"));

			if (reqData.containsKey("아이디"))
				reqData.put("reqSiteUserId", reqData.get("아이디"));

			if (reqData.containsKey("userId"))
				reqData.put("reqSiteUserId", reqData.get("userId"));

			if (!reqData.containsKey("name"))
				reqData.put("name", "");

			reqData.put("tmpDtlType", templateData.getString("tmpDtlType"));
			param.put("prodDtlType", templateData.getString("tmpDtlType"));

			if (templateData.getString("tmpDtlType").equals("ATL")) {
				reqData.put("tranTitle", tmpDtlAtlkTitle);
				reqData.put("tranType", 5);

    			if (param.getString("tmpDtlAttachYn").equals("Y")) {
    				param.put("prodDtlType", "ATLP");
    				reqData.put("tranType", 51);
    			}
    		} else if (templateData.getString("tmpDtlType").equals("FTL")) {
				if (templateData.getString("tmpDtlAttachYn").equals("Y")) { // 친구톡+이미지인 경우 별도 금액 산정이라면 ...
					param.put("prodDtlType", "FTLP");
				}
				reqData.put("tranType", 6);
			} else if (templateData.getString("tmpDtlType").equals("SMS"))
				reqData.put("tranType", 0);
			else if (templateData.getString("tmpDtlType").equals("LMS"))
				reqData.put("tranType", 4);
			else if (templateData.getString("tmpDtlType").equals("MMS"))
				reqData.put("tranType", 4);
			else if (templateData.getString("tmpDtlType").equals("EML"))
				reqData.put("tranType", 99);

			// [임시] 현재 로직상으로는 본문에 단축URL이 있으면 동일한 메시지가 발생할 수 없으므로..
			if (urlSetting.getBoolean("isExistLink") == false) {

				if ("N".equalsIgnoreCase(param.getString("regOverlapSendYn"))) {
					if (templateData.getString("tmpDtlType").equals("EML")) {
						Optional<DataMap> dupExeData = dupExeList.stream()
								.filter(data -> data.get("reqEmail").equals(reqData.getString("tranEmail"))
										&& data.get("reqMessage").equals(reqData.getString("sendMessage")))
								.findFirst();
						if (dupExeData.isPresent()) {
							errorMsg = dupExeData.get().getString("maxReqCreateDate")
									+ "에 발송 된 대상입니다.(OV_REQUEST_SEND.ROWID = " + dupExeData.get().getString("maxRowid")
									+ ")";
							boolSend = false;
							boolExcept = true;
						}
					} else {
						Optional<DataMap> dupExeData = dupExeList.stream()
								.filter(data -> data.get("reqPhoneNo").equals(reqData.getString("sendPhoneNo"))
										&& data.get("reqMessage").equals(reqData.getString("sendMessage")))
								.findFirst();
						if (dupExeData.isPresent()) {
							errorMsg = dupExeData.get().getString("maxReqCreateDate")
									+ "에 발송 된 대상입니다.(OV_REQUEST_SEND.ROWID = " + dupExeData.get().getString("maxRowid")
									+ ")";
							boolSend = false;
							boolExcept = true;
						}
					}
				}
			}

			if (boolSend)
				sendDatas.add(reqData);
			else {
				DataMap failDetail = new DataMap();
				reqData.put("errorMsg", errorMsg);
				failDetail.put("errorRow", rowNum);
				failDetail.put("errorMsg", errorMsg);
				failDetails.add(failDetail);
				failDatas.add(reqData);

				// 제외 대상도 건수 집계를 위해서만 넣어두고 실패 데이터로 보낸다.
				if (boolExcept)
					exceptDatas.add(reqData);
			}

			rowNum++;
		}

		// 단축URL 잔여 벌크 INSERT 처리
		if (urlSetting.getBoolean("isExistLink") || urlSetting.getBoolean("isExistButton") || urlSetting.getBoolean("isExistImgLink")) {
			urlShortenerV2Service.insertBulkShortenUrlDtlList(param.getInt("exeRunHstRowid"));
		}

		if (sendDatas.size() > 0) {
			// 발송대상 추가
			int loopIdx = 0;
			while (loopIdx < sendDatas.size()) {
				List<DataMap> loopSendDatas = new ArrayList<DataMap>(sendDatas.subList(loopIdx,
						sendDatas.size() - loopIdx >= 1000 ? loopIdx + 1000 : sendDatas.size()));
				requestService.insertRequestSendData(loopSendDatas);
				loopIdx += 1000;
			}

			/**
			 * 2022-06-23 임달형
			 * mts 발송시 test 세팅으로 요금 책정에서 예외 처리 하기로 논의 됨
			 * */
			param.put("mtsTests", null);
			if(boolTest || !"LIVE".equalsIgnoreCase(activeServer)){
				param.put("mtsTest", "test");
			}
			// 발송 타입별 발송 데이터 추가
			if (templateData.getString("tmpDtlType").equals("ATL"))
				mtsService.insertAtalk(param);
			else if (templateData.getString("tmpDtlType").equals("FTL"))
				mtsService.insertFtalk(param);
			else if (templateData.getString("tmpDtlType").equals("SMS"))
				mtsService.insertSms(param);
			else if (templateData.getString("tmpDtlType").equals("LMS"))
				mtsService.insertLms(param);
			else if (templateData.getString("tmpDtlType").equals("MMS"))
				mtsService.insertMms(param);
			else if (templateData.getString("tmpDtlType").equals("EML"))
				mnwiseService.insertEmail(param);
		}

		if (failDatas.size() > 0) {
			int loopFailIdx = 0;
			while (loopFailIdx < failDatas.size()) {
				List<DataMap> loopfailDatas = failDatas.subList(loopFailIdx,
						failDatas.size() - loopFailIdx >= 1000 ? loopFailIdx + 1000 : failDatas.size());
				requestService.insertSendFailData(loopfailDatas);
				loopFailIdx += 1000;
			}
		}

		DataMap rsDatas = new DataMap();
		rsDatas.put("succedCount", sendDatas.size());
		rsDatas.put("failedCount", failDatas.size() - exceptDatas.size());
		rsDatas.put("exceptCount", exceptDatas.size());
		rsDatas.put("exceptedCount", exceptedCount);
		rsDatas.put("iVisorCampName", boolTest ? "테스트발송" : param.get("iVisorCampName"));

		// 실패 데이터 포함 : 변수 처리 불가, 발송 제외
		if (failDatas.size() > 0) {
			rsDatas.put("failedResult",
					new ArrayList<DataMap>(failDetails.subList(0, failDatas.size() > 10 ? 10 : failDatas.size())));
		}

		if (sendDatas.size() > 0 && failDatas.size() > 0)
			result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "S0031");
		else if (sendDatas.size() > 0 && failDatas.size() == 0)
			result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "S0030");
		else if (sendDatas.size() == 0 && failDatas.size() > 0)
			result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "F0122");
		else if (sendDatas.size() == 0 && failDatas.size() == 0)
			result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "S0000");
		else
			result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "E0000");

		double exeSuccessAmount = 0;
		double exeDiscountAmount = 0;
		if (!boolTest) {
			// 발송 차감 정보
			DataMap contractData = apiProductService.selectContractData(param);
			// 상품단가 = 기본단가 + CRM 난이도 단가(CRM 연동이 아닌경우 0)
			double prodDtlPrice = contractData.getDouble("prodDtlPrice") + param.getDouble("extraAmount");
			double dscDtlPrice = 0;
			if (contractData.getString("dscType").equals("E")) {
				throw new CommonException("계정에 등록된 할인 정보가 잘못 적용 되었습니다. - selectContractData", param);
			} else if (contractData.getString("dscType").equals("R")) {
				// 할인액 = 상품단가 * 할인율 / 100 * -1
				dscDtlPrice = prodDtlPrice * contractData.getDouble("dscDtlRate") / 100;
			} else if (contractData.getString("dscType").equals("A")) {
				// 할인액 = 할인액
				dscDtlPrice = contractData.getDouble("dscDtlAmount");
			}

			if (dscDtlPrice > 0) {
				// 할인 금액이 발송 금액보다 클경우 발송 금액 만큼만 차감처리(할인정보 잘못 생성한 경우 우려)
				if (dscDtlPrice > prodDtlPrice)
					dscDtlPrice = prodDtlPrice;
				dscDtlPrice = dscDtlPrice * -1;
			}

			// 배송추적 캠페인의 경우 최초 발송할때 배송추적 비용 차감처리
			double swtProdDtlPrice = 0;
			double swtDscDtlPrice = 0;
			if (param.getString("regCampType").equals("1")) {
				DataMap sweetChargeData = sweetTrackerService.selectSweetTrackerCharge(param);
				if (sweetChargeData.getString("tmpDtlRowid").equals(param.getString("tmpDtlRowid"))
						&& sweetChargeData.getString("tmpUseTypeCode").equals(param.getString("tmpUseTypeCode"))) {
					String tempProdDtlType = param.getString("prodDtlType");
					param.put("prodDtlType", "SWT");
					DataMap swtContractData = apiProductService.selectContractData(param);
					param.put("prodDtlType", tempProdDtlType);
					swtProdDtlPrice = swtContractData.getDouble("prodDtlPrice");
					if (swtContractData.getString("dscType").equals("E")) {
						throw new CommonException("계정에 등록된 할인 정보가 잘못 적용 되었습니다. - selectContractData", param);
					} else if (swtContractData.getString("dscType").equals("R")) {
						swtDscDtlPrice = (swtProdDtlPrice * swtContractData.getDouble("dscDtlRate") / 100) * -1;
					} else if (swtContractData.getString("dscType").equals("A")) {
						swtDscDtlPrice = swtContractData.getDouble("dscDtlAmount");
						if (swtDscDtlPrice > swtProdDtlPrice)
							swtDscDtlPrice = swtProdDtlPrice;
						swtDscDtlPrice = swtDscDtlPrice * -1;
					}
				}
			}

			exeSuccessAmount = (prodDtlPrice + swtProdDtlPrice) * sendDatas.size();
			exeDiscountAmount = (dscDtlPrice + swtDscDtlPrice) * sendDatas.size();

			param.put("exeRunStatus", "DONE");
			param.put("exeRunTotCount", totCount);//reqDatas.size());
			param.put("exeSuccessCount", sendDatas.size());
			param.put("exeFailuerCount", failDatas.size() + exceptedCount);
			param.put("exeSuccessAmount", exeSuccessAmount);
			param.put("exeDiscountAmount", exeDiscountAmount);
			param.put("exeUnitAmount", contractData.getDouble("prodDtlPrice")); // 건당 기본 단가
			param.put("exeUnitExtraAmount", param.getDouble("extraAmount")); // 건당 CRM 난이도 단가

			DataMap subContParam = new DataMap();
			subContParam.put("sessionUserRowId", param.getInt("sessionUserRowId"));

			// 전환발송이 설정 되어 있으면..
			if ("S".equals(templateData.get("tmpFailmsgSendtype"))) {
				subContParam.put("prodDtlType", "SMS");
				DataMap smsContractData = apiProductService.selectContractData(subContParam);
				param.put("replaceUnitAmount", smsContractData.getDouble("prodDtlPrice")); // SMS 전환발송 건당 기본 단가
			} else if ("L".equals(templateData.get("tmpFailmsgSendtype"))) {
				subContParam.put("prodDtlType", "LMS");
				DataMap lmsContractData = apiProductService.selectContractData(subContParam);
				param.put("replaceUnitAmount", lmsContractData.getDouble("prodDtlPrice")); // LMS 전환발송 건당 기본 단가
			} else if ("M".equals(templateData.get("tmpFailmsgSendtype"))) {
				subContParam.put("prodDtlType", "MMS");
				DataMap mmsContractData = apiProductService.selectContractData(subContParam);
				param.put("replaceUnitAmount", mmsContractData.getDouble("prodDtlPrice")); // MMS 전환발송 건당 기본 단가
			}

			// 사용금액 차감 처리
			double reqAmount = exeSuccessAmount + exeDiscountAmount;
			if (reqAmount > 0) {
				param.put("reqAmount", reqAmount * -1);
				param.put("usedSendType", templateData.getString("tmpDtlType"));

				apiUsedChargeService.insertUsedCharge(param);
			}

			exeRunService.updateExeRunHst(param);
		}

		result.put("rsData", rsDatas);

		return result;
	}

	/**
	 * 자동 발송 요청데이터 컨버트
	 * 
	 * @param param
	 * @throws JSONException
	 * @throws Exception
	 */
	public List<DataMap> convertReqAuto(DataMap param) throws Exception, JSONException {

		List<DataMap> befDatas = JsonUtils.toArrayDataMap(param.getString("reqDatas").replaceAll("&quot;", "\""));
		List<DataMap> aftDatas = new ArrayList<DataMap>();

		for (DataMap data : befDatas) {
			DataMap tempData = new DataMap();
			tempData.put("phoneNo", data.getString("tranPhone"));
			for (DataMap var : JsonUtils.toArrayDataMap(data.get("tranVariable").toString()))
				tempData.put(var.getString("n"), var.getString("v"));
			aftDatas.add(tempData);
		}

		return aftDatas;
	}

	/**
	 * 자동 발송 요청데이터 컨버트(cafe24용 단건 발송 목적)
	 *
	 * @param param
	 * @throws JSONException
	 * @throws Exception
	 */
	private List<DataMap> convertReqAutoCafe24(DataMap param) throws Exception, JSONException {

		String reqDatas = param.getString("reqDatas").replaceAll("&quot;", "\"");
		if (reqDatas.startsWith("[")) {
			return JsonUtils.toArrayDataMap(reqDatas);
		} else {
			DataMap befDatas = JsonUtils.toDataMap(reqDatas);
			List<DataMap> aftDatas = new ArrayList<DataMap>();
			aftDatas.add(befDatas);
			return aftDatas;
		}
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