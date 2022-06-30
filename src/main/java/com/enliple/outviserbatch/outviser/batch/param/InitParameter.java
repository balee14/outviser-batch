package com.enliple.outviserbatch.outviser.batch.param;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.batch.service.BatchApiCallParamsService;

@Component
public class InitParameter {

	@Autowired
	private BatchApiCallParamsService batchApiCallParamsService;

	public final JobParameters getJobParam(DataMap dataMap) throws Exception {

		if (ObjectUtils.isEmpty(dataMap)) {
			throw new CommonException("DataMap is null");
		}

		String batchApiId = batchApiCallParamsService.insertBatchApiCallParams(dataMap);

		return new JobParametersBuilder()
				.addString("batchApiId", batchApiId)
				.toJobParameters();
	}
}
