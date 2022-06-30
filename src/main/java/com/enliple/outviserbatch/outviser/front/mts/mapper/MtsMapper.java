package com.enliple.outviserbatch.outviser.front.mts.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface MtsMapper {

	void insertAtalk(DataMap dataMap);

	void insertFtalk(DataMap dataMap);

	void insertFtalkFile(DataMap dataMap);

	void insertFtalkFileNew(DataMap dataMap);

	void insertSms(DataMap dataMap);

	void insertSmsByNotify(DataMap dataMap);

	void insertMms(DataMap dataMap);

	void insertMmsFile(DataMap dataMap);
}
