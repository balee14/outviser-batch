package com.enliple.outviserbatch.schedule.refund.mapper;

import com.enliple.outviserbatch.common.data.DataMap;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ScheduleRefundMapper {

    int testInsert ( String param );

    List<DataMap> selectRefundTargetList( DataMap param );

    DataMap selectChargeForExistRefund( DataMap param );

    int insertChargeForFailRefund( DataMap param );

    int updateAcctMstForFailRefund( DataMap param );

    DataMap selectCheckAmount ( DataMap param );

    DataMap selectUsedChargeInfo ( DataMap param );

    List<DataMap> selectChargeData ( DataMap param );

    int insertUsedCharge ( List<DataMap> param );

    int updateChargeData( List<DataMap> param);

    int updateAcctMstForAmountAndPointU ( DataMap param );

    int insertCharge ( DataMap param );
}
