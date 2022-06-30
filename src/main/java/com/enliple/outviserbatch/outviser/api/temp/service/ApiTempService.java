package com.enliple.outviserbatch.outviser.api.temp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.common.util.DateUtils;
import com.enliple.outviserbatch.outviser.api.temp.mapper.ApiTempMapper;

@Service
public class ApiTempService {

	@Autowired
	private ApiTempMapper apiTempMapper;

	/**
	 * 임시 테이블 생성
	 * 
	 * @param param
	 * @throws Exception
	 */
	public void createTempReqDataLogic(DataMap param) throws Exception {
		String tempTableName = "TEMP_"
				/*+ param.getString("campRowid")
				+ "_"*/
				+ DateUtils.getCurrentDate("yyyyMMddHHmmss")
				+ "_"
				+ param.getString("uuid");
		param.put("tempTableName", tempTableName);
		this.createTempReqData(param);
	}

	/**
	 * 발송 요청 데이터 발송 이력 확인
	 * 
	 * @param param
	 * @throws Exception
	 */
	public List<DataMap> tempReqDataLogic(DataMap param, List<DataMap> reqDatas) throws Exception {

		List<DataMap> dupList = new ArrayList<DataMap>();
		try {

			DataMap paramData = new DataMap();
			paramData.put("tempTableName", param.getString("tempTableName"));
			paramData.put("sessionUserRowId", param.getInt("sessionUserRowId"));
			paramData.put("campRowid", param.getInt("campRowid"));
			paramData.put("tmpDtlRowid", param.getInt("tmpDtlRowid"));
			paramData.put("regOverlapSendYn", param.getString("regOverlapSendYn"));
			paramData.put("regOverlapSendTerm", param.getInt("regOverlapSendTerm"));
			paramData.put("tmpDtlType", param.getString("tmpDtlType"));
			int loopFailIdx = 0;
			while (loopFailIdx < reqDatas.size()) {
				List<DataMap> loopReqDatas = reqDatas.subList(loopFailIdx,
						reqDatas.size() - loopFailIdx >= 1000 ? loopFailIdx + 1000 : reqDatas.size());
				paramData.put("datas", loopReqDatas);
				//
				this.insertTempReqData(paramData);
				loopFailIdx += 1000;
			}
			//
			dupList = this.selectTempReqData(paramData);

		} catch (Exception e) {
			throw new Exception("발송 요청 데이터 생성중 오류가 발생하였습니다. - tempReqDataLogic(" + e.getMessage() + ")");
		}
		return dupList;

	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public void createTempReqData(DataMap param) throws Exception {
		apiTempMapper.createTempReqData(param);
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public void insertTempReqData(DataMap param) throws Exception {
		apiTempMapper.insertTempReqData(param);
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public List<DataMap> selectTempReqData(DataMap param) throws Exception {
		List<DataMap> dataList = apiTempMapper.selectTempReqData(param);
		if (dataList == null) {
			throw new CommonException("SendService > selectTempReqData : dataList == null", param);
		}
		return dataList;
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public void dropTempReqData(DataMap param) throws Exception {
		apiTempMapper.dropTempReqData(param);
	}
}
