package com.enliple.outviserbatch.outviser.api.payment.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.outviser.api.payment.mapper.ApiUsedChargeMapper;
import com.enliple.outviserbatch.outviser.front.acct.service.AcctService;

@Service
public class ApiUsedChargeService {

	@Autowired
	private ApiUsedChargeMapper apiUsedChargeMapper;

	@Autowired
	private ApiChargeService apiChargeService;

	@Autowired
	private AcctService acctService;

	/**
	 * 충전 금액 차감/차증
	 * 
	 * @param param reqAmount, usedSendType, usedCampaignKey, usedItemRowid
	 * @paramDetail reqAmount : 요청금액 차감은 음수로 차증은 양수로
	 * @paramDetail usedSendType : SELECT * FROM OV_CODE_DTL WHERE MST_ROWID = 12
	 * @paramDetail usedSendType : 01(알림톡), 02(친구톡), 03(SMS), 04(LMS), 05(MMS),
	 *              11(E-MAIL), 21(WEB PUSH), 22(APP PUSH)
	 * @paramDetail usedCampaignKey : 발송된 캠페인의 키값 (캠페인 단위로 발송) // 발송 작업하면서 추가해야해
	 * @paramDetail usedItemRowid : 발송 타입의 Rowid (건별 발송) // 발송 작업하면서 추가해야해
	 * @throws Exception
	 */
	public synchronized void insertUsedCharge(DataMap param) throws Exception {

		boolean boolUsed = false;

		double reqAmount = param.getDouble("reqAmount");
		if (reqAmount == 0) {
			throw new CommonException("0원에 대한 사용 요청은 처리할 수 없습니다. - insertUsedCharge", param);
		} else if (reqAmount < 0) {
			boolUsed = true;
		}

		if (boolUsed) {
			// 충전내역의 금액과 계정정보의 금액 일치성 비교 : 다른경우 에러 리턴
			// 현재 충전금액 + 포인트가 사용금액 한도내인지 비교 : 모자른 경우 에러 리턴
			this.selectCheckAmount(param, true);

			// 차감 순서 확인 : 기본 충전금액을 모두 소진해야 포인트를 차감하며 차감 순서는 충전 종료일이 빠른 순, 충전 시작일이 빠른순 으로 한다.
			// 충전 내역이 없으면 에러 리턴
			List<DataMap> chargeDatas = this.selectChargeData(param);

			List<DataMap> usedChargeDatas = new ArrayList<DataMap>();
			double sumUsedAmount = 0;
			double sumUsedPoint = 0;

			// 충전 내역이 있으면 순서대로 차감 처리
			for (DataMap chargeData : chargeDatas) {
				DataMap usedChargeData = new DataMap();
				double usedAmount = 0;
				double usedPoint = 0;

				int usedOrder = chargeData.getInt("usedOrder");
				double chargeCurrentAmount = chargeData.getDouble("chargeCurrentAmount");
				if (usedOrder == 1) {
					if (chargeCurrentAmount >= Math.abs(reqAmount)) {
						usedAmount = Math.abs(reqAmount);
						reqAmount = 0;
					} else {
						usedAmount = chargeCurrentAmount;
						reqAmount += usedAmount;
					}

					sumUsedAmount += usedAmount;
				} else if (usedOrder == 2) {
					if (chargeCurrentAmount >= Math.abs(reqAmount)) {
						usedPoint = Math.abs(reqAmount);
						reqAmount = 0;
					} else {
						usedPoint = chargeCurrentAmount;
						reqAmount += usedPoint;
					}

					sumUsedPoint += usedPoint;
				} else {
					throw new CommonException("지원하지 않는 타입을 사용 했습니다.(1:충전금,2:적립포인트) - insertUsedCharge", param);
				}

				// 데이터맵 추가 or 수정 구분
				boolean inputYn = true;

				if (usedAmount <= 0 && usedPoint > 0) {
					// 포인트 사용이면 충전금액을 사용 내역이 있으면 수정, 없으면 추가
					for (int i = 0; i < usedChargeDatas.size(); i++) {
						// 기존 데이터맵에 충전데이터의 rowid가 존재 하는지 확인, 존재하면 인덱스 반환, 수정을 위해 inputYn 구분 값 false로 변경,
						if (usedChargeDatas.get(i).getLong("chargeRowid") == chargeData.getLong("chargeRowid")) {
							usedChargeDatas.get(i).put("usedPoint", usedPoint * -1);
							inputYn = false;
							break;
						}
					}
				}

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
				if (Math.abs(reqAmount) < 0) {
					throw new CommonException("사용 내역 처리중 오류가 발생하였습니다.(요청금액 이상 차감 시도) - insertUsedCharge", param);
				} else if (Math.abs(reqAmount) == 0) {
					break;
				}
			}

			if (sumUsedAmount + sumUsedPoint + param.getDouble("reqAmount") != 0) {
				param.put("sumUsedAmount", sumUsedAmount);
				param.put("sumUsedPoint", sumUsedPoint);
				throw new CommonException("사용 내역 반영에 오류가 발생하였습니다. - insertUsedCharge", param);
			}

			// 사용내역 차감 인서트
			this.insertUsedChargeData(usedChargeDatas);

			// 충전내역 차감 업데이트
			this.updateChargeData(usedChargeDatas);

			// 계정정보 차감 업데이트
			param.put("changeAmount", sumUsedAmount * -1);
			param.put("changePoint", sumUsedPoint * -1);

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
			param.put("changePoint", param.getDouble("reqAmount"));
			param.put("paymentAmount", 0);

			// 충전내역 환급 인서트
			apiChargeService.insertCharge(param);
		}

		// 계정정보 환급 업데이트
		acctService.updateAcctAmountData(param);

		// 계정정보의 금액과 충전정보의 금액이 일치한지 확인( 요청 금액 체크는 불필요하니 0으로 설정하여 확인 )
		param.put("reqAmount", 0);

		// 최종 검증 완료 후 커밋 or 검증 후 오류 발견시 롤백
		this.selectCheckAmount(param, true);
	}

