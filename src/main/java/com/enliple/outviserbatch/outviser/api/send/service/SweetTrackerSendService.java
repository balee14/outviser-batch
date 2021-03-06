package com.enliple.outviserbatch.outviser.api.send.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.data.RestApiResultVO;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.common.statics.StaticCode;
import com.enliple.outviserbatch.common.util.CommonUtils;
import com.enliple.outviserbatch.common.util.JsonUtils;
import com.enliple.outviserbatch.common.util.RestApiUtils;
import com.enliple.outviserbatch.outviser.api.send.mapper.SendMapper;
import com.enliple.outviserbatch.outviser.api.temp.service.ApiTempService;
import com.enliple.outviserbatch.outviser.front.addr.service.AddrService;
import com.enliple.outviserbatch.outviser.front.exceptFilter.service.ExceptFilterService;
import com.enliple.outviserbatch.outviser.front.exe.run.service.ExeRunService;
import com.enliple.outviserbatch.outviser.front.reg.campaign.mapper.RegCampaignMapper;
import com.enliple.outviserbatch.outviser.front.reg.campaign.service.RegCampaignService;
import com.enliple.outviserbatch.outviser.front.reg.template.mapper.RegTemplateMapper;
import com.enliple.outviserbatch.outviser.front.send.service.SendService;
import com.enliple.outviserbatch.outviser.front.template.mapper.TemplateMapper;

@Service
public class SweetTrackerSendService {

	private static final Logger logger = LoggerFactory.getLogger(SweetTrackerSendService.class);

	@Autowired
	private SendService sendService;

	@Autowired
	private AddrService addrService;

	@Autowired
	private RegCampaignService regCampaignService;

	@Autowired
	private ApiTempService apiTempService;

	@Autowired
	private ExeRunService exeRunService;

	@Autowired
	private SendMapper sendMapper;

	@Autowired
	private RegTemplateMapper regTemplateMapper;

	@Autowired
	private TemplateMapper templateMapper;

	@Autowired
	private RegCampaignMapper regCampaignMapper;
	
	@Autowired
	private ExceptFilterService exceptFilterService;

	@Value("${system.site.forward.addr}")
	private String forwardAddr;

	@Value("${mobtune.visor.api.url}")
	private String mobtuneApiUrl;

	@Value("${sweet.tracker.url}")
	private String sweetTrackerUrl;

	@Value("${sweet.tracker.tier}")
	private String sweetTrackerTier;

	@Value("${sweet.tracker.key}")
	private String sweetTrackerKey;

	public DataMap insertSweetTrackerTune(DataMap param) throws Exception {
		/*
		 * mobtune?????? ????????? ?????? ????????? ?????? ?????? ????????? ?????? ?????? ????????? ????????? ?????? ??? ??????????????? ?????? ?????? ?????? ??????
		 */

		DataMap result = new DataMap();
		result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "S0000");

