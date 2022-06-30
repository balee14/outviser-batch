package com.enliple.outviserbatch.schedule.refund.service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.schedule.refund.mapper.ScheduleRefundMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ScheduleRefundProcess {

    @Autowired
    private ScheduleRefundMapper scheduleRefundMapper;

    @Autowired
    private PlatformTransactionManager transactionManager;

    // 환불 및 추가 과금 진행 ( 원활한 트랜잭션 적용을 위해 service 에 같이 안넣고 따로 뺌 - js )
    @Transactional( propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void refund( DataMap param ) throws Exception{
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
        try {
            // 각각 환불 처리 시작
            this.addCharge(param);

            // 추가 과금을 위한 정보 조회
            DataMap extraCharge = scheduleRefundMapper.selectUsedChargeInfo(param);
            if (extraCharge != null && extraCharge.getDouble("REQ_AMOUNT") < 0) { // 추가 과금은 차감이라 음수
                //  차감 처리
                this.addUesdCharge(extraCharge);
            }
        }catch ( Exception e ){
            transactionManager.rollback(transactionStatus);
            throw e;
        }
        transactionManager.commit(transactionStatus);
    }

    // 환불 처리
    public void addCharge( DataMap param ) throws Exception{
        try {
            // 필수 파라미터 누락
            if (StringUtils.isBlank(param.getString("refundPoint"))) {
                throw new Exception("충전포인트 없음 - insertCharge");
            }
            if (StringUtils.isBlank(param.getString("chargeDt"))) {
                throw new Exception("충전일 없음 - insertCharge");
            }
            if (StringUtils.isBlank(param.getString("exeRunHstRowid"))) {
                throw new Exception("집행 정보 없음 - insertCharge");
            }

            // 해당 집행건에 대해 기존 환불 이력 있는지 확인
            DataMap existRefund = scheduleRefundMapper.selectChargeForExistRefund( param );
            if (existRefund != null) {
                throw new Exception(String.format("해당 집행건에 대하여 %s에 %s 환불 이력이 있습니다. - insertCharge", existRefund.getString("CH_CREATE_DATE"), existRefund.getString("CH_POINT")));
            }

            // 환불 처리
            int nRst = scheduleRefundMapper.insertChargeForFailRefund( param );
            if (nRst == 0) {
                throw new Exception("환불 이력 저장 중 오류 - insertCharge");
            }

            // 잔액 변경
            nRst = scheduleRefundMapper.updateAcctMstForFailRefund( param );
            if ( nRst == 0) {
                throw new Exception("회원 정보 잔액 변경 중 오류 - insertCharge");
            }

            // 최종 금액 검증
            param.put("reqAmount", 0);
            if( !this.checkAmount( param , false ) ){
                throw new Exception( "최종 금액 검증 불일치 - insertCharge" );
            }
        } catch (Exception e) {
            throw e;
        }
    }


    /**
     * 충전 금액 차감/차증
     *
     * @param param reqAmount, usedSendType, usedCampaignKey, usedItemRowid
     * @paramDetail reqAmount : 요청금액 차감은 음수로 차증은 양수로
     * @paramDetail usedSendType : SELECT * FROM OV_CODE_DTL WHERE MST_ROWID = 12
     * @paramDetail usedSendType : 01(알림톡), 02(친구톡), 03(SMS), 04(LMS), 05(MMS), 11(E-MAIL), 21(WEB PUSH), 22(APP PUSH)
     * @paramDetail	usedCampaignKey : 발송된 캠페인의 키값 (캠페인 단위로 발송) // 발송 작업하면서 추가해야해
     * @paramDetail usedItemRowid : 발송 타입의 Rowid (건별 발송) // 발송 작업하면서 추가해야해
     * @throws Exception
     */
    public boolean addUesdCharge( DataMap param ) throws Exception{
        // 임시로 사용금액 설정 : 음수면 차감, 양수면 차증 처리

        if(!param.containsKey("reqAmount"))
            throw new Exception("요청 금액은 필수 값 입니다. - insertUsedCharge");

        boolean boolUsed = true;
        if (param.getDouble("reqAmount") < 0) {
            // 음수면 사용여부 true
            boolUsed = true;
        }else if (param.getDouble("reqAmount") > 0) {
            // 양수면 사용여부 false
            boolUsed = false;
        }else {
            // 0이면 오류 처리
            throw new Exception("0원에 대한 사용 요청은 처리할 수 없습니다. - insertUsedCharge");
        }

        if (boolUsed) {

            try {
                // 충전내역의 금액과 계정정보의 금액 일치성 비교 : 다른경우 에러 리턴
                // 현재 충전금액 + 포인트가 사용금액 한도내인지 비교 : 모자른 경우 에러 리턴
                if( !this.checkAmount( param , true ) ){
                    throw new Exception( "최종 금액 검증 불일치 - insertCharge" );
                }

                // 차감 순서 확인 : 기본 충전금액을 모두 소진해야 포인트를 차감하며 차감 순서는 충전 종료일이 빠른 순, 충전 시작일이 빠른순 으로 한다.
                // 충전 내역이 없으면 에러 리턴
                List<DataMap> chargeDatas = scheduleRefundMapper.selectChargeData(param);
                if( chargeDatas.size() == 0 ){
                    throw new Exception( "사용가능한 충전 내역이 없습니다." );
                }

                List<DataMap> usedChargeDatas = new ArrayList<DataMap>();
                double reqAmount = param.getDouble("reqAmount");
                double sumUsedAmount = 0;
                double sumUsedPoint = 0;

                // 충전 내역이 있으면 순서대로 차감 처리
                for (DataMap chargeData : chargeDatas) {
                    // sendType이 여러개 인경우 반복문 추가하자
                    DataMap usedChargeData = new DataMap();
                    double usedAmount = 0;
                    double usedPoint = 0;

                    if (chargeData.getInt("usedOrder") == 1) {
                        if (chargeData.getDouble("chargeCurrentAmount") >= Math.abs(reqAmount)) {
                            usedAmount = Math.abs(reqAmount);
                            reqAmount = 0;
                        } else {
                            usedAmount = chargeData.getDouble("chargeCurrentAmount");
                            reqAmount += usedAmount;
                        }
                        sumUsedAmount += usedAmount;
                    } else if (chargeData.getInt("usedOrder") == 2) {
                        if (chargeData.getDouble("chargeCurrentAmount") >= Math.abs(reqAmount)) {
                            usedPoint = Math.abs(reqAmount);
                            reqAmount = 0;
                        } else {
                            usedPoint = chargeData.getDouble("chargeCurrentAmount");
                            reqAmount += usedPoint;
                        }
                        sumUsedPoint += usedPoint;
                    } else
                        throw new Exception("지원하지 않는 타입을 사용 했습니다.(1:충전금,2:적립포인트) - insertUsedCharge");

                    // 데이터맵 추가 or 수정 구분
                    boolean inputYn = true;

                    // 충전금액 사용이면 추가
                    if (usedAmount > 0)
                        inputYn = true;
                        // 포인트 사용이면 충전금액을 사용 내역이 있으면 수정, 없으면 추가
                    else if (usedPoint > 0) {
                        int updateIndex = 0;
                        for (int i = 0; i < usedChargeDatas.size(); i++) {
                            // 기존 데이터맵에 충전데이터의 rowid가 존재 하는지 확인, 존재하면 인덱스 반환, 수정을 위해 inputYn 구분 값 false로 변경,
                            // 루프 종료
                            if (usedChargeDatas.get(i).getLong("chargeRowid") == chargeData.getLong("chargeRowid")) {
                                inputYn = false;
                                updateIndex = i;
                                break;
                                // 기존 데이터맵에 충전데이터의 rowid가 존재하지 않으면 추가를 위해 inputYn 구분 값 true로 변경
                            } else
                                inputYn = true;
                        }

                        // false 이면 데이터맵 수정
                        if (!inputYn)
                            usedChargeDatas.get(updateIndex).put("usedPoint", usedPoint * -1);
                    }

                    // true 이면 데이터맵 추가
                    if (inputYn) {
                        usedChargeData.put("sessionUserRowId", param.getLong("sessionUserRowId"));
                        usedChargeData.put("campRowid", param.getLong("campRowid"));
                        usedChargeData.put("uuid", param.getString("uuid"));
                        usedChargeData.put("chargeRowid", chargeData.getLong("chargeRowid"));
                        usedChargeData.put("usedSendType", param.getString("usedSendType"));
                        usedChargeData.put("usedAmount", usedAmount * -1);
                        usedChargeData.put("usedPoint", usedPoint * -1);
                        usedChargeData.put("usedBeforeAmount", chargeData.getDouble("usedBeforeAmount"));
                        usedChargeData.put("usedBeforePoint", chargeData.getDouble("usedBeforePoint"));
                        usedChargeDatas.add(usedChargeData);
                    }

                    // 요청 금액이 0원 이하면 루프 종료 : 0원으로 떨어져야 한다.
                    if (Math.abs(reqAmount) < 0)
                        throw new Exception("사용 내역 처리중 오류가 발생하였습니다.(요청금액 이상 차감 시도)");
                    else if (Math.abs(reqAmount) == 0)
                        break;
                }

                if(sumUsedAmount + sumUsedPoint + param.getDouble("reqAmount") != 0)
                    throw new Exception("사용 내역 반영에 오류가 발생하였습니다. - insertUsedCharge");

                // 사용내역 차감 인서트
                int nRst = scheduleRefundMapper.insertUsedCharge(usedChargeDatas);
                if( nRst <= 0 ){
                    throw new Exception( "차감 데이터 처리중 오류가 발생하였습니다. 1" );
                }

                // 충전내역 차감 업데이트
                nRst = scheduleRefundMapper.updateChargeData(usedChargeDatas);
                if( nRst <= 0 ){
                    throw new Exception( "차감 데이터 처리중 오류가 발생하였습니다. 2" );
                }

                // 계정정보 차감 업데이트
                param.put("changeAmount", sumUsedAmount * -1);
                param.put("changePoint", sumUsedPoint * -1);
                nRst = scheduleRefundMapper.updateAcctMstForAmountAndPointU(param);
                if( nRst <= 0 ){
                    throw new Exception( "차감 데이터 처리중 오류가 발생하였습니다. 3" );
                }

                // 계정정보의 금액과 충전정보의 금액이 일치한지 확인( 요청 금액 체크는 불필요하니 0으로 설정하여 확인 )
                param.put("reqAmount", 0);

                // 최종 검증 완료 후 커밋 or 검증 후 오류 발견시 롤백
                if( !this.checkAmount( param , true ) ){
                    throw new Exception( "최종 금액 검증 불일치 - insertCharge" );
                }

            } catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                throw new Exception("사용내용 차감 처리중 오류가 발생했습니다. - insertUsedCharge(" + e.getMessage() + ")");
            }

        } else {
            // 사용내역 차증 인서트(20210226 넣지 않기로 결정)
            // 충전내역 차증 인서트(포인트로 넣기로 결정)
            param.put("chargeType", "CT_FAIL_REFUND");
            param.put("amount", null);
            param.put("currentAmount", null);
            param.put("point", param.getDouble("reqAmount"));
            param.put("currentPoint", param.getDouble("reqAmount"));
            param.put("pointRate", null);
            param.put("directRowid", null);
            param.put("inicisRowid", null);
            param.put("eventRef", null);
            param.put("startDate", null);
            param.put("endDate", null);
            //param.put("changeAmount", ); 환급은 포인트만 해준다.
            param.put("changePoint", param.getDouble("reqAmount"));
            param.put("paymentAmount", 0);

            try {
                // 충전내역 환급 인서트
                int nRst = scheduleRefundMapper.insertCharge(param);
                if( nRst <= 0 ){
                    throw new Exception( "차감 데이터 처리중 오류가 발생하였습니다. 5" );
                }

                // 계정정보 환급 업데이트
                nRst = scheduleRefundMapper.updateAcctMstForAmountAndPointU(param);
                if( nRst <= 0 ){
                    throw new Exception( "차감 데이터 처리중 오류가 발생하였습니다. 6" );
                }

                // 계정정보의 금액과 충전정보의 금액이 일치한지 확인( 요청 금액 체크는 불필요하니 0으로 설정하여 확인 )
                param.put("reqAmount", 0);

                // 최종 검증 완료 후 커밋 or 검증 후 오류 발견시 롤백
                if( !this.checkAmount( param , true ) ){
                    throw new Exception( "최종 금액 검증 불일치 - insertCharge" );
                }
            } catch(Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                throw new Exception("사용내용 차증 처리중 오류가 발생했습니다. - insertUsedCharge(" + e.getMessage() + ")");
            }
        }

        return true;
    }

    // 금액 검증
    public boolean checkAmount( DataMap param , boolean minusBool) throws Exception{
        DataMap data = scheduleRefundMapper.selectCheckAmount( param );
        if( data == null ){
            log.info( "데이터가 없습니다." , param );
            return false;
        }else{
            // 현재 금액 계정:충전 비교
            if ( data.getInt("checkCurrentAmount") != 0 ){
                log.info( "사용가능 금액이 계정정보와 다릅니다." , data, param );
                return false;
            }else if ( data.getInt("checkCurrentPoint") != 0 ){
                log.info( "사용가능 적립금이 계정정보와 다릅니다." , data, param );
                return false;
            }else if (  minusBool == true && data.getInt("checkUsedAmount") < 0) {
                log.info( "사용가능 금액이 부족합니다." , data, param );
                return false;
            }
        }

        return true;
    }

}
