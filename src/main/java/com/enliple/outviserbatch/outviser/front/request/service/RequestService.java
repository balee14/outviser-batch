package com.enliple.outviserbatch.outviser.front.request.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.request.mapper.RequestMapper;

@Service
public class RequestService {

	@Autowired
	private RequestMapper requestMapper;

	/**
	 * 발송 요청 데이터 추가
	 * 
	 * @param param
	 * @throws Exception
	 */
	public void insertRequestSendData(List<DataMap> sendDatas) throws Exception {
		int nRst = requestMapper.insertRequestSendData(sendDatas);
		if (nRst <= 0) {
			throw new CommonException("발송 요청 데이터 생성중 오류가 발생하였습니다. - insertRequestSendData", sendDatas);
		}
	}

	/**
	 * 발송 요청 실패 데이터 추가
	 * 
	 * @param param
	 * @throws Exception
	 */
	public void insertSendFailData(List<DataMap> sendDatas) throws Exception {
		int nRst = requestMapper.insertSendFailData(sendDatas);
		if (nRst <= 0) {
			throw new CommonException("발송 요청 실패 데이터 생성중 오류가 발생하였습니다. - insertSendFailData", sendDatas);
		}
	}
}