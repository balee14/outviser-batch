package com.enliple.outviserbatch.outviser.front.exceptFilter.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.util.DateUtils;
import com.enliple.outviserbatch.outviser.front.exceptFilter.mapper.ExceptFilterMapper;
import com.enliple.outviserbatch.outviser.front.request.service.RequestService;

@Service
public class ExceptFilterService {
    
    @Autowired
    private ExceptFilterMapper exceptFilterMapper;
    
    @Autowired
	private RequestService requestService;
    
    /**
     * 임시 테이블 생성
     * 
     * @param param
     * @throws Exception
     */
    public void createTempFiterReqDataLogic(DataMap param) throws Exception {
    	// 중복 수신 처리용 임시 테이블 생성
        String filterStr = param.getLong("crmCampNo") > 0 ? "CRM" : "ADDR";
        // 임시테이블 생성
		String tempName = "TEMP_"+ filterStr +"FILTER_"
				/*+ param.getString("campRowid")
				+ "_"*/
				+ DateUtils.getCurrentDate("yyyyMMddHHmmss")
				+ "_"
				+ param.getString("uuid");
		param.put("tempName", tempName);
		
		if(param.getLong("crmCampNo") > 0)
			this.createCrmIntersectFilterData(param);
		else
			this.createAddressFilterData(param);
    }
    
    
    /**
     * @param param
     * @return
     * @throws Exception
     */
    public List<DataMap> exceptFilterAddr(DataMap param, List<DataMap> reqDatas) throws Exception {
    	List<DataMap> resultMap = new ArrayList<DataMap>();
        DataMap paramData = new DataMap();
        
        try { 
	    	// 계정의 설정 정보 가져오기 - 중간에 변경 될수 있으니 여기서 로딩
//        	DataMap userInfo = exceptFilterMapper.selectExceptInfo(param);
	    	DataMap userInfo = this.selectUserInfo(param);
	    	
	        int checkDays = userInfo.get("acctExcDays") != null ? userInfo.getInt("acctExcDays") : 0;
	        String checkTemplete = userInfo.get("acctExcTmpYn") != null ? userInfo.getString("acctExcTmpYn") : "N";
	    	
	    	if(checkDays == 0)// && checkTemplete == 0)
	    		return reqDatas;
	    	
			paramData.put("checkDays", checkDays);
			paramData.put("checkTemplete", checkTemplete);
			paramData.put("tmpDtlRowid", param.getInt("tmpDtlRowid"));
			paramData.put("tmpDtlType", param.getString("tmpDtlType"));
			paramData.put("sessionUserRowId", param.getInt("sessionUserRowId"));
			paramData.put("tempName", param.getString("tempName"));
			
	        // 임시테이블 데이터 기록
			int loopSize = 10000;
	        int loopIdx = 0;
	        while(loopIdx < reqDatas.size()) {
	            List<DataMap> loopReqDatas = reqDatas.subList(loopIdx, reqDatas.size() - loopIdx >= loopSize ? loopIdx + loopSize : reqDatas.size());
	            paramData.put("datas", loopReqDatas);

	            exceptFilterMapper.insertAddressFilterList(paramData);
	            loopIdx += loopSize;
	        }
	        
//	        // 중복 데이터 검증
            resultMap.addAll(exceptFilterMapper.selectAddressFilterList(paramData));//교집합
            
            List<DataMap> reqIntersectList = exceptFilterMapper.selectAddressIntersectFilterList(paramData);//차집합
            insertIntersectFailList(param, reqIntersectList);
	        
        }catch(Exception e) {
        	throw new Exception("중복 수신 검증(주소록) 중 오류가 생겼습니다. - exceptFilterAddr(" + e.getMessage() + ")");
        }
        
        return resultMap;//reqDatas;//
    }
    
