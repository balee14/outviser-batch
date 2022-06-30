package com.enliple.outviserbatch.common.interceptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.common.jwt.JwtTokenProvider;
import com.enliple.outviserbatch.common.service.errorLog.service.CommonErrorLogService;
import com.enliple.outviserbatch.common.util.JsonUtils;
import com.enliple.outviserbatch.outviser.front.acct.service.AcctService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtBearerAuthInterceptor extends WebContentInterceptor {

	@Autowired
	private AcctService acctService;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private CommonErrorLogService commonErrLogService;

	@Value("${jwt.status:on}")
	private String jwtStatus;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException {

		boolean isReturn = false;

		if (jwtStatus.equalsIgnoreCase("off") || request.getServletPath().equals("/index.html")) {
			return true;
		}

		// 토큰 추출
		String token = jwtTokenProvider.extract(request);
		if (StringUtils.isBlank(token)) {
			log.warn("헤더에 토큰 정보가 존재하지 않음");
			return isReturn;
		}

		try {
			// 토큰 유효성 체크
			if (!jwtTokenProvider.validateToken(token)) {
				log.warn(String.format("유효하지 않은 토큰 - token: %s", token));
				return isReturn;
			}

			/**
			 * 토큰에 해당하는 값을 추출
			 */
			String data = jwtTokenProvider.getData(token);

			DataMap dataMap = JsonUtils.toDataMap(data);
			String sessionLogin = dataMap.getString("sessionLogin");
			String sessionAgreeVisorYn = dataMap.getString("sessionAgreeVisorYn");

			if (StringUtils.isBlank(sessionLogin) || StringUtils.isBlank(sessionAgreeVisorYn)
					|| "N".equalsIgnoreCase(sessionLogin) || "N".equalsIgnoreCase(sessionAgreeVisorYn)) {
				throw new CommonException("Inaccessible user", dataMap);
			}

			// Account User 존재 여부
			isReturn = acctService.isExistUser(dataMap);
			if (!isReturn) {
				throw new CommonException("User does not exist", dataMap);
			}

		} catch (Exception e) {
			commonErrLogService.insertErrorLog(e);
			return isReturn;
		}

		return isReturn;
	}
}
