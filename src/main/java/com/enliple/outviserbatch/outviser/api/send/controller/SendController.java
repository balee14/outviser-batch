package com.enliple.outviserbatch.outviser.api.send.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ObjectUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enliple.outviserbatch.common.bind.annotation.CommondMap;
import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.util.ResponseUtils;
import com.enliple.outviserbatch.outviser.batch.common.BatchLauncher;
import com.enliple.outviserbatch.outviser.batch.util.BatchContext;

@RequestMapping("/api/send")
@RestController
public class SendController {

	@Autowired
	private BatchLauncher batchLauncher;

	/**
	 * 캠페인 발송 요청
	 * <br> CRM 서비스측 실시간 발송 용도로 사용
	 * 
	 * @param response
	 * @param dataMap
	 * @throws Exception
	 */
	@PostMapping(value = "/crm.json")
	public void requestSend(HttpServletRequest request, HttpServletResponse response, @CommondMap DataMap dataMap) throws Exception {

		String resultCode = "F0004";

		DataMap rsData = null;

		Object obj = request.getAttribute("sessionUserInfo");
		if (obj instanceof DataMap) {
			dataMap.addAll((DataMap) obj);
		}

		if (ObjectUtils.isNotEmpty(dataMap)) {

			if (dataMap.getInt("campaignNo") <= 0) {
				resultCode = "F0006";
			} else {
				dataMap.put("batchApiUrl", request.getRequestURI());
				DataMap result = batchLauncher.start("crmSendBatchJob", dataMap);

				obj = result.get(BatchContext.MAP_OBJECT_KEY);
				if (obj instanceof DataMap) {
					rsData = (DataMap) obj;
				} else {
					resultCode = "E0000";
				}
			}
		}

		if (ObjectUtils.isEmpty(rsData)) {
			rsData = new DataMap();
			rsData.setHttpStatusAndResult(DataMap.HttpStatus.BAD_REQUEST, resultCode);
		}

		ResponseUtils.jsonMap(response, rsData);
	}

	/**
	 * 캠페인 발송 요청
	 * <br> 배송 서비스측 콜백 처리에 대한 발송
	 * 
	 * @param dataMap
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = "/sweetTracker/getData")
	public String requestTracker(HttpServletRequest request, @RequestBody DataMap dataMap) throws Exception {

		JSONObject jsonObj = new JSONObject();
		jsonObj.put("code", false);
		jsonObj.put("message", "failure");

		if (ObjectUtils.isNotEmpty(dataMap)) {
			dataMap.put("batchApiUrl", request.getRequestURI());
			DataMap result = batchLauncher.start("sweetTrackerSendBatchJob", dataMap);

			Object obj = result.get(BatchContext.MAP_OBJECT_KEY);
			if (obj instanceof JSONObject) {
				jsonObj = (JSONObject) obj;
			}
		}

		return jsonObj.toString();
	}

	@PostMapping(value = "/sweetTracker/nonCrmCampDeliveryTracking.json")
	public void nonCrmCampDeliveryTracking(HttpServletRequest request, HttpServletResponse response, @RequestBody DataMap dataMap) throws Exception {

		DataMap result = new DataMap();
		result.setHttpStatusAndResult(DataMap.HttpStatus.BAD_REQUEST, "F0006");

		if (ObjectUtils.isNotEmpty(dataMap)) {
			dataMap.put("batchApiUrl", request.getRequestURI());
			result = batchLauncher.start("deliveryTrackingSendBatchJob", dataMap);

			Object obj = result.get(BatchContext.MAP_OBJECT_KEY);
			if (obj instanceof DataMap) {
				result = (DataMap) obj;
			}
		}

		ResponseUtils.jsonMap(response, result);
	}
	
		/**
	 * 캠페인 발송 요청
	 * <br> CRM 서비스측 실시간 발송 용도로 사용
	 *
	 * @param response
	 * @param dataMap
	 * @throws Exception
	 */
	@PostMapping(value = "/cafe.json")
	public void requestSendCafe24(HttpServletRequest request, HttpServletResponse response, @CommondMap DataMap dataMap) throws Exception {

		String resultCode = "F0004";

		DataMap rsData = null;

		Object obj = request.getAttribute("sessionUserInfo");
		if (obj instanceof DataMap) {
			dataMap.addAll((DataMap) obj);
		}

		if (ObjectUtils.isNotEmpty(dataMap)) {

			if (dataMap.getInt("campaignNo") <= 0) {
				resultCode = "F0006";
			} else {
				dataMap.put("batchApiUrl", request.getRequestURI());
				DataMap result = batchLauncher.start("cafeSendBatchJob", dataMap);

				obj = result.get(BatchContext.MAP_OBJECT_KEY);
				if (obj instanceof DataMap) {
					rsData = (DataMap) obj;
				} else {
					resultCode = "E0000";
				}
			}
		}

		if (ObjectUtils.isEmpty(rsData)) {
			rsData = new DataMap();
			rsData.setHttpStatusAndResult(DataMap.HttpStatus.BAD_REQUEST, resultCode);
		}

		ResponseUtils.jsonMap(response, rsData);
	}
}