    /**
     * @param param
     * @return
     * @throws Exception
     */
    public List<DataMap> intersectFilterCrm(DataMap param, List<DataMap> reqDatas) throws Exception {
    	List<DataMap> resultMap = new ArrayList<DataMap>();
        DataMap paramData = new DataMap();
        
        try { 
	    	
        	int checkDays = param.get("acctExcDays") != null ? param.getInt("acctExcDays") : 0;
	        String checkTemplete = param.get("acctExcTmpYn") != null ? param.getString("acctExcTmpYn") : "N";
        	
//	    	if(checkDays == 0)// && checkTemplete == 0)
//	    		return reqDatas;
	    	
			paramData.put("checkDays", checkDays);
			paramData.put("checkTemplete", checkTemplete);
			paramData.put("tmpDtlRowid", param.getInt("tmpDtlRowid"));
			paramData.put("tmpDtlType", param.getString("tmpDtlType"));
			paramData.put("sessionUserRowId", param.getInt("sessionUserRowId"));
			paramData.put("tempName", param.getString("tempName"));

	        // 임시테이블 데이터 기록
			int loopSize = 10000;
	        int loopIdx = 0;
	        while(loopIdx < reqDatas.size()) {
	            List<DataMap> loopReqDatas = reqDatas.subList(loopIdx, reqDatas.size() - loopIdx >= loopSize ? loopIdx + loopSize : reqDatas.size());
	            paramData.put("datas", loopReqDatas);

	            exceptFilterMapper.insertCrmIntersectFilterList(paramData);
	            loopIdx += loopSize;
	        }

	        List<DataMap> intersectData = new ArrayList<DataMap>();
	        intersectData = exceptFilterMapper.selectCrmIntersectFilterList(paramData);

	        if(intersectData.size() > 0)
	        	resultMap.addAll(intersectData);

        }catch(Exception e) {
        	throw new Exception("중복 수신 검증(CRM) 중 오류가 생겼습니다. - exceptFilterCrm(" + e.getMessage() + ")");
        }
        
        return resultMap;//reqDatas;//
    }
    
    /**
     * @param param
     * @throws Exception
     */
    public void insertIntersectFailList(DataMap param, List<DataMap> reqIntersectList) throws Exception {
    	List<DataMap> failDatas = new ArrayList<DataMap>(); // 발송 요청 실패 데이터
		List<DataMap> failDetails = new ArrayList<DataMap>(); // 발송 요청 실패 상세 데이터
    	
    	for(DataMap intersect : reqIntersectList) {
    		intersect.put("campRowid", param.get("campRowid"));
    		intersect.put("tmpDtlRowid", param.get("tmpDtlRowid"));
    		intersect.put("tmpDtlType", param.get("tmpDtlType"));
    		
    		intersect.put("sendPhoneNo", intersect.get("phoneNo"));
    		intersect.put("tranEmail", intersect.get("email"));
    		
    		intersect.put("sessionUserRowId", param.get("sessionUserRowId"));
    		intersect.put("exeRunHstRowid", param.get("exeRunHstRowid"));
    		intersect.put("sessionAdverId", param.get("sessionAdverId"));
    		intersect.put("reqSiteUserId", intersect.containsKey("userId") ? intersect.getString("userId") : null);
    		
    		if (param.getString("tmpDtlType").equals("ATL")) {
    			intersect.put("tranType", 5);
    			if (param.getString("tmpDtlAttachYn").equals("Y")) {
    				param.put("prodDtlType", "ATLP");
    				intersect.put("tranType", 51);
    			}
    		} else if (param.getString("tmpDtlType").equals("FTL")) {
				if (param.getString("tmpDtlAttachYn").equals("Y")) // 친구톡+이미지인 경우 별도 금액 산정이라면 ...
					param.put("prodDtlType", "FTLP");
				intersect.put("tranType", 6);
			} else if (param.getString("tmpDtlType").equals("SMS"))
				intersect.put("tranType", 0);
			else if (param.getString("tmpDtlType").equals("LMS"))
				intersect.put("tranType", 4);
			else if (param.getString("tmpDtlType").equals("MMS"))
				intersect.put("tranType", 4);
			else if (param.getString("tmpDtlType").equals("EML"))
				intersect.put("tranType", 99);
    		
    		intersect.put("tranCallBack", param.get("tranCallBack"));
    		intersect.put("tranDate", param.get("tranDate"));
    		intersect.put("uuid", param.get("uuid"));
    		
    		String errorMsg = "중복 수신 제외";
	    	DataMap failDetail = new DataMap();
	    	intersect.put("errorMsg", errorMsg);
			failDetail.put("errorMsg", errorMsg);
			failDetails.add(failDetail);
			failDatas.add(intersect);
    	}
		
		if (failDatas.size() > 0) {
			int loopFailIdx = 0;
			while (loopFailIdx < failDatas.size()) {
				List<DataMap> loopfailDatas = failDatas.subList(loopFailIdx,
						failDatas.size() - loopFailIdx >= 1000 ? loopFailIdx + 1000 : failDatas.size());
				requestService.insertSendFailData(loopfailDatas);
				loopFailIdx += 1000;
			}
		}
	}
    
