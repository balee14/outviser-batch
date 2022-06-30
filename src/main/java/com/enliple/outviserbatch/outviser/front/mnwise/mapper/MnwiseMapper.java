package com.enliple.outviserbatch.outviser.front.mnwise.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface MnwiseMapper {

	void insertEmail(DataMap dataMap);
}
