package com.enliple.outviserbatch.outviser.front.urlshortener.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.exception.CommonException;
import com.enliple.outviserbatch.common.util.CommonUtils;
import com.enliple.outviserbatch.common.util.DateUtils;
import com.enliple.outviserbatch.outviser.front.shortenUrl.dtl.service.ShortenUrlDtlService;
import com.enliple.outviserbatch.outviser.front.shortenUrl.mst.service.ShortenUrlMstService;
import com.enliple.outviserbatch.outviser.front.template.service.TemplateService;

@Service
public class UrlShortenerV2Service {

	@Autowired
	private ShortenUrlDtlService shortenUrlDtlService;

	@Autowired
	private ShortenUrlMstService shortenUrlMstService;

	@Autowired
	private TemplateService templateService;

	@Value("${shorten.url.front}")
	private String shortenUrl;

	private DataMap m_urlMap = new DataMap();
	private String m_lastMarkingDt = "";
	private DataMap m_headCodeMap = new DataMap(); // 집행별 고유 2자리 코드 저장
	private final int m_splitSize = 3000; // 분할처리 사이즈

	/**
	 * 단축URL 셋팅 정보 조회
	 * 
	 * @param tmpDtlRowid
	 * @return
	 * @throws Exception
	 */
	public DataMap getUrlSettings(int exeRunHstRowid, int tmpDtlRowid) throws Exception {
		DataMap sMap = new DataMap();

		List<DataMap> urlMstListForLink = this.getUrlMstList(tmpDtlRowid, "LNK");
		boolean isExistLink = (urlMstListForLink == null || urlMstListForLink.size() == 0) ? false : true;

		List<DataMap> urlMstListForButton = this.getUrlMstList(tmpDtlRowid, "BTN");
		boolean isExistButton = (urlMstListForButton == null || urlMstListForButton.size() == 0) ? false : true;
		
		List<DataMap> urlMstListForImgLink = this.getUrlMstList(tmpDtlRowid, "IMG");
		boolean isExistImgLink = (urlMstListForImgLink == null || urlMstListForImgLink.size() == 0) ? false : true;

		sMap.put("urlMstListForLink", urlMstListForLink); // 본문 단축URL 정보
		sMap.put("isExistLink", isExistLink); // 본문 단축URL 존재 여부
		sMap.put("urlMstListForButton", urlMstListForButton); // 버튼 단축URL 정보
		sMap.put("isExistButton", isExistButton); // 버튼 단축URL 존재 여부
		sMap.put("urlMstListForImgLink", urlMstListForImgLink); // 본문 단축URL 정보
		sMap.put("isExistImgLink", isExistImgLink); // 본문 단축URL 존재 여부

		if (isExistButton) {
			sMap.put("tmpLinkList", this.getTmpLinkList(tmpDtlRowid)); // 템플릿 버튼 링크 정보
		}

		if (m_lastMarkingDt.equals(DateUtils.getCurrentDate()) == false) { // 하루 지나면 초기화
			m_lastMarkingDt = DateUtils.getCurrentDate();
			m_headCodeMap = new DataMap();
		}

		if (isExistLink || isExistButton || isExistImgLink) {
			String headCode;
			do {
				headCode = this.getHeadCode(); // 말머리코드 생성
			} while (m_headCodeMap.containsValue(headCode));

			m_headCodeMap.put(String.valueOf(exeRunHstRowid), headCode);
		}

		return sMap;
	}

	/**
	 * 단축 URL 코드 생성 (단건)
	 * 
	 * @param len
	 * @return
	 */
	public String createRandomCode(int len) {
		return CommonUtils.getRandomCode("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_", len);
	}

	public String createRandomCode(String uniqKey, int seq) {
		return m_headCodeMap.getString(uniqKey) + Integer.toString(seq, 36);
	}

	/**
	 * 말머리코드를 생성
	 * 
	 * @param len
	 * @return
	 * @throws Exception
	 */
	private String getHeadCode() throws Exception {
		int len = 4;
		DataMap param = new DataMap();
		param.put("len", len);

		int nRst = 0;
		do {
			param.put("headCode", createRandomCode(len));
			nRst = shortenUrlDtlService.selectShortenUrlDtlCount(param); // 현재 DB에 해당 앞자리 사용 중인가?
		} while (nRst > 0);

		return param.getString("headCode");
	}