		try {
			List<DataMap> deliveryApis = new ArrayList<>();
			String adverId = param.getString("adverId"); // ????????? ID sessionAdverId
			String acctRowid = param.getString("acctRowid"); // ?????? rowid
			String campaignNo = param.getString("campaignNo"); // ????????? ??????
			String campaignRowid = param.getString("campaignRowid"); // ????????? rowid

			List<DataMap> reqDatas = (List<DataMap>) param.get("reqDatas");
			for (DataMap reqData : reqDatas) {
				String userId = reqData.getString("userId"); // ???????????????20190603!
				String name = reqData.getString("name"); // ??????
				String phoneNo = reqData.getString("phoneNo"); // ????????????
				String email = reqData.getString("email"); // ?????????
				String gender = reqData.getString("gender"); // ??????
				String merryYn = reqData.getString("merryYn"); // ????????????
				String userJoinDttm = reqData.getString("userJoinDttm"); // ?????????
				String prdtName = reqData.getString("prdtName"); // ?????????
				String prdtCode = reqData.getString("prdtCode"); // ????????????
				String prdtPrice = reqData.getString("prdtPrice"); // ????????????
				String prdtTag = reqData.getString("prdtTag"); // ????????????
				String status = reqData.getString("status"); // ????????????
				String image = reqData.getString("image"); // ???????????????
				String categoryName = reqData.getString("categoryName"); // ?????? ???????????? ??????
				String categoryNo = reqData.getString("categoryNo"); // ?????? ???????????? ??????
				String adverName = reqData.getString("adverName"); // ????????????
				String tranPhone = reqData.getString("tranPhone"); // ????????????
				List<DataMap> deliveryDatas = (List<DataMap>) reqData.get("deliveryDatas");

				for (DataMap deliveryData : deliveryDatas) {
					String orderNo = deliveryData.getString("orderId"); // ?????? ID
					String orderName = deliveryData.getString("orderName"); // ?????????
					String orderQuantity = deliveryData.getString("orderQuantity"); // ????????????
					String senderName = deliveryData.getString("senderName"); // ????????????
					String recipientName = deliveryData.getString("recipientName"); // ????????????
					String recipientAddress = deliveryData.getString("recipientAddress"); // ??????????????????
					List<DataMap> invoiceDatas = (List<DataMap>) deliveryData.get("invoiceDatas");

					for (DataMap invoiceData : invoiceDatas) {
						String invoiceNo = invoiceData.getString("no"); // ???????????????
						String courierCode = invoiceData.getString("deliveryCorpCode"); // ???????????????

						// mst insert??? ?????? dataMap
						DataMap sweetTrackerMst = new DataMap();
						sweetTrackerMst.put("INVOICE_NO", invoiceNo);
						sweetTrackerMst.put("ORDER_NO", orderNo);
						sweetTrackerMst.put("ADVER_ID", adverId);
						sweetTrackerMst.put("COURIER_CODE", courierCode);
						sweetTrackerMst.put("LEVEL", 0);
						sweetTrackerMst.put("TRAN_PHONE", tranPhone);
						sweetTrackerMst.put("ACCT_ROWID", "".equals(acctRowid) ? null : acctRowid);
						sweetTrackerMst.put("CRM_NO", "".equals(campaignNo) ? null : campaignNo);
						sweetTrackerMst.put("CAMP_ROWID", "".equals(campaignRowid) ? null : campaignRowid);

						logger.debug(sweetTrackerMst.toString());

						// ??????????????? ??????
						DataMap mstData = sendMapper.selectSweetTrackerMstByInvoiceNo(sweetTrackerMst);
						if (mstData == null || mstData.isEmpty()) {
							sendMapper.insertSweetTrackerMst(sweetTrackerMst);

							String rowid = sweetTrackerMst.getString("ROWID");

							if ("".equals(campaignRowid)) {
								Map<String, String> valueMap = new HashMap<>();
								valueMap.put("userId", userId);
								valueMap.put("name", name);
								valueMap.put("phoneNo", phoneNo);
								valueMap.put("email", email);
								valueMap.put("gender", gender);
								valueMap.put("merryYn", merryYn);
								valueMap.put("userJoinDttm", userJoinDttm);
								valueMap.put("prdtName", prdtName);
								valueMap.put("prdtCode", prdtCode);
								valueMap.put("prdtPrice", prdtPrice);
								valueMap.put("prdtTag", prdtTag);
								valueMap.put("status", status);
								valueMap.put("image", image);
								valueMap.put("categoryName", categoryName);
								valueMap.put("categoryNo", categoryNo);
								valueMap.put("adverName", adverName);
								valueMap.put("tranPhone", tranPhone);
								valueMap.put("orderNo", orderNo);
								valueMap.put("orderName", orderName);
								valueMap.put("orderQuantity", orderQuantity);
								valueMap.put("senderName", senderName);
								valueMap.put("recipientName", recipientName);
								valueMap.put("recipientAddress", recipientAddress);
								valueMap.put("invoiceNo", invoiceNo);
								valueMap.put("courierCode", courierCode);
								valueMap.put("courierName",
										StaticCode.getCodeName("DELIVERY_COMPANY_CODE", courierCode));

								for (String key : valueMap.keySet()) {
									if (!"".equals(valueMap.get(key))) {
										DataMap value = new DataMap();
										value.put("DELIVERY_MST_ROWID", rowid);
										value.put("NAME", key);
										value.put("VALUE", valueMap.get(key));
										sendMapper.insertSweetTrackerVarGrp(value);
									}
								}
							}

							// api ????????? ?????? dataMap
							DataMap deliveryApi = new DataMap();
							deliveryApi.put("num", invoiceNo);
							deliveryApi.put("code", courierCode);
							deliveryApi.put("fid", rowid);
							deliveryApis.add(deliveryApi);
						}
					}
				}
			}

			// ????????? ?????? or ??????
			if (deliveryApis.size() > 0) {
				DataMap sweetTrackerApi = new DataMap();
				sweetTrackerApi.put("callback_url", forwardAddr + "/api/send/sweetTracker/getData");
				sweetTrackerApi.put("callback_type", "json");
				sweetTrackerApi.put("tier", sweetTrackerTier);
				sweetTrackerApi.put("key", sweetTrackerKey);
				sweetTrackerApi.put("list", deliveryApis);

				logger.debug("body : " + JsonUtils.toString(sweetTrackerApi));

				RestApiResultVO apiResult = RestApiUtils.callRestApi(sweetTrackerUrl + "add_invoice_list", "POST", "JSON",
						JsonUtils.toString(sweetTrackerApi));

				if (apiResult.getHttpStatus() == HttpStatus.OK) {
					logger.debug("result body : " + apiResult.getBody());
					DataMap resultBody = JsonUtils.toDataMap(apiResult.getBody());

					String list = resultBody.getString("list");

					if (!"".equals(list)) {

						List<DataMap> apiBodys = JsonUtils.toArrayDataMap(list);

						int succedCount = 0;
						int failedCount = 0;
						DataMap rsData = new DataMap();
						DataMap sweetTrackerResult = new DataMap();
						DataMap sendResult = new DataMap();
						List<DataMap> failedResult = new ArrayList<>();

						for (DataMap apiBody : apiBodys) {
							DataMap sweetTrackerMst = sendMapper.selectSweetTrackerMstByRowId(apiBody);
							apiBody.put("MST_ROWID", apiBody.getString("fid"));
							if ("true".equals(apiBody.getString("success"))) {
								apiBody.put("level", 0);
								apiBody.put("ERR_YN", "N");
								succedCount++;

								// ???????????? ?????????????????? ????????? ??????
								// crm ??????
								if ("".equals(campaignRowid)) {
									DataMap map = new DataMap();
									map.put("ACCT_ROWID", acctRowid);
									map.put("CRM_NO", campaignNo);
									List<DataMap> campaignList = sendMapper.selectCampaignMstByCrmNo(map);
									for (DataMap campaign : campaignList) {
										campaign.put("TMP_USE_TYPE_CODE", "DS");
										DataMap tmpGrp = regTemplateMapper.selectTmpGrpByTmpUseTypeCode(campaign);
										// ????????? ??????
										if (tmpGrp != null) {
											map.put("tmpRowid", tmpGrp.getString("TMP_ROWID"));
											DataMap tmpData = templateMapper.selectTemplate(map);

											DataMap sendParam = new DataMap();
											sendParam.put("tranPhone", sweetTrackerMst.getString("TRAN_PHONE"));
											sendParam.put("campRowid", campaign.getString("ROWID"));
											sendParam.put("campaignNo", campaign.getString("REG_CRM_NO"));
											sendParam.put("sessionUserRowId", campaign.getString("ACCT_ROWID"));
											sendParam.put("regCampType", campaign.getString("REG_CAMP_TYPE"));
											sendParam.put("sessionAdverId", adverId);
											sendParam.put("tmpDtlRowid", tmpData.getString("tmpDtlRowid"));
											sendParam.put("tmpUseTypeCode", tmpGrp.getString("TMP_USE_TYPE_CODE"));
											sendParam.put("crmYn", "Y");

											// crm ??????
											map.put("DELIVERY_MST_ROWID", sweetTrackerMst.getString("ROWID"));
											List<DataMap> varList = sendMapper.selectSweetTrackerVarList(map);

											for (DataMap var : varList) {
												sendParam.put(var.getString("NAME"), var.getString("VALUE"));
												if ("?????????".equals(var.getString("NAME"))) {
													sendParam.put("email", var.getString("VALUE"));
												}
											}
											sendParam.put("????????????", sweetTrackerMst.getString("CREATE_DATE_TEXT"));

//											sendParam.put("????????????", sweetTrackerMst.getString("ORDER_NO"));
//											sendParam.put("????????????(?????????)",StaticCode.getCodeName("DELIVERY_COMPANY_CODE", sweetTrackerMst.getString("COURIER_CODE")));
//											sendParam.put("???????????????",sweetTrackerMst.getString("INVOICE_NO"));
//											sendParam.put("???????????????","");
//											sendParam.put("??????",name);

											sendResult = this.requestSendTracker(sendParam);
										}
									}
									// ?????? ?????????
								} else {
									DataMap map = new DataMap();
									map.put("ROWID", campaignRowid);
									DataMap campaign = regCampaignMapper.selectCampaignMst(map);
									map.put("TMP_USE_TYPE_CODE", "DS");
									DataMap tmpGrp = regTemplateMapper.selectTmpGrpByTmpUseTypeCode(map);
									// ????????? ??????
									if (tmpGrp != null) {
										map.put("tmpRowid", tmpGrp.getString("TMP_ROWID"));
										DataMap tmpData = templateMapper.selectTemplate(map);

										DataMap sendParam = new DataMap();
										sendParam.put("tranPhone", sweetTrackerMst.getString("TRAN_PHONE"));
										sendParam.put("campRowid", campaign.getString("ROWID"));
										sendParam.put("sessionUserRowId", campaign.getString("ACCT_ROWID"));
										sendParam.put("regCampType", campaign.getString("REG_CAMP_TYPE"));
										sendParam.put("tmpDtlRowid", tmpData.getString("tmpDtlRowid"));
										sendParam.put("tmpUseTypeCode", tmpGrp.getString("TMP_USE_TYPE_CODE"));
										sendParam.put("crmYn", "N");

										// ?????? ????????? ????????????
										map.put("sessionUserRowId", campaign.getString("ACCT_ROWID"));
										map.put("campRowid", campaign.getString("ROWID"));
										List<DataMap> addressVals = addrService.selectExcelAddress(map);
										// List<DataMap> addressVals =
										// outviserDao.selectList("enliple.ibot.outviser.api.send.selectExcelAddress",
										// map);

										for (DataMap addressVal : addressVals) {
											if (sweetTrackerMst.getString("INVOICE_NO")
													.equals(addressVal.getString("var4"))) {
												for (int i = 1; i <= 20; i++) {
													if (!"".equals(addressVal.getString("var" + i))) {
														if (i == 3) {
															sendParam.put("??????" + i,
																	StaticCode.getCodeName("DELIVERY_COMPANY_CODE",
																			addressVal.getString("var" + i)));
														} else {
															sendParam.put("??????" + i, addressVal.getString("var" + i));
														}
													}
												}

												sendParam.put("??????", addressVal.getString("name"));
												sendParam.put("????????????", addressVal.getString("phoneNo"));
												sendParam.put("?????????", addressVal.getString("email"));
												sendParam.put("email", addressVal.getString("email"));

											}
										}

										sendParam.put("????????????", sweetTrackerMst.getString("CREATE_DATE_TEXT"));
										sendParam.put("???????????????", "");

										sendResult = this.requestSendTracker(sendParam);
									}
								}

							} else {
								apiBody.put("level", -1);
								apiBody.put("ERR_YN", "Y");
								apiBody.put("ERR_MSG",
										"[" + apiBody.getString("e_code") + "]" + apiBody.getString("e_message"));

								DataMap fail = new DataMap();
								fail.put("adverId", adverId);
								fail.put("campaignNo", campaignNo);
								fail.put("tranPhone", sweetTrackerMst.getString("TRAN_PHONE"));
								fail.put("orderId", sweetTrackerMst.getString("ORDER_NO"));
								fail.put("deliveryCorpCode", sweetTrackerMst.getString("COURIER_CODE"));
								fail.put("no", apiBody.getString("num"));
								fail.put("e_message", apiBody.getString("e_message"));
								fail.put("e_code", apiBody.getString("e_code"));
								failedResult.add(fail);
								failedCount++;
							}

							sendMapper.insertSweetTrackerLog(apiBody);
						}

						if (failedCount > 0) {
							// ?????? ?????? ??????
							result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "E0000");
							sweetTrackerResult.put("failedResult", failedResult);
						}

						sweetTrackerResult.put("succedCount", succedCount);
						sweetTrackerResult.put("failedCount", failedCount);

						rsData.put("sweetTrackerResult", sweetTrackerResult);

						if (!sendResult.isEmpty()) {
							rsData.put("sendResult", sendResult.get("rsData"));

							if (!"S0000".equals(sendResult.getString("rsCode"))) {
								result.setHttpStatusAndResult(DataMap.HttpStatus.OK, sendResult.getString("rsCode"),
										sendResult.getString("rsMsg"));
							}
						}

						result.put("rsData", rsData);

					} else {
						// API ??????
						String eCode = resultBody.getString("e_code");
						String eMsg = resultBody.getString("e_message");
						if (!"".equals(eCode) && !"".equals(eMsg)) {
							result.setHttpStatusAndResult(DataMap.HttpStatus.OK, eCode, eMsg);
						} else {
							result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "E0000");
						}
					}
				} else {
					// API ?????? ??????
					result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "E0000");
				}
			} else {
				result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "E0000", "???????????? ????????? ????????? ??????????????? ?????????.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			result.setHttpStatusAndResult(DataMap.HttpStatus.OK, e);
		}

		logger.debug("result : " + result.toString());

		return result;
	}

	public DataMap insertSweetTrackerSand(DataMap param) throws Exception {

		DataMap result = new DataMap();

		int level = param.getInt("level");

		int levelCnt = sendMapper.selectSweetTrackerLogByLevelCnt(param);

		DataMap sweetTracker = sendMapper.selectSweetTrackerMstByRowId(param);

		// ????????? ??????
		if (level != -99 && levelCnt == 1) {

			// ????????? ??????????????? ????????? ??????
			String campRowid = sweetTracker.getString("CAMP_ROWID");
			// crm ??????
			if ("".equals(campRowid)) {
				List<DataMap> campaignList = sendMapper.selectCampaignMstByCrmNo(sweetTracker);
				for (DataMap campaign : campaignList) {
					if (level == 5) {
						campaign.put("TMP_USE_TYPE_CODE", "DI");
					} else if (level == 6) {
						campaign.put("TMP_USE_TYPE_CODE", "DC");
					}
					DataMap tmpGrp = regTemplateMapper.selectTmpGrpByTmpUseTypeCode(campaign);
					// ????????? ??????
					if (tmpGrp != null) {
						DataMap map = new DataMap();
						map.put("tmpRowid", tmpGrp.getString("TMP_ROWID"));
						DataMap tmpData = templateMapper.selectTemplate(map);

						DataMap sendParam = new DataMap();
						sendParam.put("tranPhone", sweetTracker.getString("TRAN_PHONE"));
						sendParam.put("campRowid", campaign.getString("ROWID"));
						sendParam.put("campaignNo", campaign.getString("REG_CRM_NO"));
						sendParam.put("sessionUserRowId", campaign.getString("ACCT_ROWID"));
						sendParam.put("regCampType", campaign.getString("REG_CAMP_TYPE"));
						sendParam.put("sessionAdverId", sweetTracker.getString("ADVER_ID"));
						sendParam.put("tmpDtlRowid", tmpData.getString("tmpDtlRowid"));
						sendParam.put("tmpUseTypeCode", tmpGrp.getString("TMP_USE_TYPE_CODE"));
						sendParam.put("crmYn", "Y");

						// crm ??????
						map.put("DELIVERY_MST_ROWID", sweetTracker.getString("ROWID"));
						List<DataMap> varList = sendMapper.selectSweetTrackerVarList(map);

						for (DataMap var : varList) {
							sendParam.put(var.getString("NAME"), var.getString("VALUE"));
							if ("?????????".equals(var.getString("NAME"))) {
								sendParam.put("email", var.getString("VALUE"));
							}
						}
						sendParam.put("????????????", sweetTracker.getString("CREATE_DATE_TEXT"));

//						sendParam.put("????????????",sweetTracker.getString("ORDER_NO"));
//						sendParam.put("???????????????",sweetTracker.getString("INVOICE_NO"));

						if (!"".equals(param.getString("recvAddr"))) {
							sendParam.put("???????????????", param.getString("recvAddr"));
						}

						if (!"".equals(param.getString("telnoOffice"))) {
							sendParam.put("????????????(?????????)",
									sendParam.getString("????????????(?????????)") + "(" + param.getString("telnoOffice") + ")");
						}

						String man = param.getString("man");
						String tel_man = param.getString("telnoMan");
						if (!"".equals(man) && !"".equals(tel_man)) {
							man += "(" + tel_man + ")";
						}
						sendParam.put("???????????????(?????????)", man);

						result = this.requestSendTracker(sendParam);
					}

					if (level == 6) {
						campaign.put("TMP_USE_TYPE_CODE", "DCB");
						tmpGrp = regTemplateMapper.selectTmpGrpByTmpUseTypeCode(campaign);
						// ????????? ??????
						if (tmpGrp != null) {
							DataMap map = new DataMap();
							map.put("tmpRowid", tmpGrp.getString("TMP_ROWID"));
							DataMap tmpData = templateMapper.selectTemplate(map);

							DataMap sendParam = new DataMap();
							sendParam.put("tranPhone", sweetTracker.getString("TRAN_PHONE"));
							sendParam.put("campRowid", campaign.getString("ROWID"));
							sendParam.put("campaignNo", campaign.getString("REG_CRM_NO"));
							sendParam.put("sessionUserRowId", campaign.getString("ACCT_ROWID"));
							sendParam.put("regCampType", campaign.getString("REG_CAMP_TYPE"));
							sendParam.put("sessionAdverId", sweetTracker.getString("ADVER_ID"));
							sendParam.put("tmpDtlRowid", tmpData.getString("tmpDtlRowid"));
							sendParam.put("tmpUseTypeCode", tmpGrp.getString("TMP_USE_TYPE_CODE"));
							sendParam.put("tranDate", tmpGrp.getString("TRAN_DATE"));
							sendParam.put("crmYn", "Y");

							// crm ??????
							map.put("DELIVERY_MST_ROWID", sweetTracker.getString("ROWID"));
							List<DataMap> varList = sendMapper.selectSweetTrackerVarList(map);

							for (DataMap var : varList) {
								sendParam.put(var.getString("NAME"), var.getString("VALUE"));
								if ("?????????".equals(var.getString("NAME"))) {
									sendParam.put("email", var.getString("VALUE"));
								}
							}

//							sendParam.put("????????????",sweetTracker.getString("ORDER_NO"));
//							sendParam.put("???????????????",sweetTracker.getString("INVOICE_NO"));

							if (!"".equals(param.getString("recvAddr"))) {
								sendParam.put("???????????????", param.getString("recvAddr"));
							}

							if (!"".equals(param.getString("telnoOffice"))) {
								sendParam.put("????????????(?????????)",
										sendParam.getString("????????????(?????????)") + "(" + param.getString("telnoOffice") + ")");
							}

							String man = param.getString("man");
							String tel_man = param.getString("telnoMan");
							if (!"".equals(man) && !"".equals(tel_man)) {
								man += "(" + tel_man + ")";
							}
							sendParam.put("???????????????(?????????)", man);

							result = this.requestSendTracker(sendParam);
						}
					}
				}
				// ???????????????
			} else {
				DataMap map = new DataMap();
				map.put("ROWID", campRowid);
				DataMap campaign = regCampaignMapper.selectCampaignMst(map);
				if (level == 5) {
					map.put("TMP_USE_TYPE_CODE", "DI");
				} else if (level == 6) {
					map.put("TMP_USE_TYPE_CODE", "DC");
				}
				DataMap tmpGrp = regTemplateMapper.selectTmpGrpByTmpUseTypeCode(campaign);
				// ????????? ??????
				if (tmpGrp != null) {
					map.put("tmpRowid", tmpGrp.getString("TMP_ROWID"));
					DataMap tmpData = templateMapper.selectTemplate(map);

					DataMap sendParam = new DataMap();
					sendParam.put("tranPhone", sweetTracker.getString("TRAN_PHONE"));
					sendParam.put("campRowid", campaign.getString("ROWID"));
					sendParam.put("sessionUserRowId", campaign.getString("ACCT_ROWID"));
					sendParam.put("regCampType", campaign.getString("REG_CAMP_TYPE"));
					sendParam.put("tmpDtlRowid", tmpData.getString("tmpDtlRowid"));
					sendParam.put("tmpUseTypeCode", tmpGrp.getString("TMP_USE_TYPE_CODE"));
					sendParam.put("crmYn", "N");

					// ?????? ????????? ????????????
					map.put("sessionUserRowId", campaign.getString("ACCT_ROWID"));
					map.put("campRowid", campaign.getString("ROWID"));
					List<DataMap> addressVals = addrService.selectExcelAddress(map);
					// List<DataMap> addressVals =
					// outviserDao.selectList("enliple.ibot.outviser.api.send.selectExcelAddress",
					// map);

					for (DataMap addressVal : addressVals) {
						if (sweetTracker.getString("INVOICE_NO").equals(addressVal.getString("var4"))) {
							for (int i = 1; i <= 20; i++) {
								if (!"".equals(addressVal.getString("var" + i))) {
									if (i == 3) {
										sendParam.put("??????" + i, StaticCode.getCodeName("DELIVERY_COMPANY_CODE",
												addressVal.getString("var" + i)));
									} else {
										sendParam.put("??????" + i, addressVal.getString("var" + i));
									}
								}
							}

							sendParam.put("??????", addressVal.getString("name"));
							sendParam.put("????????????", addressVal.getString("phoneNo"));
							sendParam.put("?????????", addressVal.getString("email"));
							sendParam.put("email", addressVal.getString("email"));

						}
					}

					sendParam.put("????????????", sweetTracker.getString("CREATE_DATE_TEXT"));

					String man = param.getString("man");
					String tel_man = param.getString("telnoMan");
					if (!"".equals(man) && !"".equals(tel_man)) {
						man += "(" + tel_man + ")";
					}
					sendParam.put("???????????????(?????????)", man);
					sendParam.put("???????????????", param.getString("recvAddr"));

					result = this.requestSendTracker(sendParam);
				}

				if (level == 6) {
					map.put("TMP_USE_TYPE_CODE", "DCB");
					tmpGrp = regTemplateMapper.selectTmpGrpByTmpUseTypeCode(map);
					// ????????? ??????
					if (tmpGrp != null) {
						map.put("tmpRowid", tmpGrp.getString("TMP_ROWID"));
						DataMap tmpData = templateMapper.selectTemplate(map);

						DataMap sendParam = new DataMap();
						sendParam.put("tranPhone", sweetTracker.getString("TRAN_PHONE"));
						sendParam.put("campRowid", campaign.getString("ROWID"));
						sendParam.put("sessionUserRowId", campaign.getString("ACCT_ROWID"));
						sendParam.put("regCampType", campaign.getString("REG_CAMP_TYPE"));
						sendParam.put("tmpDtlRowid", tmpData.getString("tmpDtlRowid"));
						sendParam.put("tmpUseTypeCode", tmpGrp.getString("TMP_USE_TYPE_CODE"));
						sendParam.put("tranDate", tmpGrp.getString("TRAN_DATE"));
						sendParam.put("crmYn", "N");

						// ?????? ????????? ????????????
						map.put("sessionUserRowId", campaign.getString("ACCT_ROWID"));
						map.put("campRowid", campaign.getString("ROWID"));
						List<DataMap> addressVals = addrService.selectExcelAddress(map);
						// List<DataMap> addressVals =
						// outviserDao.selectList("enliple.ibot.outviser.api.send.selectExcelAddress",
						// map);

						for (DataMap addressVal : addressVals) {
							if (sweetTracker.getString("INVOICE_NO").equals(addressVal.getString("var4"))) {
								for (int i = 1; i <= 20; i++) {
									if (!"".equals(addressVal.getString("var" + i))) {
										if (i == 3) {
											sendParam.put("??????" + i, StaticCode.getCodeName("DELIVERY_COMPANY_CODE",
													addressVal.getString("var" + i)));
										} else {
											sendParam.put("??????" + i, addressVal.getString("var" + i));
										}
									}
								}

								sendParam.put("??????", addressVal.getString("name"));
								sendParam.put("????????????", addressVal.getString("phoneNo"));
								sendParam.put("?????????", addressVal.getString("email"));
								sendParam.put("email", addressVal.getString("email"));

							}
						}

						String man = param.getString("man");
						String tel_man = param.getString("telnoMan");
						if (!"".equals(man) && !"".equals(tel_man)) {
							man += "(" + tel_man + ")";
						}
						sendParam.put("???????????????(?????????)", man);
						sendParam.put("???????????????", param.getString("recvAddr"));

						result = this.requestSendTracker(sendParam);
					}
				}
			}

			// ????????????
			if (level == 6) {
				// crm ??????
				if ("".equals(campRowid)) {
					DataMap body = new DataMap();
					body.put("adverId", sweetTracker.getString("ADVER_ID"));
					body.put("deliveryComCd", sweetTracker.getString("COURIER_CODE"));
					body.put("invoiceNo", sweetTracker.getString("INVOICE_NO"));
					body.put("orderNo", sweetTracker.getString("ORDER_NO"));
					body.put("deliveryDate", param.getString("time_trans"));

					logger.debug("crm api body : " + body.toString());

					RestApiUtils.callRestApi(mobtuneApiUrl + "delivery-complete", "POST", "JSON", JsonUtils.toString(body));
				}
			}
		}

		return result;
	}

	public DataMap insertNonCrmCampDeliveyTracking(DataMap param) throws Exception {
		DataMap result = new DataMap();

		DataMap campaign = regCampaignMapper.selectCampaignMst(param);

		param.put("acctRowid", campaign.getString("ACCT_ROWID"));
		param.put("campaignRowid", campaign.getString("ROWID"));

		// ?????? ????????? ????????????
		DataMap map = new DataMap();
		map.put("sessionUserRowId", campaign.getString("ACCT_ROWID"));
		map.put("campRowid", campaign.getString("ROWID"));
		List<DataMap> addressVars = addrService.selectExcelAddress(map);
		// List<DataMap> addressVars =
		// outviserDao.selectList("enliple.ibot.outviser.api.send.selectExcelAddress",
		// map);

		List<DataMap> reqDatas = new ArrayList<>();

		for (DataMap address : addressVars) {
			DataMap invoiceData = new DataMap();
			invoiceData.put("no", address.getString("var4"));
			invoiceData.put("deliveryCorpCode", address.getString("var3"));
			List<DataMap> invoiceDatas = new ArrayList<>();
			invoiceDatas.add(invoiceData);

			DataMap deliveryData = new DataMap();
			deliveryData.put("orderId", address.getString("var1"));
			deliveryData.put("invoiceDatas", invoiceDatas);
			List<DataMap> deliveryDatas = new ArrayList<>();
			deliveryDatas.add(deliveryData);

			DataMap reqData = new DataMap();
			reqData.put("tranPhone", address.getString("phoneNo"));
			reqData.put("deliveryDatas", deliveryDatas);
			reqDatas.add(reqData);
		}

		param.put("reqDatas", reqDatas);

		result = insertSweetTrackerTune(param);

		return result;
	}

	public DataMap selectSweetTrackerCharge(DataMap param) throws Exception {
		DataMap dataMap = sendMapper.selectSweetTrackerCharge(param);
		if (dataMap == null) {
			throw new CommonException("SweetTrackerService > selectSweetTrackerCharge : dataMap == null", param);
		}
		return dataMap;
	}

	public void updateSweetTrackerLog(DataMap param) {
		int level = param.getInt("level");

		if (level != -99) {
			param.put("ERR_YN", "N");
		} else {
			param.put("ERR_YN", "Y");
		}
		sendMapper.insertSweetTrackerLog(param);
		sendMapper.updateSweetTrackerMst(param);
	}

	private DataMap requestSendTracker(DataMap param) throws Exception {

		DataMap result = new DataMap();

		if (param.isInvalid("campRowid")) {
			result.setHttpStatusAndResult(DataMap.HttpStatus.BAD_REQUEST, "F0006");
			return result;
		}

		try {

			DataMap reqData = regCampaignService.selectRequiredData(param);

			if (reqData == null) {
				result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "F0007");
				return result;
			}

			if (reqData.getString("exeStatus").equals("LIVE")
					&& reqData.getString("tmpDtlAtlkInspectionStatus").equals("APR")) {
				param.put("crmSend", false);
				param.put("exeRowid", reqData.get("exeRowid"));
				param.put("campRowid", reqData.get("campRowid"));
				param.put("crmCampNo", reqData.get("crmCampNo"));
				param.put("templateRowid", reqData.get("templateRowid"));
				param.put("tranDate", param.containsKey("tranDate") ? param.get("tranDate") : reqData.get("tranDate"));
				param.put("iVisorCampName", reqData.get("regName"));
				param.put("regOverlapSendYn", reqData.get("regOverlapSendYn"));
				param.put("regOverlapSendTerm", reqData.get("regOverlapSendTerm"));
				param.put("regCampType", reqData.get("regCampType"));
				param.put("phoneNo", param.getString("tranPhone"));
				param.put("uuid", CommonUtils.getUUID());

				// ?????? ????????? ??????
				apiTempService.createTempReqDataLogic(param);
				
				// ?????? ?????? ????????? ????????? ??????
				exceptFilterService.createTempFiterReqDataLogic(param);

				// ?????? ?????? ??????
				exeRunService.insertExeRunHst(param);

				result = sendService.sendProcess(param);
			} else {
				result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "E0000");
				result.put("rsMsg", "?????? ????????? ???????????? ???????????? ????????????.");
			}
		} catch (Exception e) {
			result.setHttpStatusAndResult(DataMap.HttpStatus.OK, "E0000");
			if (e != null)
				result.put("rsMsg", e.getMessage());
		} finally {
			// ?????? ????????? ??????
			if (param.containsKey("tempTableName")) {
				apiTempService.dropTempReqData(param);
			}
			
			// ?????? ?????? ????????? ??????
			if (param.containsKey("tempName")) {
				exceptFilterService.dropFilterData(param);
			}
		}

		return result;
	}

	/* ?????? ???????????? insert ??????
	public int insertSweetTracker(DataMap param) throws Exception {
		return outviserDao.insert("enliple.ibot.outviser.api.sweetTracker.insertSweetTracker", param);
	}
	*/
}
