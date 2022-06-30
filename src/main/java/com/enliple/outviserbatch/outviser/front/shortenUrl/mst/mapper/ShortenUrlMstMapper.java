package com.enliple.outviserbatch.outviser.front.shortenUrl.mst.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface ShortenUrlMstMapper {

	List<DataMap> selectShortenUrlMstList(DataMap dataMap);
}