	/**
	 * 충전, 계정 정보의 금액 체크
	 * 
	 * @param param
	 * @throws Exception
	 */
	public void selectCheckAmount(DataMap param, boolean isCheckedUseAmount) throws Exception {

		String throwMessage = "";

		DataMap data = apiUsedChargeMapper.selectCheckAmount(param);
		if (ObjectUtils.isEmpty(data)) {
			throwMessage = "결과가 없습니다.";
		} else if (data.getInt("checkCurrentAmount") != 0) { // 현재 금액 계정:충전 비교
			throwMessage = "사용가능 금액이 계정정보와 다릅니다.";
		} else if (data.getInt("checkCurrentPoint") != 0) { // 현재 포인트 계정:충전 비교
			throwMessage = "사용가능 적립금이 계정정보와 다릅니다.";
		} else if (isCheckedUseAmount && data.getInt("checkUsedAmount") < 0) { // 현재 금액 기준 사용 가능여부
			throwMessage = "사용가능 금액이 부족합니다.";
		}

		if (StringUtils.isNotBlank(throwMessage)) {
			throw new CommonException(String.format("%s - selectCheckAmount", throwMessage), param);
		}
	}

	/**
	 * 충전 내역 : 차감 순서로 데이터 조회
	 * 
	 * @param param
	 * @throws Exception
	 */
	private List<DataMap> selectChargeData(DataMap param) throws Exception {

		List<DataMap> datas = apiUsedChargeMapper.selectChargeData(param);
		if (datas.size() == 0) {
			throw new CommonException("사용가능한 충전 내역이 없습니다. - selectChargeData", param);
		}

		return datas;
	}

	/**
	 * 사용 내역 인서트
	 * 
	 * @param param
	 * @throws Exception
	 */
	private void insertUsedChargeData(List<DataMap> param) throws Exception {

		int nRst = apiUsedChargeMapper.insertUsedCharge(param);
		if (nRst <= 0) {
			throw new CommonException("차감 데이터 처리중 오류가 발생하였습니다. - insertUsedChargeData", param.toString());
		}
	}

	/**
	 * 충전 내역 업데이트
	 * 
	 * @param param
	 * @throws Exception
	 */
	private void updateChargeData(List<DataMap> param) throws Exception {

		int nRst = apiUsedChargeMapper.updateChargeData(param);
		if (nRst <= 0) {
			throw new CommonException("차감 데이터 처리중 오류가 발생하였습니다. - updateChargeData", param.toString());
		}
	}
}