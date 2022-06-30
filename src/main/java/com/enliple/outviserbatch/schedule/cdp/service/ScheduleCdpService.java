package com.enliple.outviserbatch.schedule.cdp.service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.util.MobtuneCrmUtil;
import com.enliple.outviserbatch.outviser.front.crm.mapper.CrmMapper;
import com.enliple.outviserbatch.schedule.cdp.mapper.ScheduleCdpMapper;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("schedule_cdpService")
public class ScheduleCdpService {

    @Autowired
    private ScheduleCdpMapper cdpMapper;

    @Autowired
    private CrmMapper crmMapper;

    private static final Logger logger = LoggerFactory.getLogger(ScheduleCdpService.class);

    public void runCdpListBackup(){
        // 예약 리스트 목록 불러오기
        // 현재 시간 기준으로 미래 4일 가져오기
        List<DataMap> campignList = cdpMapper.selectReserveCampaignList();

        String resultBackup = "N";
        // 1. cdp 등록 여부 확인 - cdp 등록된 경우만 루프
        for (DataMap campign : campignList) {
            try {
                if (campign.getLong("regCrmNo") > 0) {
                    DataMap param = new DataMap();
                    param.put("sessionAdverId", campign.getString("adverId"));
                    param.put("crmCampNo", campign.getLong("regCrmNo"));
                    resultBackup = "S";
                    String extraAmountCsvPattern  = crmMapper.selectCdpExtraAmountCsvPattern(param.getString("sessionAdverId"));
                    //MobtuneCrmUtil.saveCrmAddrByFile(param,extraAmountCsvPattern);
                    MobtuneCrmUtil.saveCrmAddrByFile(param,extraAmountCsvPattern);
                }
            } catch (Exception e) {
                logger.debug("cdpListBackupParam : " + campign);
                logger.debug("cdpListBackupError : " + e);
                resultBackup = "F";
                //                continue;
            }

            DataMap dataMap = new DataMap();
            dataMap.put("adverId", campign.getString("adverId"));
            dataMap.put("cdpCampignId", campign.getLong("regCrmNo"));
            dataMap.put("campign", campign.toString());
            dataMap.put("resultBackup", resultBackup);
            cdpMapper.insertLog(dataMap);

        }
    }
}
