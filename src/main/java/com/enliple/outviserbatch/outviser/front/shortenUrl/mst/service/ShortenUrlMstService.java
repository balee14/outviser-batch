package com.enliple.outviserbatch.outviser.front.shortenUrl.mst.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.front.shortenUrl.mst.mapper.ShortenUrlMstMapper;

@Service
public class ShortenUrlMstService {

	@Autowired
	private ShortenUrlMstMapper shortenUrlMstMapper;

	/**
	 * @param param
	 * @throws Exception
	 */
	public List<DataMap> selectShortenUrlMstList(DataMap dataMap) throws Exception {
		List<DataMap> dataList = shortenUrlMstMapper.selectShortenUrlMstList(dataMap);
		if (dataList == null) {
			throw new CommonException("ShortenUrlMstService > selectShortenUrlMstList : null", dataMap);
		}
		return dataList;
	}

}