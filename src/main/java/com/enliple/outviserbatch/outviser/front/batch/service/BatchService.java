package com.enliple.outviserbatch.outviser.front.batch.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.batch.mapper.BatchMapper;

@Service
public class BatchService {

	@Autowired
	private BatchMapper batchMapper;

	/**
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<DataMap> selectBatch(DataMap param) throws Exception {
		List<DataMap> resultMap = batchMapper.selectBatch(param);
		if (resultMap == null) {
			throw new CommonException("BatchService > selectBatch : resultMap == null", param);
		}
		return resultMap;
	}

	/**
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public long runCheckDaily(DataMap param) throws Exception {
		long resultMap = batchMapper.runCheckDaily(param);
		if (resultMap < 1) {
			throw new CommonException("BatchService > runCheckDaily : resultMap == 0", param);
		}
		return resultMap;
	}

	/**
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public int insertLog(DataMap param) throws Exception {
		int resultMap = batchMapper.insertLog(param);
		if (resultMap < 1) {
			throw new CommonException("BatchService > insertLog : resultMap == 0", param);
		}
		return resultMap;
	}

	/**
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public int updateLog(DataMap param) throws Exception {
		int resultMap = batchMapper.updateLog(param);
		if (resultMap < 1) {
			throw new CommonException("BatchService > updateLog : resultMap == 0", param);
		}
		return resultMap;
	}
}