	private List<DataMap> getUrlMstList(int tmpDtlRowid, String urlType) throws Exception {
		DataMap param = new DataMap();
		param.put("tmpDtlRowid", tmpDtlRowid);
		param.put("urlType", urlType);
		param.put("delYn", "N");

		List<DataMap> urlMstList = shortenUrlMstService.selectShortenUrlMstList(param); // tmpDtlRowid로 단축 URL 마스터 조회
		return urlMstList;
	}

	private List<DataMap> getBulkUrlDtlList(int exeRunHstRowid) {
		String key = "BULK_" + exeRunHstRowid;

		if (m_urlMap.containsKey(key) == false) {
			m_urlMap.put(key, new ArrayList<DataMap>());
		}

		return (List<DataMap>) m_urlMap.get(key);
	}

	/**
	 * 현재 URL이 몇번째인가
	 * 
	 * @param exeRunHstRowid
	 * @return
	 */
	private int getBulkUrlSeq(int exeRunHstRowid) {
		String key = "BULK_" + exeRunHstRowid + "_SEQ";

		if (m_urlMap.containsKey(key) == false) {
			m_urlMap.put(key, 1);
		} else {
			m_urlMap.put(key, m_urlMap.getInt(key) + 1);
		}

		return m_urlMap.getInt(key);
	}

	private void setBulkUrlDtlList(int exeRunHstRowid, DataMap subParam) throws Exception {
		List<DataMap> bulkList = this.getBulkUrlDtlList(exeRunHstRowid);

		int seq = this.getBulkUrlSeq(exeRunHstRowid);
		subParam.put("code", createRandomCode(String.valueOf(exeRunHstRowid), seq));
		bulkList.add(subParam);

		// 분할건수 채워질때마다 INSERT 처리
		if (seq % m_splitSize == 0) {
			this.insertBulkShortenUrlDtlList(exeRunHstRowid, false);
		}
	}

	/**
	 * 단축URL 벌크 INSERT 처리
	 * 
	 * @param exeRunHstRowid
	 * @throws Exception
	 */
	public void insertBulkShortenUrlDtlList(int exeRunHstRowid, boolean isLast) throws Exception {
		List<DataMap> bulkList = this.getBulkUrlDtlList(exeRunHstRowid);

		if (bulkList != null && bulkList.size() > 0) {
			int nRst = shortenUrlDtlService.insertBulkShortenUrlDtlList(bulkList);
			if (nRst == bulkList.size()) {
				m_urlMap.remove("BULK_" + exeRunHstRowid); // 성공했으면 임시 저장 리스트 삭제
			} else {
				int fromIdx = m_urlMap.getInt("BULK_" + exeRunHstRowid + "_SEQ") - m_splitSize;
				int toIdx = m_urlMap.getInt("BULK_" + exeRunHstRowid + "_SEQ");
				DataMap dataMap = new DataMap();
				dataMap.put("fromIdx", fromIdx);
				dataMap.put("toIdx", toIdx);
				throw new CommonException("단축URL 저장중 오류 발생 - insertBulkShortenUrlDtlList", dataMap);
			}
		}

		if (isLast) {
			m_urlMap.remove("BULK_" + exeRunHstRowid + "_SEQ"); // 마지막이면 SEQ도 삭제
			m_headCodeMap.remove(String.valueOf(exeRunHstRowid)); // 사용한 말머리코드도 삭제
		}
	}

	public void insertBulkShortenUrlDtlList(int exeRunHstRowid) throws Exception {
		this.insertBulkShortenUrlDtlList(exeRunHstRowid, true);
	}

