package com.enliple.outviserbatch.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.jdbc.support.JdbcUtils;

import com.enliple.outviserbatch.common.data.DataMap;

public class CommonUtils {

	public static String getUUID() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	public static String getUUID(int size) {
		return getUUID().substring(0, size);
	}

	/**
	 * 파일명
	 * @return 
	 */
	public static String getUniqueName() {   
		long currentTime = System.currentTimeMillis();   
		SimpleDateFormat simDf = new SimpleDateFormat("yyyyMMddHHmmss");   
		int randomNumber = (int)(Math.random()*999999);   
	 
		return CommonUtils.rpad(simDf.format(new Date(currentTime)) + Integer.toString(randomNumber), 20, "0");
	}
	
	/**
	 * 키값생성
	 * @return
	 */
	public static String getUnique() throws Exception{   
		long currentTime = System.currentTimeMillis();
		SimpleDateFormat simDf = new SimpleDateFormat("yyyyMMddHHmmss");
		int randomNumber = (int) (Math.random() * 999999999);   
		
		String txt = CommonUtils.rpad(simDf.format(new Date(currentTime)) + Integer.toString(randomNumber), 23, "0");
		
		return CryptogramUtils.md5Encode(txt);
	}
	
	/**
	 * 페이지값 넣기
	 * @param param
	 */
	public static void pageNum(DataMap param, int pageSize){
		int page = param.getInt("page"); 
		if(page == 0) page = 1;
		
		int startNum = 0;
		try{
			int temp = param.getInt("page");
			if(temp != 0){
				temp = ((temp-1) * pageSize);
			}
			startNum = temp;
		}catch(Exception e){}
		
		param.put("startNum", startNum);
		param.put("pageSize", pageSize);
		param.put("page", page);
	}
	
	/**
	 * totalCnt값
	 * @param list
	 * @return
	 */
	public static int totalCnt(List<DataMap> list){
		return totalCnt(list, "TOTAL_CNT");
	}
	
	public static int totalCnt(List<DataMap> list, String str){
		int totalCnt = 0;
		if (null == list) return 0;
		if(list.size() > 0){
			totalCnt = list.get(0).getInt(str);
		}
		return totalCnt;
	}
	
	/**
     * LPAD
     * @param str 원본 String
     * @param num LPAD 숫자
     * @param join 문자열
     * @return
     */
    public static String lpad(String str, int num, String join){
        String returnVal = str;
        String appendVal = "";
        if(str.length() >= num) return returnVal;
        
        for(int i=0; i<num-str.length();i++){
            appendVal += join;
        }
        
        returnVal = appendVal+returnVal;
        return returnVal;
    }
    
    /**
     * RPAD
     * @param str 원본 String
     * @param num RPAD 숫자
     * @param join 문자열
     * @return
     */
    public static String rpad(String str, int num, String join){
        
        String returnVal = str;
        String appendVal = "";
        if(str.length() >= num) return returnVal;
        
        for(int i=0; i<num-str.length();i++){
            appendVal += join;
        }
        
        returnVal = returnVal+appendVal;
        return returnVal;
    }
    
    /**
	 * int 타입으로 온 데이터 결과 메시지 만들기
	 * @param result
	 * @return
	 */
    public static DataMap resultMessage(int result){
    	return resultMessage(result, "정상적으로 처리되었습니다.");
    }
	public static DataMap resultMessage(int result, String success){
		
		DataMap resultMap = new DataMap();
		
		if(result > 0){
			resultMap.put("resultCode", "1");
			resultMap.put("resultMsg", success);
		}else{
			resultMap.put("resultCode", "-1");
			resultMap.put("resultMsg", "실패하였습니다");
		}
		
		return resultMap;
	}
	
	public static DataMap resultMap(int resultCode, String message){
		
		DataMap resultMap = new DataMap();
		
		resultMap.put("resultCode", resultCode);
		resultMap.put("resultMsg", message);
		
		return resultMap;
	}
	
	/**
	 * 검색조건 조합
	 * @param param
	 * @return
	 */
	public static String searchText(DataMap param){
		String[] strArr = {
					"page"
					, "keyfield"
					, "keyword"
					, "page_size"
				};
		return searchText(param, strArr);
	}
	
	public static String searchText(DataMap param, String[] strArr){
		String returnVal = "";

		String at = "";
		for(int i=0; i<strArr.length; i++){
			String temp = param.getString(strArr[i]);
			if(!"".equals(temp)){
				//temp = temp.replaceAll("\"", "");
				//temp = temp.replaceAll("\'", "");
				try {
					if(!("keyfield".equals(strArr[i]) && "".equals(param.getString("keyword")) || "keyword".equals(strArr[i]) && "".equals(param.getString("keyfield")))){
						returnVal += at + strArr[i] + "=" +URLEncoder.encode(temp, "UTF-8");
						at = "&";
					}
					//returnVal += at + strArr[i] + "=" +temp;
				} catch (UnsupportedEncodingException e) {}
			}
		}

		return returnVal;
	}
	
