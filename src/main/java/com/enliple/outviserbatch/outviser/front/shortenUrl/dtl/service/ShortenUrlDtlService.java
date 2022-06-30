package com.enliple.outviserbatch.outviser.front.shortenUrl.dtl.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.shortenUrl.dtl.mapper.ShortenUrlDtlMapper;

@Service
public class ShortenUrlDtlService {

	@Autowired
	private ShortenUrlDtlMapper shortenUrlDtlMapper;

	/**
	 * @param param
	 * @throws Exception
	 */
	public int insertShortenUrlDtlList(Object[] dataObjects) throws Exception {
		int dataCnt = shortenUrlDtlMapper.insertShortenUrlDtlList(dataObjects);
		if (dataCnt < 0) {
			throw new CommonException("ShortenUrlDtlService > insertShortenUrlDtlList : null", dataObjects);
		}
		return dataCnt;
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public int selectShortenUrlDtlCount(DataMap dataMap) throws Exception {
		int dataCnt = shortenUrlDtlMapper.selectShortenUrlDtlCount(dataMap);
		if (dataCnt < 0) {
			throw new CommonException("ShortenUrlDtlService > selectShortenUrlDtlCount : null", dataMap);
		}
		return dataCnt;
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public int insertBulkShortenUrlDtlList(List<DataMap> dataMap) throws Exception {
		int dataCnt = shortenUrlDtlMapper.insertBulkShortenUrlDtlList(dataMap);
		if (dataCnt < 0) {
			throw new CommonException("ShortenUrlDtlService > insertBulkShortenUrlDtlList : null", dataMap);
		}
		return dataCnt;
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public int insertShortenUrlDtl(DataMap dataMap) throws Exception {
		int dataCnt = shortenUrlDtlMapper.insertShortenUrlDtl(dataMap);
		if (dataCnt < 0) {
			throw new CommonException("ShortenUrlDtlService > insertShortenUrlDtl : null", dataMap);
		}
		return dataCnt;
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public DataMap selectShortenUrl(DataMap dataMap) throws Exception {
		DataMap param = shortenUrlDtlMapper.selectShortenUrl(dataMap);
		if (param == null) {
			throw new CommonException("ShortenUrlDtlService > selectShortenUrl : null", dataMap);
		}
		return param;
	}

	/**
	 * @param param
	 * @throws Exception
	 */
	public int updateShortenUrlForInflowCnt(DataMap dataMap) throws Exception {
		int dataCnt = shortenUrlDtlMapper.updateShortenUrlForInflowCnt(dataMap);
		if (dataCnt < 0) {
			throw new CommonException("ShortenUrlDtlService > updateShortenUrlForInflowCnt : null", dataMap);
		}
		return dataCnt;
	}
}