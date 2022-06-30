package com.enliple.outviserbatch.schedule.cafe24.service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.data.RestApiResultVO;
import com.enliple.outviserbatch.common.util.CryptogramUtils;
import com.enliple.outviserbatch.common.util.JsonUtils;
import com.enliple.outviserbatch.common.util.RestApiUtils;
import com.enliple.outviserbatch.schedule.cafe24.mapper.ScheduleCafe24TokenReissuanceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("schedule_cafe24TokenReissuanceService")
public class ScheduleCafe24TokenReissuanceService {

	@Autowired
	private ScheduleCafe24TokenReissuanceMapper scheduleCafe24TokenReissuanceMapper;

	public void getAccessTokenUsingRefreshToken(List<DataMap> refrashTargetList) throws Exception {

		for(DataMap refrashTarget : refrashTargetList) {
			DataMap cafeParam = new DataMap();
			int acctRowId = refrashTarget.getInt("acctRowId");
			String mallId = refrashTarget.getString("mallId");
			String clientId = refrashTarget.getString("clientId");
			String clientSecretKey = refrashTarget.getString("clientSecretKey");
			String refreshToken = refrashTarget.getString("refreshToken");

			cafeParam.put("grant_type", "refresh_token");
			cafeParam.put("refresh_token", refreshToken);

			String clientBase64 = CryptogramUtils.base64Encode(clientId + ":" + clientSecretKey);
			String tokenUrl = "https://" + mallId + ".cafe24api.com/api/v2" + "/oauth/token";

			Map<String, String> paramHeader = new HashMap<>();
			paramHeader.put("Authorization", "Basic " + clientBase64);
			paramHeader.put("Content-Type", "application/x-www-form-urlencoded");

			RestApiResultVO apiResultApi = RestApiUtils.callRestApi(tokenUrl, "POST", "FORM", cafeParam, paramHeader);

			if (apiResultApi.getHttpStatus() == HttpStatus.OK) {

				DataMap bodyMap = JsonUtils.toDataMap(apiResultApi.getBody());
				String responseAccessToken = bodyMap.getString("access_token").trim();
				String responseRefreshToken = bodyMap.getString("refresh_token").trim();
				String responseRefreshTokenExpiresAt = bodyMap.getString("refresh_token_expires_at").trim();

				DataMap param = new DataMap();
				param.put("acctRowId", acctRowId);
				param.put("accessToken", responseAccessToken);
				param.put("refreshToken", responseRefreshToken);
				param.put("refreshTokenExpiresAt", responseRefreshTokenExpiresAt);

				scheduleCafe24TokenReissuanceMapper.updateTokenExpirationImminent(param);

			}

		}



	}

}

