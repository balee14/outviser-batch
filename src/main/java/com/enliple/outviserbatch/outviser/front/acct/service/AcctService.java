package com.enliple.outviserbatch.outviser.front.acct.service;

import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.acct.mapper.AcctMapper;
import com.enliple.outviserbatch.user.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AcctService {

	@Autowired
	private AcctMapper acctMapper;

	@Autowired
	private UserService ibotUserService;

	private final static String BEARER = "Bearer";

	/**
	 * @param request
	 * @return
	 */
	public DataMap ibotTokenByResultAcctInfo(HttpServletRequest request) {

		DataMap dataMap = new DataMap();
		dataMap.put("ibotResultCode", "USER_9999_CODE");
		dataMap.put("ibotResultMsg", "기타 오류 (ibot)");

		String serviceType = request.getHeader("service-type");
		String token = "";

		Enumeration<String> headers = request.getHeaders(HttpHeaders.AUTHORIZATION);
		while (headers.hasMoreElements()) {
			String value = headers.nextElement();

			if (value.startsWith(BEARER)) {
				token = value.substring(BEARER.length()).trim();
				break;
			}
		}

		if (StringUtils.isBlank(token)) {
			log.warn("IBOT token is not valid");
			return dataMap;
		}

		try {
			DataMap result = ibotUserService.validCheckToken(token, serviceType);	// ibot 토큰 유효성 체크 API 호출

			dataMap.clear();

			String rsCode = result.getString("resultCode");
			if ("USER_0000_CODE".equals(rsCode)) {
				DataMap itemsMap = result.getDataMap("items");
				String acctLoginId = itemsMap.getString("ibotId");

				DataMap dummy = new DataMap();
				dummy.put("acctLoginId", acctLoginId);

				dummy = acctMapper.selectAccount(dummy);
				long acctRowid = 0;
				if (ObjectUtils.isNotEmpty(dummy)) {
					acctRowid = dummy.getLong("rowid");
				}

				dataMap.put("sessionUserRowId", acctRowid);
				dataMap.put("sessionUserId", acctLoginId);
				dataMap.put("sessionAdverId", itemsMap.getString("adverId"));
			} else {
				dataMap.put("ibotResultCode", rsCode);
				dataMap.put("ibotResultMsg",  result.getString("resultMessage"));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return dataMap;
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public DataMap selectUserInfo(DataMap param) throws Exception {
		DataMap resultMap = acctMapper.selectUserInfo(param);
		if (resultMap == null) {
			throw new CommonException("AcctService > selectUserInfo : resultMap = null", param);
		}
		return resultMap;
	}

	/**
	 * 계정 정보 충전금액 업데이트
	 * 
	 * @param param
	 * @throws Exception
	 */
	public void updateAcctAmountData(DataMap param) throws Exception {
		int nRst = acctMapper.updateAcctMstForAmountAndPointU(param);
		if (nRst <= 0) {
			throw new CommonException("차감 데이터 처리중 오류가 발생하였습니다. - updateAcctAmountData", param);
		}
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public List<DataMap> selectAllSenderInfoList() throws Exception {
		List<DataMap> resultMap = acctMapper.selectAllSenderInfoList();
		if (resultMap == null) {
			throw new CommonException("AcctService > selectAllSenderInfoList : resultMap = null");
		}
		return resultMap;
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public boolean isExistUser(DataMap dataMap) throws Exception {
		return acctMapper.isExistUser(dataMap);
	}
}