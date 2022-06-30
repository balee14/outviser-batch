package com.enliple.outviserbatch.common.statics;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

import com.enliple.outviserbatch.common.action.service.StartUpService;
import com.enliple.outviserbatch.common.data.DataMap;

public class StaticCode {

	public static DataMap codeList = new DataMap();
	public static DataMap codeValue = new DataMap();
	
	public static void initCode() throws Exception {
		ApplicationContext appContext = ContextLoader.getCurrentWebApplicationContext();
		StartUpService startUpService = (StartUpService)appContext.getBean(StartUpService.class);
		
		initCode(startUpService.selectCodeList());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void initCode(List<DataMap> list) throws Exception {
	
		if(null != list && list.size() > 0){
			
			String codeGrpId = list.get(0).getString("MST_CODE");
			
			List tmepCodeList = new ArrayList();
			DataMap tmepCodeValue = new DataMap();
			for(int i=0; i<list.size(); i++){
				DataMap map = list.get(i);
				String ckCodeGrpId = map.getString("MST_CODE");
				if(!codeGrpId.equals(ckCodeGrpId)){
					codeList.put(codeGrpId, tmepCodeList);
					codeValue.put(codeGrpId, tmepCodeValue);
					tmepCodeList = new ArrayList();
					tmepCodeValue = new DataMap();
				}
				
				tmepCodeList.add(list.get(i));
				tmepCodeValue.put(map.getString("CODE")+"_VALUE", map.getString("NAME"));
								
				codeGrpId = map.getString("MST_CODE");
			}
			
			codeList.put(codeGrpId, tmepCodeList);
			codeValue.put(codeGrpId, tmepCodeValue);
		}
	}
	
	/**
	 * 코드명 가져오기
	 * @param grp
	 * @param code
	 * @return
	 */
	public static String getCodeName(String grp, String code){
		DataMap grpMap = (DataMap)codeValue.get(grp);
		if(null == grpMap) return "";
		return (String)grpMap.get(code+"_VALUE");
	}
	
	/**
	 * 코드 리스트 가져오기
	 * @param grp
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<DataMap> getCodeList(String grp){
		return (List<DataMap>)codeList.get(grp);
	}
	
	/**
	 * 구분자 코드값
	 * @param grp
	 * @param str
	 * @return
	 */
	public static String getCodeNameDelimiter(String grp, String str, String oriDelimiter,String newDelimiter){
		String returnVal = "";
		
		if("".equals(str)){
			return returnVal;
		}
		
		String[] strArr = str.split(oriDelimiter);
		if(null == strArr || strArr.length == 0){
			return returnVal;
		}
		
		String at = "";
		for(int i=0; i<strArr.length; i++){
			returnVal += at + getCodeName(grp, strArr[i]);
			at = newDelimiter;
		}
		
		return returnVal;
	}
}