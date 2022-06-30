package com.enliple.outviserbatch.outviser.front.template.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.template.mapper.TemplateMapper;

@Service("front_template_TemplateService")
public class TemplateService {

	@Autowired
	private TemplateMapper templateMapper;

	/**
	 * 템플릿 조회
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public DataMap selectExeTemplate(DataMap param) throws Exception {
		DataMap templateData = templateMapper.selectExeTemplate(param);
		if (templateData == null) {
			throw new CommonException("TemplateService > selectExeTemplate : null", param);
		}
		return templateData;
	}

	/**
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<DataMap> selectTemplateAttach(int tmpDtlRowid) throws Exception {
		List<DataMap> templateData = templateMapper.selectTemplateAttach(tmpDtlRowid);
		if (templateData == null) {
			throw new CommonException("TemplateService > selectTemplateAttach : null", tmpDtlRowid);
		}
		return templateData;
	}

	/**
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<DataMap> selectTemplateLink(int tmpDtlRowid) throws Exception {
		List<DataMap> templateData = templateMapper.selectTemplateLink(tmpDtlRowid);
		if (templateData == null) {
			throw new CommonException("TemplateService > selectTemplateLink : null", tmpDtlRowid);
		}
		return templateData;
	}

	/**
	 * 변수 목록 가져오기
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<DataMap> selectExeTempVarList(DataMap param) throws Exception {
		List<DataMap> templateData = templateMapper.selectExeTempVarList(param);
		if (templateData == null) {
			throw new CommonException("TemplateService > selectExeTempVarList : null", param);
		}
		return templateData;
	}

	/**
	 * 그룹 변수 목록 가져오기
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<DataMap> selectExeTempGrpVarList(DataMap param) throws Exception {
		List<DataMap> templateData = templateMapper.selectExeTempGrpVarList(param);
		if (templateData == null) {
			throw new CommonException("TemplateService > selectExeTempGrpVarList : null", param);
		}
		return templateData;
	}

	/**
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<DataMap> selectTmpListBySenderRowid(DataMap param) throws Exception {
		List<DataMap> templateData = templateMapper.selectTmpListBySenderRowid(param);
		if (templateData == null) {
			throw new CommonException("TemplateService > selectTmpListBySenderRowid : null", param);
		}
		return templateData;
	}

	/**
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public int updateTemplateInspect(DataMap param) throws Exception {
		int templateData = templateMapper.updateTemplateInspect(param);
		if (templateData < 1) {
			throw new CommonException("TemplateService > updateTemplateInspect : null", param);
		}
		return templateData;
	}

	/**
	 * 집행 처리할 템플릿
	 * 
	 * @param param
	 * @throws Exception
	 * @comment
	 */
	public DataMap checkTemplate(DataMap param) throws Exception {

		// 템플릿 조회
		DataMap templateData = this.selectExeTemplate(param);
		// 검수 상태가 APR인지 확인
		if (!templateData.getString("tmpStatus").equals("APR")) {
			throw new CommonException("템플릿이 검수되지 않았습니다.", param);
		}

		// 템플릿이 알림톡, 친구톡인 경우
		if (templateData.getString("tmpDtlType").equals("ATL") || templateData.getString("tmpDtlType").equals("FTL")) {
			// 발신 프로필 유무 확인
			if (templateData.get("atlkSenderKey") == null) {
				throw new CommonException("템플릿에 등록된 발신 프로필이 없습니다.", param);
			}

			// 템플릿에 대체 메세지 타입이 있는 경우
			if (!templateData.getString("tmpFailmsgSendtype").equals("N")) {
				// 회신번호 등록여부 확인
				if (templateData.get("tranCallBack") == null) {
					throw new CommonException("템플릿에 등록된 회신 번호가 없습니다.", param);
				}

				// 메세지 내용 등록여부 확인
				if (templateData.get("tmpDtlContent") == null) {
					throw new CommonException("템플릿에 등록된 대체 메세지 내용이 없습니다.", param);
				}
			}
		}

		// 템플릿이 이메일인 경우 발신 이메일 등록 여부
		if (templateData.getString("tmpDtlType").equals("EML") && templateData.get("emailSenderAddress") == null) {
			throw new CommonException("템플릿에 등록된 발신 이메일이 없습니다.", param);
		}

		return param;
	}
}