	/**
	 * 발송 전 본문 단축 URL 최종 처리 (단축 URL 마스터 UPDATE 후 발송 본문 내 URL 코드 치환)
	 * 
	 * @param nSendCnt
	 * @param param
	 * @throws Exception
	 */
	public void replaceShortenUrlFromBody(DataMap urlSetting, int exeRunHstRowid, DataMap param) throws Exception {
		if (urlSetting.getBoolean("isExistLink")) {
			try {
				// URL 갯수만큼 for 처리
				for (DataMap urlMst : (List<DataMap>) urlSetting.get("urlMstListForLink")) {
					DataMap subParam = new DataMap();
					subParam.put("urlMstRowid", urlMst.getInt("ROWID"));
					subParam.put("urlGrpKey", param.getString("urlGrpKey"));

					this.setBulkUrlDtlList(exeRunHstRowid, subParam);

					StringBuilder sb = new StringBuilder();
					sb.append("#{");
					sb.append(urlMst.getString("URL_TYPE"));
					sb.append(urlMst.getString("URL_NO"));
					sb.append("}");
					String addVar = sb.toString(); // 코드로 치환 할 변수
					param.put("sendMessage", param.getString("sendMessage").replace(addVar, subParam.getString("code")));

					// 대체문자 내 단축 URL 코드 치환
					if ("N".equals(param.getString("tranReplaceType")) == false) {
						param.put("tranReplaceMessage", param.getString("tranReplaceMessage").replace(addVar, subParam.getString("code")));
					}
				}
			} catch (Exception e) {
				throw new CommonException(param, e);
			}
		}
	}

	private List<DataMap> getTmpLinkList(int tmpDtlRowid) throws Exception {
		List<DataMap> tmpLinkList = templateService.selectTemplateLink(tmpDtlRowid);
		return tmpLinkList;
	}

	/**
	 * 발송 전 버튼 단축 URL 최종 처리
	 * 
	 * @param tmpDtlRowid
	 * @return
	 * @throws Exception
	 */
	public List<DataMap> replaceShortenUrlFromButton(DataMap urlSetting, int exeRunHstRowid, DataMap param)
			throws Exception {

		List<DataMap> btnList = new ArrayList<DataMap>();

		if (urlSetting.getBoolean("isExistButton")) {
			try {
				// tmpDtlRowid로 단축 URL 마스터 조회
				List<DataMap> urlMstList = (List<DataMap>) urlSetting.get("urlMstListForButton");

				for (DataMap tmpLink : (List<DataMap>) urlSetting.get("tmpLinkList")) {
					DataMap btn = new DataMap();
					btn.put("name", tmpLink.getString("tmpLinkName"));
					btn.put("type", tmpLink.getString("tmpLinkType"));
					btn.put("url_mobile", tmpLink.getString("tmpLinkMobile")); // 일반 URL 들어있는 기존 데이터 처리를 위함..
					btn.put("url_pc", tmpLink.getString("tmpLinkPc")); // 일반 URL 들어있는 기존 데이터 처리를 위함(2)..

					List<DataMap> filteredUrlMstList = urlMstList.stream()
							.filter(dataMap -> (dataMap.getInt("rowid") == tmpLink.getInt("urlMstRowidMobile") || dataMap.getInt("rowid") == tmpLink.getInt("urlMstRowidPc")))
							.collect(Collectors.toList());
					for (DataMap urlMst : filteredUrlMstList) {
						DataMap subParam = new DataMap();
						subParam.put("urlMstRowid", urlMst.getInt("ROWID"));
						subParam.put("urlGrpKey", param.getString("urlGrpKey"));

						this.setBulkUrlDtlList(exeRunHstRowid, subParam);

						// 버튼 단축 URL 코드 치환
						String url = shortenUrl + subParam.getString("code");

						if ("BMO".equals(urlMst.getString("URL_TYPE"))) {
							btn.put("url_mobile", url);
						} else if ("BPC".equals(urlMst.getString("URL_TYPE"))) {
							btn.put("url_pc", url);
						}
					}

					btnList.add(btn);
				}
			} catch (Exception e) {
				throw new CommonException(param, e);
			}
		}

		return btnList;
	}
	
	
	/**
	 * 발송 전 이미지 단축 URL 
	 * 
	 * @throws Exception
	 */
	public String replaceShortenUrlFromImgLink(DataMap urlSetting, int exeRunHstRowid, DataMap param) throws Exception {
		String imgLink = "";

		if (urlSetting.getBoolean("isExistImgLink")) {
			try {
				DataMap tmpLink = ((List<DataMap>) urlSetting.get("urlMstListForImgLink")).get(0); 
	
				DataMap subParam = new DataMap();
				subParam.put("urlMstRowid", tmpLink.getInt("ROWID"));
				subParam.put("urlGrpKey", param.getString("urlGrpKey"));

				this.setBulkUrlDtlList(exeRunHstRowid, subParam);

				// 단축 URL 코드 치환
				imgLink = shortenUrl + subParam.getString("code");
				
			} catch (Exception e) {
				throw new CommonException(param, e);
			}
		}

		return imgLink;
	}
}