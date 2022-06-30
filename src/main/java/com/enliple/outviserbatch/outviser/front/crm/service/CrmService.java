package com.enliple.outviserbatch.outviser.front.crm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.common.util.CommonUtils;
import com.enliple.outviserbatch.common.util.JsonUtils;
import com.enliple.outviserbatch.common.util.MobtuneCrmUtil;
import com.enliple.outviserbatch.outviser.front.crm.mapper.CrmMapper;
import com.enliple.outviserbatch.outviser.front.exceptFilter.service.ExceptFilterService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("front_crm_CrmService")
public class CrmService {

	@Autowired
	private CrmMapper crmMapper;

	@Autowired
	private ExceptFilterService exceptFilterService;

	// 추후 필요시 Mobtune CRM API 추가 : /api/v1/channels 연동 가능 채널 목록 조회 API.
	private String channelId = "01";

	/**
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public DataMap checkRun(DataMap param) throws Exception {
		DataMap resultMap = crmMapper.checkRun(param);
		if (resultMap == null) {
			throw new CommonException("CrmService > checkRun : resultMap == null", param);
		}
		return resultMap;
	}

	/**
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public int insertCrmLog(DataMap param) throws Exception {
		int resultMap = crmMapper.insertCrmLog(param);
		if (resultMap < 1) {
			throw new CommonException("CrmService > insertCrmLog : resultMap == 0", param);
		}
		return resultMap;
	}

	/**
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public int updateCrmLog(DataMap param) throws Exception {
		int resultMap = crmMapper.updateCrmLog(param);
		if (resultMap < 1) {
			throw new CommonException("CrmService > updateCrmLog : resultMap == 0", param);
		}
		return resultMap;
	}

	/**
	 * CRM 정보 확인
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public DataMap requestCampaignCrm(DataMap param) throws Exception {

		param.put("campaignNo", param.getString("crmCampNo")); // CRM 캠페인번호
		param.put("campNo", param.getString("crmCampNo")); // CRM 캠페인번호

		String adverId = param.getString("sessionAdverId");
		// [CRM] /api/{channelName}/v1/{adverId}/campaigns/{campNo} 캠페인 상세 조회 API.
		String reqUri = adverId + "/campaigns/" + param.getString("campaignNo");
		String extraAmountCsvPattern  = crmMapper.selectCdpExtraAmountCsvPattern(adverId);

		DataMap crmData = MobtuneCrmUtil.callMobtuneCrmApiMap(reqUri,extraAmountCsvPattern);
		if (crmData.getString("rsCode").contains("CRM")) { // [210929] CRM 장애 발생 시..
			crmData = MobtuneCrmUtil.getCrmDataFromFile(param, false);
		}

		DataMap crmCampData = crmData.getDataMap("rsData");

		// 광고주 ID 동일한지 체크
		if (!crmCampData.getString("adverId").toUpperCase().equals(param.getString("sessionAdverId").toUpperCase())) {
			throw new CommonException("i-Bot과 Mobtune의 광고주 계정정보가 다릅니다.", param);
		}

		// 난이도별 금액
		if (crmCampData.containsKey("extraAmount")) {
			param.put("extraAmount", crmCampData.getInt("extraAmount"));
		} else {
			throw new CommonException("CRM 캠페인 난이도 점수가 책정되지 않았습니다.", param);
		}

		if (crmCampData.containsKey("status")) {
			param.put("crmStatus", crmCampData.getInt("status")); // 1:활성화, 2:비활성화
		} else {
			throw new CommonException("CRM 캠페인 상태 정보를 확인 할 수 없습니다.", param);
		}

		if (crmCampData.containsKey("startDate")) {
			param.put("crmStartDate", crmCampData.getInt("startDate"));
		} else {
			throw new CommonException("CRM 캠페인 시작일자를 확인 할 수 없습니다.", param);
		}

		if (crmCampData.containsKey("endDate")) {
			param.put("crmEndDate", crmCampData.getInt("endDate"));
		} else {
			throw new CommonException("CRM 캠페인 종료일자를 확인 할 수 없습니다.", param);
		}

		if (crmCampData.containsKey("realtimeYn")) {
			param.put("crmRealtimeYn", crmCampData.getString("realtimeYn"));
		}

		if (param.getInt("crmStatus") != 1) {
			throw new CommonException("연동된 CRM 캠페인이 비활성화 상태 입니다.", param);
		}

		return param;
	}

	/**
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public DataMap getCrmAddress(DataMap param) throws Exception {
		DataMap result = new DataMap();
		DataMap crmData = null;

		int sendCnt = 0;
		int errorCnt = 0;
		int doubleCnt = 0;

		List<DataMap> returnIntersectData = new ArrayList<DataMap>();
		List<DataMap> viewList = new ArrayList<DataMap>();
		List<DataMap> headerViewList = new ArrayList<DataMap>();

		String adverId = param.getString("sessionAdverId");
		String campNo = param.getString("campNo");

		// CRM API 연동 파라미터 추가 20210315
		param.put("channelId", channelId);
		param.put("channelUserId", param.getString("sessionUserId"));

		// 주소록에서 조회용으로 사용할때 사용
		param.put("limit", 0);
		param.put("offset", 0);
		param.put("searchType", "all");

		/*
		 * CRM API Call to Map
		 *  - 캠페인 상세 조회 API
		 *  - 변수 주소록 변수 header 값 참고 해야 할 데이터
		 *  - /api/{channelName}/v1/{adverId}/campaigns/{campNo}
		 */