    /**
     * @param param
     * @throws Exception
     */
    public void createAddressFilterData(DataMap param) throws Exception {
    	exceptFilterMapper.createAddressFilterData(param);
    }
    
    /**
     * @param param
     * @throws Exception
     */
    public void createCrmIntersectFilterData(DataMap param) throws Exception {
    	exceptFilterMapper.createCrmIntersectFilterData(param);
    }
    
    /**
     * @param param
     * @throws Exception
     */
    public void dropFilterData(DataMap param) throws Exception {
    	exceptFilterMapper.dropFilterData(param);
    }
    
    /**
     * @param param
     * @throws Exception
     */
    public DataMap selectUserInfo(DataMap param) throws Exception {
    	// 계정의 설정 정보 가져오기 - 중간에 변경 될수 있으니 여기서 로딩
    	DataMap userInfo = exceptFilterMapper.selectExceptInfo(param);
    	return userInfo;
    }
    
    
    
    
    /**
     * @param param
     * @return
     * @throws Exception
     */
    /*
    public List<DataMap> exceptFilterCrm(DataMap param, List<DataMap> reqDatas) throws Exception {
    	List<DataMap> resultMap = new ArrayList<DataMap>();
        DataMap paramData = new DataMap();
        
        try { 
	    	// 계정의 설정 정보 가져오기 - 중간에 변경 될수 있으니 여기서 로딩
        	DataMap userInfo = exceptFilterMapper.selectExceptUserInfo(paramData);
	    	
	        int checkDays = userInfo.get("acctExcDays") != null ? userInfo.getInt("acctExcDays") : 0;
	        String checkTemplete = userInfo.get("acctExcTmpYn") != null ? userInfo.getString("acctExcTmpYn") : "N";
	    	
	    	if(checkDays == 0)// && checkTemplete == 0)
	    		return reqDatas;
	    	
			paramData.put("checkDays", checkDays);
			paramData.put("checkTemplete", checkTemplete);
			paramData.put("tmpDtlRowid", param.getInt("tmpDtlRowid"));
			paramData.put("tmpDtlType", param.getString("tmpDtlType"));
			paramData.put("sessionUserRowId", param.getInt("sessionUserRowId"));
	
	        // 임시테이블 생성
			String tempName = "TEMP_ADDRFILTER_" + param.getString("campRowid") + "_" + DateUtils.getCurrentDate("yyyyMMddHHmmss");
			paramData.put("tempName", tempName);

			// crm용 임시 테이블 생성
			exceptFilterMapper.createCrmFilterData(paramData);
	        
	        // 임시테이블 데이터 기록
			int loopSize = 10000;
	        int loopIdx = 0;
	        while(loopIdx < reqDatas.size()) {
	            List<DataMap> loopReqDatas = reqDatas.subList(loopIdx, reqDatas.size() - loopIdx >= loopSize ? loopIdx + loopSize : reqDatas.size());
	            paramData.put("datas", loopReqDatas);

	            exceptFilterMapper.insertCrmFilterList(paramData);
	            loopIdx += loopSize;
	        }
	        
            resultMap.addAll(exceptFilterMapper.selectCrmFilterList(paramData));
        
        }catch(Exception e) {
        	throw new Exception("중복 수신 검증(CRM) 중 오류가 생겼습니다. - exceptFilterCrm(" + e.getMessage() + ")");
        }finally {
        	// 임시테이블 삭제
        	if (paramData.containsKey("tempName")) {
        		exceptFilterMapper.dropCrmFilterData(paramData);
        	}
        }
        
        return resultMap;//reqDatas;//
    }
    
    
    public List<DataMap> exceptFilterAddr() throws Exception {
    	// 테스트를 위해 임시로 설정
        DataMap param = new DataMap();
        param.put("sessionUserRowId", 1);
		param.put("campRowid", 1874);
		param.put("days", 7);
		param.put("templete", 0);
		param.put("tmpDtlRowid", 1874);
		param.put("tmpDtlType", "EML");
        List<DataMap> reqDatas = addrService.selectExcelAddress(param);
        return exceptFilterAddr(param, reqDatas);
    }
    
    public List<DataMap> exceptFilterCrmTEST() throws Exception {
    	DataMap param = new DataMap();
//    	List<DataMap> reqDatas = new ArrayList<DataMap>();
    	param.put("tempName", "TEMP_CRMFILTER_1874_20211206113754");
    	param.put("tmpDtlRowid", 1874);
    	param.put("tmpDtlType", "EML");
    	param.put("sessionUserRowId", 1);
    	param.put("campRowid", 1874);

    	List<DataMap> reqDatas = addrService.selectExcelAddress(param);
    	
    	return exceptFilterCrmTEST(param, reqDatas);
    }
    
    public List<DataMap> exceptFilterCrmTEST(DataMap param, List<DataMap> reqDatas) throws Exception {
    	List<DataMap> resultMap = new ArrayList<DataMap>();
        DataMap paramData = new DataMap();
        
        
        try { 
        	
        	// 계정의 설정 정보 가져오기 - 중간에 변경 될수 있으니 여기서 로딩
        	DataMap userInfo = exceptFilterMapper.selectExceptInfo(param);
	
        	log.warn(userInfo.toString());
        	
	        int checkDays = userInfo.get("acctExcDays") != null ? userInfo.getInt("acctExcDays") : 0;
	        String checkTemplete = userInfo.get("acctExcTmpYn") != null ? userInfo.getString("acctExcTmpYn") : "N";
	
	        // 임시테이블 생성
			paramData.put("tempName", param.get("tempName"));
			paramData.put("checkDays", checkDays);
			paramData.put("checkTemplete", checkTemplete);
			paramData.put("tmpDtlRowid", param.getInt("tmpDtlRowid"));
			paramData.put("tmpDtlType", "EML");
			paramData.put("sessionUserRowId", param.getInt("sessionUserRowId"));
			
			exceptFilterMapper.createCrmIntersectFilterData(paramData);

			log.warn("insert start");
			
//	        // 임시테이블 데이터 기록
			int loopSize = 10000;
	        int loopIdx = 0;
	        while(loopIdx < reqDatas.size()) {
	            List<DataMap> loopReqDatas = reqDatas.subList(loopIdx, reqDatas.size() - loopIdx >= loopSize ? loopIdx + loopSize : reqDatas.size());
	            paramData.put("datas", loopReqDatas);

	            exceptFilterMapper.insertCrmIntersectFilterList(paramData);
	            loopIdx += loopSize;
	            
	        }
	        
	        log.warn("result check");
	        
	        log.warn(paramData.getString("tmpDtlType"));
	        log.warn(paramData.getString("checkTemplete"));
	        
	        //exceptFilterMapper.selectCrmIntersectFilterList(paramData) 0건 일경우 오류 발생
	    
        
	        List<DataMap> intersectData = new ArrayList<DataMap>();
	        intersectData = exceptFilterMapper.selectCrmIntersectFilterList(paramData);
        
            resultMap.addAll(intersectData == null ? null : intersectData);
        
	        if(resultMap.size() > 0) {
		        log.warn("resultMap get(0) : " + resultMap.get(0));
		        log.warn("resultMap size : " + resultMap.size());
	        }
	        log.warn(paramData.getString("tmpDtlType"));
	        log.warn(paramData.getString("checkTemplete"));
	        
	        HashMap<String, String> hashMap = new HashMap<String, String>();
			for (DataMap intersect : resultMap) {
				hashMap.put(intersect.getString("checkKey"),"");
			}
	        

			DataMap cdata = new DataMap();
			cdata.put("phoneNo", "01000000099");
			
//			log.warn(hashMap.toString());
			log.warn(cdata.toString());
//        	if(hashMap.containsKey(cdata.getString("phoneNo")))
//        		log.warn("1231231231231231232");
	            
        	if(hashMap.containsKey(cdata.getString("email")))
        		log.warn("eeeeeeeeeeeeeeeeeeeeeeeeee");
        
        }catch(Exception e) {
        	log.error("EEEE : " + e);
        }
        
        return resultMap;//reqDatas;//
    }
*/
}