package com.enliple.outviserbatch.outviser.api.action.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.util.ResponseUtils;
import com.enliple.outviserbatch.outviser.batch.common.BatchLauncher;

@RestController
public class ActionController {

	@Autowired
	private BatchLauncher batchLauncher;

	/**
	 * [POST] 캠페인 발송 요청
	 * 
	 * @param response
	 * @param param(senderKey, templateCode, senderKeyType(필수X))
	 * @param content-type     : appliction/x-www-form-urlencoded or raw-JSON
	 * @throws Exception
	 */
	@PostMapping(value = "/requestAction.json")
	public void requestAction(HttpServletRequest request, HttpServletResponse response, @RequestBody DataMap dataMap) throws Exception {

		DataMap result = new DataMap();
		result.setHttpStatusAndResult(DataMap.HttpStatus.BAD_REQUEST, "F0006");

		if (ObjectUtils.isNotEmpty(dataMap)) {
			dataMap.put("batchApiUrl", request.getRequestURI());
			batchLauncher.startAsync("actionBatchJobStart", dataMap);
			result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "S0000");
		}

		ResponseUtils.jsonMap(response, result);
	}


	/**
	 * [POST] SEND API 발송 요청
	 *
	 * @param response
	 * @param param(senderKey, templateCode, senderKeyType(필수X))
	 * @param content-type     : appliction/x-www-form-urlencoded or raw-JSON
	 * @throws Exception
	 */
	@PostMapping(value = "/sendApiAction.json")
	public void sendApiAction(HttpServletRequest request, HttpServletResponse response, @RequestBody DataMap dataMap) throws Exception {

		DataMap result = new DataMap();
		result.setHttpStatusAndResult(DataMap.HttpStatus.BAD_REQUEST, "F0006");

		if (ObjectUtils.isNotEmpty(dataMap)) {
			dataMap.put("batchApiUrl", request.getRequestURI());
			batchLauncher.startAsync("actionBatchJobStart", dataMap);
			result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "S0000");
		}

		ResponseUtils.jsonMap(response, result);
	}
}