		String extraAmountCsvPattern  = crmMapper.selectCdpExtraAmountCsvPattern(adverId);

		crmData = MobtuneCrmUtil.callMobtuneCrmApiMap(adverId + "/campaigns/" + campNo, extraAmountCsvPattern);
		if (crmData.getString("rsCode").contains("CRM")) {
			crmData = MobtuneCrmUtil.getCrmDataFromFile(param, false);
		}

		crmData = crmData.getDataMap("rsData");

		/*
		 * 모수 카운트
		 *  - 데이터가 0000.0 식으로 넘어오고 있어 string -> double -> int 형식으로 형변환을 거친다.
		 */
		sendCnt = Integer.parseInt(String.format("%.0f", crmData.getDouble("sendCnt")));

		JSONArray jsonArr = null;
		Object crmObj = crmData.get("variables");
		if (crmObj instanceof JSONArray) {
			jsonArr = (JSONArray) crmObj;
		}

		// 데이터중 valiMap의 variables 의 null 체크
		if (jsonArr != null) {
			// 변수 목록
			List<DataMap> headerList = JsonUtils.toArrayDataMap(jsonArr.toString());

			// crm의 변수 값을 grid 에 맞게 수정해 준다.
			headerViewList = setHeaderViewList(headerList);
		}

		/*
		 * CRM API Call to List
		 *  - 캠페인별 사용자 목록(모수) 조회 API
		 *  - /api/{channelName}/v1/{adverId}/campaigns/{campNo}/target
		 */
		boolean needGetCrmDataFromFile=false;
		try {
			crmData = MobtuneCrmUtil.callVisorApi(adverId + "/campaigns/" + campNo + "/target", sendCnt);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			// CRM 장애 발생시 처리
			needGetCrmDataFromFile=true;
		}//end catch

		/**
		 * 22.04.19 김대현
		 *  if(CRM에 장애가 발생하거나 crmList가 비어있다.)
		 *  	=> 백업한 CRMDATA를 가져온다.
		 */
		if("R".equalsIgnoreCase(param.getString("regSendTimeType"))) {
			if (needGetCrmDataFromFile || (crmData.getArrayDataMap("crmList").isEmpty())) {
				crmData = MobtuneCrmUtil.getCrmDataFromFile(param, true);
			}//end if
		}

		result.put("rsCode", crmData.getString("rsCode"));
		result.put("rsMsg", crmData.getString("rsMsg"));

		List<DataMap> crmList = crmData.getArrayDataMap("crmList");
		if (crmList.size() > 0) {
			List<DataMap> filteredList = new ArrayList<DataMap>();

			// 전화번호 중복체크용 맵
			HashMap<String, Boolean> tempPhoneNumMap = new HashMap<String, Boolean>();
			// 이메일 중복체크용 맵
			HashMap<String, Boolean> tempEmailMap = new HashMap<String, Boolean>();

			for (DataMap cm : crmList) {
				String phoneNum = cm.getString("phoneNo");
				String email = cm.getString("email");

				// 둘 다 값이 없으면 불량 데이터임
				if (StringUtils.isBlank(phoneNum) && StringUtils.isBlank(email)) {
					errorCnt++;
					continue;
				}

				// 전화번호 또는 이메일이 중복인 경우
				if (tempPhoneNumMap.containsKey(phoneNum) || tempEmailMap.containsKey(email)) {
					doubleCnt++;
					continue;
				}

				// 중복체크용 맵에 값 등록
				if (StringUtils.isNotBlank(phoneNum)) {
					tempPhoneNumMap.put(phoneNum, true);
				}

				// 중복체크용 맵에 값 등록
				if (StringUtils.isNotBlank(email)) {
					tempEmailMap.put(email, true);
				}

				filteredList.add(cm);
			}

			// 중복 수신 검증
			if (filteredList.size() > 0) {
				List<DataMap> intersectData = null;

				DataMap userInfo = exceptFilterService.selectUserInfo(param);

				int checkDays = userInfo.getInt("acctExcDays");
				param.put("acctExcDays", checkDays);
				param.put("acctExcTmpYn", userInfo.getString("acctExcTmpYn"));

				HashMap<String, String> hashMap = new HashMap<String, String>();
				if(checkDays > 0) {
					intersectData = exceptFilterService.intersectFilterCrm(param, filteredList);
					for (DataMap intersect : intersectData) {
						hashMap.put(intersect.getString("checkKey"),"");
					}
				}

				String sessionUserRowId = param.getString("sessionUserRowId");

				String receiverKey = "phoneNo";
				if (param.getString("tmpDtlType").equals("EML")) {
					receiverKey = "email";
				}

				for (DataMap cdata : filteredList) {
					DataMap viewData = new DataMap();

					// 중복 수신 데이터 담기
					if (checkDays > 0 && intersectData.size() > 0) {
						String receiver = cdata.getString(receiverKey).trim();
						receiver = "phoneNo".equals(receiverKey) ? receiver.replaceAll("-", "") : receiver;

						if (hashMap.containsKey(receiver)) {
							returnIntersectData.add(cdata);
							continue;
						}
					}

					viewData.put("acctRowid", sessionUserRowId);

					if (param.containsKey("varList")) {
						// 템플릿에서 선택한 변수에 맞춰서 CRM 데이터를 매핑한다.
						this.setVariables(param, viewData, cdata);
					} else {
						for (DataMap headerView : headerViewList) {
							// 헤더정보에 있는 mapKey가 모수데이터 에 해당하는 key 있는지 확인한다.
							if (cdata.containsKey(headerView.get("name")) == true) {
								viewData.put(headerView.get("name"),
								cdata.get(headerView.get("name")));
							} else {
								viewData.put(headerView.get("name"), "");
							}
						}
					}

					// 전송시에는 유효성 검사 체크
					viewData = crmDataSet(viewData);

					if (isValidateCrmMap(viewData)) {
						viewList.add(viewData);
					}
				}
			}

		} else {
			result.put("rsCode", "S0002");
			result.put("rsMsg", "해당 캠페인의 모수 데이터(주소 데이터)가 없습니다.");
		}

