package com.enliple.outviserbatch.user.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.data.RestApiResultVO;
import com.enliple.outviserbatch.common.util.JsonUtils;
import com.enliple.outviserbatch.common.util.RestApiUtils;

@Service
public class UserService {

	@Value("${ibot.api.valid.check.token.url}")
	private String ibotApiValidCheckTokenUrl;

	/**
	 * ibot 토큰유효체크
	 * 
	 * @param token
	 * @param serviceType "MOBON" 고정값, 내부 처리했으므로 안 넣어도 됨.
	 * @return
	 * @throws Exception
	 */
	public DataMap validCheckToken(String token, String serviceType) throws Exception {

		DataMap result = null;

		if (StringUtils.isBlank(serviceType)) {
			serviceType = "MOBTUNE"; // 현재는 고정값
		}

		DataMap param = new DataMap();
		param.put("token", token);
		param.put("serviceType", serviceType);

		RestApiResultVO apiResult = RestApiUtils.callRestApi(ibotApiValidCheckTokenUrl, "POST", "JSON", param);
		if (apiResult.getHttpStatus() == HttpStatus.OK) {
			result = JsonUtils.toDataMap(apiResult.getBody());
		}

		return result;
	}
}