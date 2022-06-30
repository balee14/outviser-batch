package com.enliple.outviserbatch.common.interceptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.util.ResponseUtils;
import com.enliple.outviserbatch.outviser.front.acct.service.AcctService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ServletInterceptor extends WebContentInterceptor {

	@Autowired
	private AcctService acctService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException {

		log.warn("Request URI ==> : {}", request.getRequestURI());

		String uri = request.getRequestURI();

		// CRM 요청인 경우
		if (StringUtils.isNotBlank(uri) && uri.contains("crm.json")) {

			if ("127.0.0.1".equals(request.getRemoteAddr())) {
				return true;
			}

			DataMap dataMap = acctService.ibotTokenByResultAcctInfo(request);

			long acctRowid = dataMap.getLong("sessionUserRowId");
			if (acctRowid > 0) {
				request.setAttribute("sessionUserInfo", dataMap);
			} else {
				try {
					ResponseUtils.jsonMap(response, dataMap);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}

				return false;
			}
		}

		return true;
	}
}