	/**
	 * urlEncode
	 * @param str
	 * @return
	 */
	public static String urlEncode(String str){
		String returnVal = "";
		try {
			returnVal = URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnVal;
	}
	
	/**
	 * urlDecode
	 * @param str
	 * @return
	 */
	public static String urlDecode(String str){
		String returnVal = "";
		try {
			returnVal = URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnVal;
	}
	
	/**
     * 문자열 14자리 변경
     * @param str
     * @param types 1 : 2001.10.20 11:22
     * @return
     */
    public static String stringToDate(String str, int types){
    	if("".equals(str) || str.length() < 8) return "";

    	String returnVal = "";

    	String yyyy = "";
    	String mm = "";
    	String dd = "";
        	        
    	String hh = "";
    	String mi = "";
    	String ss = "";

    	if(str.length() == 8){
    		yyyy = str.substring(0, 4);
        	mm = str.substring(4, 6);
        	dd = str.substring(6, 8);
    	}else if(str.length() == 10){
    		yyyy = str.substring(0, 4);
        	mm = str.substring(5, 7);
        	dd = str.substring(8, 10);
    	}else{
    		yyyy = str.substring(0, 4);
        	mm = str.substring(5, 7);
        	dd = str.substring(8, 10);
            
        	hh = str.substring(11, 13);
        	mi = str.substring(14, 16);
        	ss = str.substring(17, 19);
    	}
    	
    	switch (types) {
			case 1: 
				returnVal = yyyy+"."+mm+"."+dd;
				break;
			case 2: 
				returnVal = yyyy+"."+mm+"."+dd+" "+hh+":"+mi;
				break;
			case 3: 
				returnVal = yyyy+"."+mm+"."+dd+" "+hh+":"+mi+":"+ss;
				break;
			case 4: 
				returnVal = mm+"."+dd+" "+hh+":"+mi;
				break;
			case 5: 
				returnVal = mm+"-"+dd;
				break;
			case 11: 
				returnVal = yyyy+"-"+mm+"-"+dd;
				break;
			case 12: 
				returnVal = mm+"/"+dd;
				break;
			case 13: 
				returnVal = yyyy+"-"+mm+"-"+dd+" "+hh+":"+mi+":"+ss;
				break;
			case 21: 
				returnVal = yyyy+"년 "+mm+"월 "+dd+"일";
				break;
			case 22: 
				returnVal = yyyy+"년 "+mm+"월 "+dd+"일 "+hh+"시 "+mi+"분";
				break;
			case 23: 
				returnVal = yyyy+"년 "+mm+"월 "+dd+"일 "+hh+"시 "+mi+"분 "+ss+"초";
				break;
			case 31: 
				returnVal = yyyy+". "+mm+". "+dd;
				break;
			case 41: 
				returnVal = yyyy+mm+dd;
				break;
			case 51: 
				returnVal = yyyy+"."+mm;
				break;
			case 52: 
				returnVal = yyyy+"-"+mm;
				break;
			case 61: 
				returnVal = hh+":"+mi;
				break;
			default:
				break;
		}
    	
    	return returnVal;
    }
    
    /**
	 * 검색 체크
	 * @param str
	 * @param at
	 * @return
	 */
	public static String searchNoValue(String str, String at){
		if(str == "") return "";
		return at + str;
	}
	
	/**
     * 업로드된 파일 경로
     * @param str
     * @return
     */
	/*
    public static String uploadReplace(String str) {
        return ConfigUtils.getString("FILE.UPLOAD.LOGICAL")+str.replaceAll("\\\\", "/");
    }
     */
	
	/**
	 * 라인피드 br 치환
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public static String contentBr(String str) throws Exception {
		return StringEscapeUtils.escapeHtml(str).replaceAll("\n", "<br>");
	}
	
	/**
	 * 콤마로 구분된값을 코드값 유무 체크
	 * @param arrStr
	 * @param str
	 * @return
	 */
	public static boolean isCode(String arrStr, String str){
		
		boolean returnVal = false;
		
		String[] arr = arrStr.split(",");
		
		for(int i=0; i<arr.length; i++){
			if(arr[i].trim().equals(str)){
				returnVal = true;
				break;
			}
		}
		
		return returnVal;
	}
	
	/**
	 * 세자리 콤마
	 * @param num
	 * @return
	 */
	public static String comma(String num) {
		double n = 0;
		try {
			n = Double.parseDouble(num);
		} catch (Exception e) {
			return "";
		}
		
		DecimalFormat df = new DecimalFormat("#,###");
		return df.format(n);
	}
	
	/**
	 * 파일용량
	 * @param bytes
	 * @param si
	 * @return
	 */
	public static String readableByte(String str) {
		return readableByte(str, true);
	}
	public static String readableByte(String str, boolean si) {
		long bytes = 0;
		try{	
			bytes = Long.valueOf(str);
		}catch(Exception e){}

		int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + "B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f%sB", bytes / Math.pow(unit, exp), pre);
	}
	
	/**
	 * 맵에있는값 소문자로 변환
	 * @param map
	 * @return
	 */
	public static DataMap dataMapToLowerCase(DataMap map){
        DataMap resultMap = new DataMap();
		String keyAttribute = "";

        Iterator<Object> itr = map.keySet().iterator();
        while(itr.hasNext()){
            keyAttribute = (String) itr.next();
            resultMap.put(keyAttribute.toLowerCase(), map.get(keyAttribute));
            map.get(keyAttribute);
        }
        return resultMap;
    }
	
	/**
	 * DataMap에 있는 키 Underscores -> CamelCase로 변경
	 * @param map
	 * @return
	 */
	public static DataMap dataMapToCamelCase(DataMap map){
        DataMap resultMap = new DataMap();
		String keyAttribute = "";

        Iterator<Object> itr = map.keySet().iterator();
        while(itr.hasNext()){
            keyAttribute = (String) itr.next();
            resultMap.put(JdbcUtils.convertUnderscoreNameToPropertyName(keyAttribute.toLowerCase()), map.get(keyAttribute));
            map.get(keyAttribute);
        }
        return resultMap;
    }
	
	/**
	 * List&lt;DataMap>에 있는 키 Underscores -> CamelCase로 변경
	 * @param mapList
	 * @return
	 */
	public static List<DataMap> dataMapToCamelCase(List<DataMap> mapList){
		List<DataMap> resultList = new ArrayList<DataMap>();
		
		for (DataMap map : mapList) {
			resultList.add(dataMapToCamelCase(map));
		}
		
		return resultList;
	}	
	
	/**
	 * maskingName
	 *  
	 * @param str
	 * @return String
	 */
	public static String maskingName(String str) {
		String replaceString = str;

		String pattern = "";
		if(str.length() == 2) {
			pattern = "^(.)(.+)$";
		} else {
			pattern = "^(.)(.+)(.)$";
		}

		Matcher matcher = Pattern.compile(pattern).matcher(str);

		if(matcher.matches()) {
			replaceString = "";

			for(int i=1;i<=matcher.groupCount();i++) {
				String replaceTarget = matcher.group(i);
				if(i == 2) {
					char[] c = new char[replaceTarget.length()];
					Arrays.fill(c, '*');

					replaceString = replaceString + String.valueOf(c);
				} else {
					replaceString = replaceString + replaceTarget;
				}

			}
		}
		return replaceString;
	}
	
	/**
	 * encrypt number in text
	 * 
	 * maskingCallNumber
	 *  
	 * @param str
	 * @return String
	 */
	public static String maskingCallNumber(String phoneNum){
		 /*
	      * 요구되는 휴대폰 번호 포맷
	      * 01055557777 또는 0113339999 로 010+네자리+네자리 또는 011~019+세자리+네자리 이!지!만!
	      * 사실 0107770000 과 01188884444 같이 가운데 번호는 3자리 또는 4자리면 돈케어
	      * */
	      String regex = "(01[016789])(\\d{3,4})\\d{4}$";
	      Matcher matcher = Pattern.compile(regex).matcher(phoneNum);
	      if (matcher.find()) {
	         String replaceTarget = matcher.group(2);
	         char[] c = new char[replaceTarget.length()];
	         Arrays.fill(c, '*');
	         return phoneNum.replace(replaceTarget, String.valueOf(c));
	      }
	      return phoneNum;
	   }
	
	public static String maskingEmail(String email) {
		/*
	      * 요구되는 메일 포맷
	      * {userId}@domain.com
	      * */
	      String regex = "\\b(\\S+)+@(\\S+.\\S+)";
	      Matcher matcher = Pattern.compile(regex).matcher(email);
	      if (matcher.find()) {
	         String id = matcher.group(1); // 마스킹 처리할 부분인 userId
	         /*
	         * userId의 길이를 기준으로 세글자 초과인 경우 뒤 세자리를 마스킹 처리하고,
	         * 세글자인 경우 뒤 두글자만 마스킹,
	         * 세글자 미만인 경우 모두 마스킹 처리
	         */
	         int length = id.length();
	         if (length < 3) {
	            char[] c = new char[length];
	            Arrays.fill(c, '*');
	            return email.replace(id, String.valueOf(c));
	         } else if (length == 3) {
	            return email.replaceAll("\\b(\\S+)[^@][^@]+@(\\S+)", "$1**@$2");
	         } else {
	            return email.replaceAll("\\b(\\S+)[^@][^@][^@]+@(\\S+)", "$1***@$2");
	         }
	      }
	      return email;
	   }
	
	/**
	 * 랜덤 코드 생성
	 * @param source 랜덤 코드 생성 대상 문자열
	 * @param length 랜덤 코드 자리수
	 * @return
	 */
	public static String getRandomCode(String source, int length) {
		String code = "";
		
		Random rnd = new Random();
		
		for (int i = 0; i < length; i++) {
			int rIdx = rnd.nextInt(source.length());
			code += source.substring(rIdx, rIdx + 1);
		}
		
		return code;
	}
	
	/**
	 * [임시] 문자열의 이모지를 제거함
	 * @param str
	 * @return
	 */
	public static String removeEmoji(String str) {
		/* for (char c : str.toCharArray()) {
			byte b = (byte) c;
			System.out.println(c + " : " + b);
		} */
		
		/*for (int i = 0; i < str.length(); i++) {
			String s = str.substring(i, i + 1);
			System.out.println(s);
		}
		
		for (byte b : str.getBytes()) {
			System.out.println(b);
		}*/		
		
		// UTF-8 기본 1바이트 문자들 + 한글 + 한자 범위
		return str.replaceAll("[^\\u0000-\\u007f\\u3131-\\u314e\\u314f-\\u3163\\uac00-\\ud7a3\\u4e00-\\u9fd5]", "");
	}
	
	public static boolean isPhoneNum(String phoneNum) {
		boolean result = false;
		phoneNum = phoneNum.replaceAll("[^0-9]", "");
		
		if (phoneNum.length() == 10 || phoneNum.length() == 11) {
			if ("01".equals(phoneNum.substring(0,2))){
				result = true;
			}
		}
		return result;
	}

	/**
	 * 전화번호 번호만 남기고 나머지는 없앰 
	 * @param phoneNoStr
	 * @return
	 */
	public static String makePhoneOnlyNumber(String phoneNoStr) {
		return phoneNoStr.replaceAll("[^0-9]", "");
	}	
	 
	/** * Comment : 정상적인 이메일 인지 검증. */
	public static boolean isValidEmail(String email) {
		 boolean err = false; 
		 String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
		 Pattern p = Pattern.compile(regex);
		 Matcher m = p.matcher(email);
		 if(m.matches()) {
			 err = true; 
			 }
		 return err; 
	}

	/** 발신번호 검증 **/
	public static boolean validationTelNumber( String number ) {
		number.replaceAll("/[^0-9]/g", "");
		int length = number.length();

		String[] nationwideNumber = new String[]{ "1566", "1600", "1670", "1577", "1588", "1899", "1522", "1544", "1644", "1661", "1599", "1688", "1666", "1855", "1811", "1877" };
		String[] regionNumber = new String[]{ "02", "051", "053", "032", "062", "042", "052", "044", "031", "033", "043", "041", "063", "061", "054", "055", "064" };

		if( length < 8 || length > 12 ){
			return  false;
		}else if( length == 8 ){	// 전국 대표 번호가 아닐경우
			if( !isIn( number.substring( 0, 4 ), nationwideNumber ) ){
				return false;
			}
		}else if( length == 9 ) { // 지역번호가 아니거나 중간 번호가 0이나 1로 시작할 경우
			if ( number.substring(0, 2) != "02" || isIn(number.substring(2, 1), "0", "1") ) {
				return false;
			}
		}else if( length == 10 ) {  // 지역번호가 아니거나 중간 번호가 0이나 1로 시작할 경우
			if ( ( number.substring(0, 2) != "02" && !isIn( number.substring(0, 3) , regionNumber ) ) || isIn(number.substring(2, 1), "0", "1")) {
				return false;
			}
		}else if( length == 11 || length == 12 ){ // 휴대폰 번호

		}

		return true;
	}

	/**
	 * 1번째 인자가 다음에 포함되는지 검증
	 */
	public static boolean isIn( String val, String... s ){ if( val == null ){ return false; } for( String str : s ){ if( val.equals( str ) ){ return true; } } return false; }


}