package com.enliple.outviserbatch.outviser.batch.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface BatchApiCallParamsMapper {

	@Select("SELECT BATCH_API_ID AS batchApiId, BATCH_API_REQUEST_JSON AS batchApiRequestJson FROM BATCH_API_CALL_PARAMS WHERE BATCH_API_ID = #{batchApiId}")
	DataMap selectBatchApiCallParams(String batchApiId);

	@Insert("INSERT INTO BATCH_API_CALL_PARAMS (BATCH_API_URL, BATCH_API_REQUEST_JSON) "
			+ "VALUES (#{batchApiUrl}, #{batchApiRequestJson})")
	@SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "batchApiId", resultType = Integer.class, before = false)
	int insertBatchApiCallParams(DataMap dataMap);
}
