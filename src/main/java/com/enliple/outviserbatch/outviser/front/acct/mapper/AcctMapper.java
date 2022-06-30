package com.enliple.outviserbatch.outviser.front.acct.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.enliple.outviserbatch.common.data.DataMap;

@Mapper
public interface AcctMapper {
    
	DataMap selectAccount(DataMap dataMap);
	
    DataMap selectUserInfo(DataMap param);
    
    int updateAcctMstForAmountAndPointU(DataMap dataMap);
    
    List<DataMap> selectAllSenderInfoList();
    
    boolean isExistUser(DataMap dataMap);
}