		result.put("dataSize", sendCnt);
		result.put("errorCnt", errorCnt);
		result.put("doubleCnt", doubleCnt);
		result.put("headerList", headerViewList);
		result.put("rsData", viewList);
		result.put("intersectList", returnIntersectData);
		return result;
	}

	/**
	 * @param headerList
	 * @return
	 * @throws Exception
	 */
	private List<DataMap> setHeaderViewList(List<DataMap> headerList) throws Exception {
		List<DataMap> rtnList = new ArrayList<DataMap>();
		for (DataMap viewData : headerList) {
			DataMap tmpMap = new DataMap();

			tmpMap.put("header", viewData.get("codeName"));
			tmpMap.put("name", viewData.get("keyName"));
			tmpMap.put("align", "center");
			tmpMap.put("minWidth", "100");
			rtnList.add(tmpMap);

		}

		return rtnList;
	}

	/**
	 * 템플릿 변수 설정
	 * 
	 * @param param
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void setVariables(DataMap param, DataMap viewData, DataMap cdata) throws Exception {
		try {
			for (DataMap varData : (List<DataMap>) param.get("varList")) {
				if (varData.get("varFixedVal") == null || varData.getString("varFixedVal").equals("")) {
					if (cdata.containsKey(varData.get("varKey")))
						viewData.put(varData.get("varKey"),
								cdata.get(varData.get("varKey")).toString().equals("") ? null
										: cdata.get(varData.get("varKey")));
				} else {
					viewData.put(varData.get("varKey"), varData.get("varFixedVal"));
				}
			}
		} catch (Exception e) {
			throw new CommonException(String.format("CRM 데이터 생성중 오류가 발생했습니다. - setVariables : %s", e.getMessage()),
					param);
		}
	}

	/**
	 * @param viewData
	 * @return
	 */
	public DataMap crmDataSet(DataMap viewData) {

		DataMap resultMap = new DataMap();
		resultMap = viewData;

		String phoneNo = "";
		String email = "";

		phoneNo = viewData.getString("phoneNo", "");
		email = viewData.getString("email", "");

		if (!"".equals(phoneNo)) {
			phoneNo = CommonUtils.makePhoneOnlyNumber(phoneNo);

			if (CommonUtils.isPhoneNum(phoneNo)) {
				resultMap.put("phoneNo", phoneNo);
			} else {
				resultMap.put("phoneNo", "");
			}
		}

		if (!"".equals(email)) {

			if (CommonUtils.isValidEmail(email)) {
				resultMap.put("email", email);
			} else {
				resultMap.put("email", "");
			}
		}
                    
		return resultMap;

	}

	/**
	 * 데이터 유효성 체크 (crm 연동시 사용)
	 * 
	 * @param viewData
	 * @return
	 */
	public boolean isValidateCrmMap(DataMap viewData) {

		boolean result = true;

		if (viewData.containsKey("name") == false) {
			return false;
		} else {

			if (viewData.containsKey("phoneNo") == false && viewData.containsKey("email")) {
				return false;
			} else {

				if ("".equals(viewData.getString("phoneNo", "")) && "".equals(viewData.getString("email", ""))) {
					return false;
				}
			}
		}

		return result;
	}
}
