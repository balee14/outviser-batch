package com.enliple.outviserbatch.outviser.front.mts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.mts.mapper.MtsMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MtsService {

	@Autowired
	private MtsMapper mtsMapper;

	public void insertAtalk(DataMap param) throws Exception {

		try {
			mtsMapper.insertAtalk(param);
		} catch (Exception e) {
			throw new CommonException(String.format("알림톡 발송 요청 중 오류가 발생하였습니다. - insertAtalk(%s)", e.getMessage()), param);
		}
	}

	public void insertFtalk(DataMap param) throws Exception {

		try {
			mtsMapper.insertFtalk(param);

			/**
			 * 2022-03-07 에이전트 교체로 우선순위 변경으로 분리
			 * insertFtalkFile(param)
			 * 임달형
			 */
//			if (param.getString("tmpDtlAttachYn").equals("Y")) {
//				mtsMapper.insertFtalkFile(param);
//			}

		} catch (Exception e) {
			throw new CommonException(String.format("친구톡 발송 요청 중 오류가 발생하였습니다. - insertFtalk(%s)", e.getMessage()), param);
		}
	}

	public void insertFtalkFile(DataMap param) throws Exception{

		try{
			if (param.getString("tmpDtlAttachYn").equals("Y")) {
				mtsMapper.insertFtalkFileNew(param);
			}
		} catch (Exception e) {
			throw new CommonException(String.format("친구톡 이미지 업로드 중 오류가 발생하였습니다. - insertFtalkImg(%s)", e.getMessage()), param);
		}

	}

	public void insertSms(DataMap param) throws Exception {

		try {
			mtsMapper.insertSms(param);
		} catch (Exception e) {
			throw new CommonException(String.format("SMS 발송 요청 중 오류가 발생하였습니다. - insertSms(%s)", e.getMessage()), param);
		}
	}

	public void insertSmsByNotify(DataMap param) {

		try {
			mtsMapper.insertSmsByNotify(param);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void insertLms(DataMap param) throws Exception {

		try {
			mtsMapper.insertMms(param);
		} catch (Exception e) {
			throw new CommonException(String.format("LMS 발송 요청 중 오류가 발생하였습니다. - insertMms(%s)", e.getMessage()), param);
		}
	}

	public void insertMms(DataMap param) throws Exception {

		try {
			mtsMapper.insertMms(param);

			if (param.getString("tmpDtlAttachYn").equals("Y")) {
				mtsMapper.insertMmsFile(param);
			}

		} catch (Exception e) {
			throw new CommonException(String.format("MMS 발송 요청 중 오류가 발생하였습니다. - insertMms(%s)", e.getMessage()), param);
		}
	}
}
