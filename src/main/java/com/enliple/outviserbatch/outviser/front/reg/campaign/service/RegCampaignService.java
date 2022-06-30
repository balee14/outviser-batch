package com.enliple.outviserbatch.outviser.front.reg.campaign.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.reg.campaign.mapper.RegCampaignMapper;

@Service
public class RegCampaignService {

	@Autowired
	private RegCampaignMapper regCampaignMapper;

	/**
	 * 캠페인 상태 확인
	 * 
	 * @param param
	 * @throws Exception
	 * @comment
	 */
	public DataMap checkCampaign(DataMap param, boolean boolTest) throws Exception {

		// 집행 처리전 체크 사항
		// 1. 캠페인 상태 집행전 여부
		if (!param.containsKey("campRowid") && !boolTest) {
			throw new CommonException("캠페인 번호는 필수 정보입니다.", param);
		}

		// 캠페인 상태 확인
		DataMap campaignData = selectExeCampaign(param);
		if (campaignData == null) {
			throw new CommonException("캠페인을 사용할 수 없거나 등록되지 않았습니다.", param);
		}

		if (campaignData.get("templateRowid") == null) {
			throw new CommonException("캠페인에 템플릿을 등록하지 않았습니다.", param);
		}

		if (campaignData.get("regSendTimeType") == null) {
			throw new CommonException("발송 시점이 설정되지 않았습니다.", param);
		}

		// 2. 알림톡 템플릿 사용 가능 여부
		param.put("templateRowid", campaignData.get("templateRowid"));
		param.put("crmCampNo", campaignData.get("crmCampNo"));
		param.put("tranDate", campaignData.get("tranDate"));
		param.put("regSendTimeType", campaignData.get("regSendTimeType"));
		param.put("regOverlapSendYn", campaignData.get("regOverlapSendYn"));
		param.put("regOverlapSendTerm", campaignData.get("regOverlapSendTerm"));
		param.put("regCampType", campaignData.get("regCampType"));
		return param;
	}

	/**
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<DataMap> selectRequiredDataList(DataMap param) throws Exception {
		List<DataMap> requiredDatas = regCampaignMapper.selectRequiredDataList(param);
		if (requiredDatas == null) {
			throw new CommonException("RegCampaignService > selectRequiredDataList : null", param);
		}
		return requiredDatas;
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public DataMap selectRequiredData(DataMap param) throws Exception {
		DataMap dataMap = regCampaignMapper.selectRequiredData(param);
		if (dataMap == null) {
			throw new CommonException("RegCampaignService > selectRequiredData : dataMap == null", param);
		}
		return dataMap;
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public void updateCampaignMstByTemplateRowid(DataMap param) throws Exception {
		int dataCnt = regCampaignMapper.updateCampaignMstByTemplateRowid(param);
		if (dataCnt < 1) {
			throw new CommonException("RegCampaignService > updateCampaignMstByTemplateRowid : dataCnt < 1", param);
		}
	}

	private DataMap selectExeCampaign(DataMap param) throws Exception {
		DataMap dataMap = regCampaignMapper.selectExeCampaign(param);
		if (dataMap == null) {
			throw new CommonException("RegCampaignService > selectExeCampaign : dataMap == null", param);
		}
		return dataMap;
	}
}