package com.enliple.outviserbatch.outviser.front.exceptFilter.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface ExceptFilterMapper {
	
	DataMap selectExceptInfo(DataMap dataMap);

	void dropFilterData(DataMap dataMap);
	
	
	void createAddressFilterData(DataMap dataMap);
    
	void insertAddressFilterList(DataMap dataMap);
	
    List<DataMap> selectAddressFilterList(DataMap dataMap);
    
    List<DataMap> selectAddressIntersectFilterList(DataMap dataMap);
	    
    
    void createCrmIntersectFilterData(DataMap dataMap);
    
    void insertCrmIntersectFilterList(DataMap dataMap);
    
    List<DataMap> selectCrmIntersectFilterList(DataMap dataMap);
    
    
    
//    void createCrmFilterData(DataMap dataMap);
//    
//	void dropCrmFilterData(DataMap dataMap);
//	
//	void insertCrmFilterList(DataMap dataMap);
//	
//    List<DataMap> selectCrmFilterList(DataMap dataMap);
    
    //테스트 쿼리
    void selectAddressFilterListTEST(DataMap dataMap);
}
