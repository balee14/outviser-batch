package com.enliple.outviserbatch.common.util;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.data.RestApiResultVO;

public class MtsUtils {

	public static DataMap callMtsApi(String apiUrl, DataMap param) throws Exception {
		DataMap result = new DataMap();
		String mtsCode = "";
		String mtsMsg = "";

		// authCode 추가
		param.put("authCode", PropertiesUtils.getValue("common.mts.authcode"));

		// MTS API Call : 프로필 관련 API는 x-www-form-urlencoded로 호출이 되어야 한다.
		RestApiResultVO apiResult = RestApiUtils.callRestApi(apiUrl, "POST", "FORM", param);

		// MTS Data Check
		mtsCode = JsonUtils.toMap(apiResult.getBody()).get("code").toString();
		if (JsonUtils.toMap(apiResult.getBody()).containsKey("message"))
			mtsMsg = JsonUtils.toMap(apiResult.getBody()).get("message").toString();

		// MTS의 결과 코드가 200이 아니면 MTS가 보내주는 메세지를 찍는다. 없을경우 API에서 제공해준 메세지를 찍는다.
		if (!mtsCode.equals("200")) {
			result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "M0" + mtsCode);
			if (!mtsMsg.isEmpty())
				result.put("rsMsg", mtsMsg);

			return result;
		}

		// MTS에서 보내주는 결과에 data 키는 있지만 실제 데이터가 없는 경우가 있으니 별도 에러 처리
		if (JsonUtils.toMap(apiResult.getBody()).containsKey("data")
				&& JsonUtils.toMap(apiResult.getBody()).get("data").toString().equals("null")) {
			result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "M0508");
		}
		// result Data Set
		else {
			result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "S0000");
			if (JsonUtils.toMap(apiResult.getBody()).containsKey("data"))
				result.put("rsData", JsonUtils.toMap(apiResult.getBody()).get("data"));
		}

		return result;
	}
}
