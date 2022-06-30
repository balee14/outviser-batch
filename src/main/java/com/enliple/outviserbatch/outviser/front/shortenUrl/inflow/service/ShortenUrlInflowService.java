package com.enliple.outviserbatch.outviser.front.shortenUrl.inflow.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.shortenUrl.inflow.mapper.ShortenUrlInflowMapper;

@Service
public class ShortenUrlInflowService {

	@Autowired
	private ShortenUrlInflowMapper shortenUrlInflowMapper;

	/**
	 * @param param
	 * @throws Exception
	 */
	public int insertShortenUrlInflowLog(List<DataMap> listDataMap) throws Exception {
		int dataCnt = shortenUrlInflowMapper.insertShortenUrlInflowLog(listDataMap);
		if (dataCnt < 0) {
			throw new CommonException("ShortenUrlInflowService > insertShortenUrlInflowLog : null", listDataMap);
		}
		return dataCnt;
	}

}