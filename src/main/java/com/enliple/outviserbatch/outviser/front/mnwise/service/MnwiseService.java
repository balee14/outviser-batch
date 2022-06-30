package com.enliple.outviserbatch.outviser.front.mnwise.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.mnwise.mapper.MnwiseMapper;
import com.enliple.outviserbatch.outviser.front.template.service.TemplateService;

@Service
public class MnwiseService {

	@Autowired
	private MnwiseMapper mnwiseMapper;

	@Autowired
	private TemplateService templateService;

	@Value("${file.upload.fullpath}")
	private String fileUploadFullPath;

	public void insertEmail(DataMap param) throws Exception {

		try {
			if (param.getString("tmpDtlAttachYn").equals("Y")) {
				List<DataMap> emailAttList = templateService.selectTemplateAttach(param.getInt("tmpDtlRowid"));

				String filePath = "";
				int forIdx = 1;
				for (DataMap emailAtt : emailAttList) {
					filePath = fileUploadFullPath;
					filePath += emailAtt.getString("tmpAttFilepath");
					filePath += "|";
					filePath += emailAtt.getString("tmpAttFilename");

					param.put("filePath" + String.valueOf(forIdx), filePath);
					forIdx++;
				}
				param.put("ecareNo", 2);
			} else {
				param.put("ecareNo", 1);
			}

			mnwiseMapper.insertEmail(param);

		} catch (Exception e) {
			throw new CommonException(String.format("E-MAIL 발송 요청 중 오류가 발생하였습니다. - insertEmail(%s)", e.getMessage()), param);
		}
	}
}
