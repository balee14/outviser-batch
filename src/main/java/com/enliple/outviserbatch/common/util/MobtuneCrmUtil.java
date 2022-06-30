package com.enliple.outviserbatch.common.util;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.data.RestApiResultVO;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.common.statics.StaticCampCondScore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class MobtuneCrmUtil {

	private final static String activeServer = PropertiesUtils.getValue("spring.profiles.active");

	private final static String fileUploadPhysical = PropertiesUtils.getValue("file.upload.physical");

	private final static String mobtuneVisorApiUrl = PropertiesUtils.getValue("mobtune.visor.api.url");

	private final static int REQ_LIMIT = PropertiesUtils.getInt("common.crm.limit");

	/**
	 * CRM 맵 형식으로 데이터 받을때 (NO param, No extraAmount)
	 * @param apiUrl	API URL
	 * @return
	 * @throws Exception
	 */
	public static DataMap callMobtuneCrmApiMap(String apiUrl) throws Exception {
		return callMobtuneCrmApiMap(apiUrl, null, "");
	}//callMobtuneCrmApiMap

	/**
	 * CRM 맵 형식으로 데이터 받을때 (NO param)
	 * @param apiUrl	API URL
	 * @param extraAmountCsvPattern - 추가요금을 조회 원할시 OV_CDP_EXTRA_AMOUNT에서 조회한 pattern
	 * @return
	 * @throws Exception
	 */
	public static DataMap callMobtuneCrmApiMap(String apiUrl, String extraAmountCsvPattern) throws Exception {
		return callMobtuneCrmApiMap(apiUrl, null, extraAmountCsvPattern);
	}//callMobtuneCrmApiMap

	/**
	 * CRM 맵 형식으로 데이터 받을때 (No extraAmount)
	 * @param apiUrl 	API URL
	 * @param param		API로 보낼 PARAM
	 * @return
	 * @throws Exception
	 */
	public static DataMap callMobtuneCrmApiMap(String apiUrl, DataMap param) throws Exception {
		return callMobtuneCrmApiMap(apiUrl, param, "");
	}//callMobtuneCrmApiMap

	/**
	 * CRM 맵 형식으로 데이터 받을때
	 * @param apiUrl 	API URL
	 * @param param		API로 보낼 PARAM
	 * @param extraAmountCsvPattern - 추가요금을 조회 원할시 OV_CDP_EXTRA_AMOUNT에서 조회한 pattern
	 * @return
	 * @throws Exception
	 */
	public static DataMap callMobtuneCrmApiMap(String apiUrl, DataMap param, String extraAmountCsvPattern) throws CommonException {
		if (! apiUrl.contains("http")) {
			apiUrl = mobtuneVisorApiUrl + apiUrl;
		}

		log.info("crm : callMobtuneCrmApiMap >> req apiUrl : {}", apiUrl);
		log.info("crm : callMobtuneCrmApiMap >> req param : {}", param);

		DataMap result = new DataMap();
		String rtnCode = "";

		// MOBTUNE CRM API는 JSON 으로 호출이 되어야 한다.
		RestApiResultVO apiResult;
		if (param == null) {
			apiResult = RestApiUtils.callRestApi(apiUrl, "GET", "JSON", "");
		} else {
			apiResult = RestApiUtils.callRestApi(apiUrl, "GET", "JSON", param);
		}

		// 통신 Check
		rtnCode = apiResult.getHttpStatus().toString();

		log.info("crm : callMobtuneCrmApiMap >> res data : {}", apiResult.getBody());

		// 통신 결과 코드가 200이 아니면 API에서 제공해준 메세지를 찍는다.
		if (!rtnCode.contains("200")) {
			result.setHttpStatusAndResult(DataMap.HttpStatus.INTERNAL_SERVER_ERROR, "CRM" + rtnCode);
			result.put("rsMsg", "CRM 통신오류 입니다. 오류코드:" + rtnCode);
			return result;
		} else {
			result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "S0000");

			if (apiResult.getBody() != null) {

				try {
					DataMap apiRstBody = JsonUtils.toDataMap(apiResult.getBody());
					// data가 없는 경우가 있으니 초기값은 null로 해두자
					result.put("rsData", null);

					if (apiRstBody.containsKey("data")) {
						if (apiRstBody.get("data").getClass().getName().equals("org.json.JSONObject")) {
							// json이면 data에 작업해서 rsData로 담자
							DataMap rsData = JsonUtils.toDataMap(apiResult.getBody()).getDataMap("data");

							/* 난이도 점수 임시 처리 */
							if (rsData.containsKey("conditions")) {
								List<DataMap> condList = JsonUtils.toArrayDataMap(rsData.getString("conditions"));
								int score = getScoreByCondName(condList);
								rsData.put("score", score);
								//int extraAmount = getExtraAmountByScore(rsData);
								/* 난이도 점수 임시 처리 - 끝 */
								//22.04.12 CDP연동 요금을 "난이도" 별  -> "조건 수" 별로 변경
								if(StringUtils.isNotBlank(extraAmountCsvPattern)){
									int extraAmount = getExtraAmountByConditionCnt(extraAmountCsvPattern, getFilterCond(condList));//condList.size());
									rsData.put("extraAmount", extraAmount); // 난이도 점수에 따른 추가 요금
								}//end if
							}

							result.put("rsData", rsData);
						} else {
							// json이 아니면 api 결과를 rsData로 담자... 그냥... 모르겠다. 에라이
							result.put("rsData", apiRstBody);
						}
					}
				} catch (JSONException e) {
					result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "E0000");
					result.put("data", "");
					result.put("rsMsg", "데이터가 없습니다.");
				}
			}
		}

		return result;
	}

	/**
	 * CRM 리스트 형식으로 데이터 받을때
	 * 
	 * @param apiUrl
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static List<DataMap> callApiList(String apiUrl, DataMap param) throws Exception {
		List<DataMap> crmList;

		long time = System.currentTimeMillis();
		log.info("[CRM CALL START] {} - param > {}", apiUrl, param);

		RestApiResultVO apiResult = RestApiUtils.callRestApi(apiUrl, "GET", "JSON", param);

		log.info("[CRM CALL END - sec:{}] {} - param > {}", (System.currentTimeMillis() - time) / 1000, apiUrl, param);

		// 통신 Check
		String rtnCode = apiResult.getHttpStatus().toString();
		if (rtnCode.contains("200")) {
			String resultBody = apiResult.getBody();
			DataMap crmMap = JsonUtils.toDataMap(resultBody);
			crmList = JsonUtils.toArrayDataMap(crmMap.getString("data"));
		} else {
			throw new CommonException(String.format("[CODE %s] CRM 통신 오류 / reqUrl: %s", rtnCode, apiUrl), param);
		}

		return crmList;
	}

	public static DataMap callVisorApi(String reqUri) throws Exception {
		return callVisorApi(reqUri, -1);
	}

	public static DataMap callVisorApi(String reqUri, int sendCnt) throws Exception {
		if (! reqUri.contains("http")) {
			reqUri = mobtuneVisorApiUrl + reqUri;
		}

		DataMap param = new DataMap();

		int limit = 0;
		String searchType = "all";
		boolean isPaging = false;

		if (sendCnt >= REQ_LIMIT) {
			limit = REQ_LIMIT;
			searchType = "limit";
			isPaging = true;
			param.put("sendCnt", sendCnt);
		}

		param.put("offset", 0);
		param.put("limit", limit);
		param.put("searchType", searchType);
		param.put("isPaging", isPaging);

		DataMap result = callVisorApi(reqUri, param, new DataMap());
		result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "S0000");

		return result;
	}

	private static DataMap callVisorApi(String apiUrl, DataMap param, DataMap saveData) throws Exception {
		List<DataMap> crmList = callApiList(apiUrl, param);

		if (ObjectUtils.isNotEmpty(saveData)) {
			crmList.addAll(saveData.getArrayDataMap("crmList"));
			saveData.clear();
		}
		saveData.put("crmList", crmList);

		if (param.getBoolean("isPaging")) {
			int offset = param.getInt("offset");
			offset += param.getInt("limit");

			if (param.getInt("sendCnt") > offset) {
				param.put("offset", offset);
			} else {
				return saveData;
			}

			/*
			 * 재귀 호출
			 */
			return callVisorApi(apiUrl, param, saveData);
		}

		return saveData;
	}

	/**
	 * CRM 데이터를 파일로 저장
	 * 
	 * @param param
	 */
	public static void saveCrmAddrByFile(DataMap param, String extraAmountCsvPattern) {
		String adverId = param.getString("sessionAdverId");
		String campNo = param.getString("crmCampNo");

		// 업로드 경로
		String filePath = fileUploadPhysical + "/crm";
		File path = new File(filePath);
		if (! path.exists()) {
			path.mkdir();
		}
		filePath = String.format("%s/%s_%s", filePath, adverId, campNo);

		Writer writer1 = null, writer2 = null;
		try {
			// 캠페인 상세 조회
			DataMap crmData = callMobtuneCrmApiMap(adverId + "/campaigns/" + campNo, extraAmountCsvPattern);
			String jsonToStr = JsonUtils.toString(crmData);
			if ("live".contains(activeServer)) {
				jsonToStr = DesEncryptUtils.encrypt(jsonToStr);
			}

			writer1 = new BufferedWriter(new FileWriter(String.format("%s.txt", filePath)));
			writer1.write(jsonToStr);
			writer1.flush();

			// 캠페인별 모수 조회
			crmData = callVisorApi(adverId + "/campaigns/" + campNo + "/target");
			jsonToStr = JsonUtils.toString(crmData);
			if ("live".contains(activeServer)) {
				jsonToStr = DesEncryptUtils.encrypt(jsonToStr);
			}

			writer2 = new BufferedWriter(new FileWriter(String.format("%s_list.txt", filePath)));
			writer2.write(jsonToStr);
			writer2.flush();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(writer1);
			IOUtils.closeQuietly(writer2);
		}
	}

	/**
	 * 저장해둔 CRM 데이터 조회
	 * 
	 * @param param
	 * @param isList
	 * @return
	 */
	public static DataMap getCrmDataFromFile(DataMap param, boolean isList) {
		String adverId = param.getString("sessionAdverId");
		String campNo = param.getString("campNo");

		String fileFullPath = String.format("%s/crm/%s_%s", fileUploadPhysical, adverId, campNo);
		fileFullPath += isList ? "_list.txt" : ".txt";

		DataMap crmData = null;

		File file = new File(fileFullPath);
		if (file.exists()) {
			try (Scanner scanner = new Scanner(file)) {
				StringBuilder sb = new StringBuilder();

				while (scanner.hasNextLine()) {
					sb.append(scanner.nextLine());
				}

				String strCrm = sb.toString();
				if ("live".contains(activeServer)) {
					strCrm = DesEncryptUtils.decrypt(strCrm);
				}

				crmData = JsonUtils.toDataMap(strCrm);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		if (ObjectUtils.isEmpty(crmData)) {
			crmData = new DataMap();
		}

		return crmData;
	}

	/**
	 * [임시] 난이도 점수 계산 (추후 CRM에서 계산해서 주면 삭제)
	 * 
	 * @param condName
	 * @return
	 */
	private static int getScoreByCondName(List<DataMap> condList) {
		int score = 0;

		for (DataMap cond : condList) {
			score += StaticCampCondScore.getDataMap().getInt(cond.getString("condKey"));
		}

		return score;
	}

	/**
	 * 2022-05-12 임달형
	 * @return filtered cnt
	 */
	public static int getFilterCond(List<DataMap> condList) {
		int cnt = 0;

		for (DataMap cond : condList) {
			if(condCheck(cond.getString("condKey"))){
				cnt++;
			}
		}

		return cnt;
	}

	public static boolean condCheck(String condkey){
		boolean boo = false;

		String[] array = {"01","11","16"};

		if(!Arrays.asList(array).contains(condkey)){
			boo = true;
		}

		return boo;
	}

	/**
	 * 난이도 점수별 추가 금액 조회
	 * 
	 * @param score
	 * @return
	 */
	private static int getExtraAmountByScore(DataMap dataMap) {

		String user = dataMap.getString("adverId");

		// 예외 사용자
		if ("justone".equalsIgnoreCase(user)) {
			log.info("User[justone] extraAmount 0");
			return 0;
		} else if ("abm_nymall".equalsIgnoreCase(user)) {
			log.info("User[abm_nymall] extraAmount 0");
			return 0;
		} else if ("careb".equalsIgnoreCase(user)) {
			log.info("User[careb] extraAmount 0");
			return 0;
		} else if ("ffull".equalsIgnoreCase(user)) {
			log.info("User[ffull] extraAmount 5");
			return 5;
		}

		int extraAmount = 0;
		int score = dataMap.getInt("score");

		if (1 <= score && score <= 2) {
			extraAmount = 0;
		} else if (3 <= score && score <= 5) {
			extraAmount = 10;
		} else if (6 <= score && score <= 8) {
			extraAmount = 20;
		} else if (9 <= score && score <= 10) {
			extraAmount = 30;
		} else if (11 <= score && score <= 13) {
			extraAmount = 40;
		} else if (14 <= score) {
			extraAmount = 50;
		}

		return extraAmount;
	}

	/**
	 * 2022.04.26 김대현
	 * OV_CDP_EXTRA_AMOUNT 테이블에서 SELECT한 CSV pattern을 받아
	 * 해당 패턴과 매핑되는 조건수에 따른 각 요금을 RETURUN
	 * @param csvPattern
	 * @param condCount
	 * @return
	 */
	private static int  getExtraAmountByConditionCnt(String extraAmountCsvPattern, int conditionCnt){
		//최종 return할 mapper
		List<Integer> mappingList = new ArrayList<>();

		/*
		 * 0번 index의 값은 조건 1개의 가격
		 * 1번 index의 값은 조건 2개의 가격
		 * 배열의 크기가 5 이면 , 조건이 5개부터 그 이상의 조건수는 가격이 동일하다는 뜻
		 */
		List<Integer> csvMappingList = Stream.of(extraAmountCsvPattern.split(","))
									.map(s-> Integer.parseInt(s))
									.collect(Collectors.toList());

		/*
		 *	배열의 크기와 조건수를 비교하여, min값을 구한다.
		 * 		-> 조건수가 배열의 크기보다
		 * 			-->	작으면 조건수 만큼 가격 책정을 하면된다.
		 * 			--> 크다면 배열의 크기로 가격 책정을 하면된다.
		 */
		conditionCnt=Math.min(csvMappingList.size(), conditionCnt);

		/*
		 * index번호와 조건 수를 맞춰주기위해서
		 * mappingList.add(csvMappingList.get(0))한뒤
		 * csvMappingList 를 mappingList에 삽입
		 */
		mappingList.add(csvMappingList.get(0));
		mappingList.addAll(csvMappingList);

		return mappingList.get(conditionCnt);
	}//getExtraAmountByConditionCnt

}//class
