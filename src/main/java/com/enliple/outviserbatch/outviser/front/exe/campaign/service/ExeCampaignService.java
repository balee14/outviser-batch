package com.enliple.outviserbatch.outviser.front.exe.campaign.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.exe.campaign.mapper.ExeCampaignMapper;

@Service
public class ExeCampaignService {

	@Autowired
	private ExeCampaignMapper exeCampaignMapper;

	/**
	 * 집행 데이터 추가
	 * 
	 * @param param
	 * @throws Exception
	 * @comment
	 */
	public void insertExecutional(DataMap param) throws Exception {

		// 집행 생성
		int nRst = exeCampaignMapper.insertExecutional(param);
		if (nRst <= 0) {
			throw new Exception("집행 데이터 저장중 오류가 발생하였습니다. - insertExecutional");
		}

		// 집행 이력 생성
		param.put("exeHisModType", "I");
		insertExecutionalHis(param);
	}

	/**
	 * 집행 데이터 수정
	 * 
	 * @param param
	 * @throws Exception
	 * @comment
	 */
	public void exeHisModType(DataMap param) throws Exception {

		// 집행 정보 수정
		updateExecutional(param);

		// 집행 이력 생성
		String exeStatus = param.containsKey("exeStatus") ? param.getString("exeStatus") : "STOP";
		if (exeStatus.toUpperCase().equals("DELETE"))
			param.put("exeHisModType", "D");
		else
			param.put("exeHisModType", "U");

		// 집행 히스토리
		insertExecutionalHis(param);
	}

	/**
	 * 집행 정보 수정
	 * 
	 * @param param
	 * @throws Exception
	 * @comment
	 */
	public void updateExecutional(DataMap param) throws Exception {
		int nRst = exeCampaignMapper.updateExecutional(param);
		if (nRst <= 0) {
			throw new CommonException("집행 데이터 이력 생성중 오류가 발생하였습니다. - updateExecutional", param);
		}

	}

	/**
	 * 집행 히스토리
	 * 
	 * @param param
	 * @throws Exception
	 * @comment
	 */
	public void insertExecutionalHis(DataMap param) throws Exception {
		int nRst = exeCampaignMapper.insertExecutionalHis(param);
		if (nRst <= 0) {
			throw new CommonException("집행 히스토리 생성중 오류가 발생하였습니다. - insertExecutionalHis", param);
		}
	}

	/**
	 * 집행 데이터 변경여부 확인
	 * 
	 * @param param
	 * @throws Exception
	 * @comment
	 */
	public boolean selectExeModCheck(DataMap param) throws Exception {

		return exeCampaignMapper.selectExeModCheck(param);
	}

	/**
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public DataMap selectExeInfoForAdmin(DataMap param) throws Exception {
		// 집행 정보 수정
		DataMap exeMap = exeCampaignMapper.selectExeInfoForAdmin(param);
		return exeMap;
	}

}