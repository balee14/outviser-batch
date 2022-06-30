package com.enliple.outviserbatch.outviser.batch.service;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.util.JsonUtils;
import com.enliple.outviserbatch.outviser.batch.mapper.BatchApiCallParamsMapper;

@Service
public class BatchApiCallParamsService {

	@Autowired
	private BatchApiCallParamsMapper batchApiCallParamsMapper;

	public String insertBatchApiCallParams(DataMap param) {

		String batchApiUrl = "";
		try {
			batchApiUrl = param.remove("batchApiUrl").toString();
		} catch (Exception e) {}

		String requestJsonStr = JsonUtils.toString(param);

		DataMap dataMap = new DataMap();
		dataMap.put("batchApiUrl", batchApiUrl);
		dataMap.put("batchApiRequestJson", requestJsonStr);
		batchApiCallParamsMapper.insertBatchApiCallParams(dataMap);

		return dataMap.getString("batchApiId");
	}

	public DataMap getBatchApiCallParams(DataMap dataMap) throws Exception {

		String batchApiIdStr = dataMap.getString("batchApiId");
		return getBatchApiCallParams(batchApiIdStr);
	}

	public DataMap getBatchApiCallParams(String batchApiIdStr) throws Exception {

		DataMap param = batchApiCallParamsMapper.selectBatchApiCallParams(batchApiIdStr);
		if (ObjectUtils.isNotEmpty(param)) {
			return JsonUtils.toDataMap(param.getString("batchApiRequestJson"));
		}

		return new DataMap();
	}
}
