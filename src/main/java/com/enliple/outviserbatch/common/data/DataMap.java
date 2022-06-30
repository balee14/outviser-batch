package com.enliple.outviserbatch.common.data;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.enliple.outviserbatch.common.statics.StaticCode;
import com.enliple.outviserbatch.common.util.JsonUtils;
import com.oreilly.servlet.MultipartRequest;

@SuppressWarnings("rawtypes")
public class DataMap extends HashMap<Object, Object> {

    private static final long serialVersionUID = 1568324898795L;
    
    public DataMap(){
    	super();
    }

	public DataMap(Map arg0){
    	super();
    	this.addAll(arg0);
    }
	
	/**
	 * camelCase로 요청해도 Underscores로 변환해서 조회해봄
	 */
	@Override
	public boolean containsKey(Object key) {
		boolean b = super.containsKey(key);
		
		if (b == false) {
			String usKey = this.convertCamelCaseToUnderscores(key);			
			b = (super.containsKey(usKey) || super.containsKey(usKey.toLowerCase()));
		}
		
		return b;		
	}
	
	/**
	 * n개의 object key 를 조회하여 1개라도 없는 경우 false
	 * @param keys
	 * @return
	 */
	public boolean containsKey(Object... keys) {

		for (Object key : keys) {
			if (!this.containsKey(key)) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * camelCase로 요청해도 Underscores로 변환해서 가져와봄
	 */
	@Override
	public Object get(Object key) {
		Object returnObj = null;
		
		if (super.containsKey(key)) {
			returnObj = super.get(key);
		} else {
			String usKey = this.convertCamelCaseToUnderscores(key);
			
			if (super.containsKey(usKey)) {
				returnObj = super.get(usKey);
			} else if (super.containsKey(usKey.toLowerCase())) {
				returnObj = super.get(usKey.toLowerCase());
			}
		} 
		
		return returnObj;
	}
	
	/**
	 * Camel case를 Underscores(대문자)로 변환
	 * @param key
	 * @return
	 */
	public String convertCamelCaseToUnderscores(Object key) {
		String str = key.toString();
		
        String regex = "([a-z0-9])([A-Z]+)";  
        String replacement = "$1_$2";  
  
        str = str.replaceAll(regex, replacement); 
  
        return str.toUpperCase();
	}
	
    /**
     * HTTP Status Code
     */
    public enum HttpStatus {
    	OK(200, "성공")
    	, BAD_REQUEST(400, "파라미터 오류")
    	, UNAUTHORIZED(401, "미인증")
    	, FORBIDDEN(403, "해당 권한 없음")
    	, METHOD_NOT_ALLOWED(405, "미지원 Method")
    	, REQUEST_TIMEOUT(408, "응답 시간 초과")
    	, INTERNAL_SERVER_ERROR(500, "기타 오류");

    	private int code;
    	private String msg;
    	
		HttpStatus(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}
		
		public int getCode() {return code;}
		public String getMsg() {return msg;}
    } 
    
    private HttpStatus httpStatus;
    
    public void setHttpStatus(HttpStatus httpStatus) {
    	this.httpStatus = httpStatus;

    	if (httpStatus != HttpStatus.OK) {
    		this.put("msg", httpStatus.getMsg());
    	}
    }    
    public void setHttpStatus(HttpStatus httpStatus, String msg) {
    	this.httpStatus = httpStatus;
    	this.put("msg", msg);
    }
    public void setHttpStatus(org.springframework.http.HttpStatus orgHttpStatus) {
    	if (orgHttpStatus.value() == 200) {
    		this.setHttpStatus(HttpStatus.OK);
    	} else if (orgHttpStatus.value() == 400) {
    		this.setHttpStatus(HttpStatus.BAD_REQUEST);
    	} else if (orgHttpStatus.value() == 401) {
    		this.setHttpStatus(HttpStatus.UNAUTHORIZED);
    	} else if (orgHttpStatus.value() == 403) {
    		this.setHttpStatus(HttpStatus.FORBIDDEN);
    	} else if (orgHttpStatus.value() == 405) {
    		this.setHttpStatus(HttpStatus.METHOD_NOT_ALLOWED);
    	} else if (orgHttpStatus.value() == 408) {
    		this.setHttpStatus(HttpStatus.REQUEST_TIMEOUT);
    	} else {
    		this.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    	}
    }
    
    public HttpStatus getHttpStatus() {
    	return this.httpStatus;
    }
    
    
    private String contentType;
    
    public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public void setHttpStatusAndResult(HttpStatus httpStatus, String resultCode, String resultMsg) {
		this.httpStatus = httpStatus;
		this.put("rsCode", resultCode);
    	this.put("rsMsg", resultMsg);
	}
	
    public void setHttpStatusAndResult(HttpStatus httpStatus, String resultCode) {
    	this.setHttpStatusAndResult(httpStatus, resultCode, StaticCode.getCodeName("DATAMAP_RSMSG", resultCode));
    }
    
    public void setHttpStatusAndResult(HttpStatus httpStatus, Exception e) {
    	this.setHttpStatusAndResult(httpStatus, "E0000", e.toString());
    }
    
	public void setHttpStatusAndResult(Exception e) {
    	this.setHttpStatusAndResult(httpStatus.INTERNAL_SERVER_ERROR, "E0000", e.toString());
    }
	
    
    /**
     * 유효하지 않은 값인지 체크 (String)
     * @param key
     * @param minLen 최소 길이
     * @param maxLen 최대 길이
     * @return
     */
    public boolean isInvalid(String key, int minLen, int maxLen) {
    	boolean b = true;
    	
    	if (this.containsKey(key) == false
    			|| this.get(key) == null
    			|| "".equals(this.getString(key).trim())) {
    		this.put("msg", key + "는(은) 필수 파라미터 입니다.");
    	} else if (0 < minLen
    			&& this.getString(key).trim().length() < minLen) {
    		this.put("msg", key + "는(은) " + minLen + "자 이상 입력해야 합니다.");
    	} else if (0 < maxLen
    			&& maxLen < this.getString(key).trim().length()) {
    		this.put("msg", key + "는(은) " + maxLen + "자 이하로 입력해야 합니다.");
    	} else {
    		b = false;
    	}
    	
    	return b;
    }
    
    /**
     * 유효하지 않은 값인지 체크 (String)
     * @param key
     * @return
     */
    public boolean isInvalid(String key) {
    	return this.isInvalid(key, 0, 0);
    }
    
    /**
     * 유효하지 않은 값인지 체크 (String)
     * @param key
     * @param maxLen
     * @return
     */
    public boolean isInvalid(String key, int len) {
    	boolean b = true;
    	
    	if (this.containsKey(key) == false
    			|| this.get(key) == null
    			|| "".equals(this.getString(key).trim())) {
    		this.put("msg", key + "는(은) 필수 파라미터 입니다.");
    	} else if (this.getString(key).trim().length() != len) {
    		this.put("msg", key + "는(은) " + len + "자로 입력해야 합니다.");
    	} else {
    		b = false;
    	}
    	
    	return b;
    }
    
    /**
     * 유효하지 않은 값인지 체크 (int)
     * @param key
     * @param min 최소 크기
     * @param max 최대 크기
     * @return
     */
    public boolean isInvalidDigit(String key, int min, int max) {
    	boolean b = true;
    	
    	if (this.containsKey(key) == false
    			|| this.get(key) == null
    			|| "".equals(this.getString(key).trim())) {
    		this.put("msg", key + "는(은) 필수 파라미터 입니다.");
    	} else if (this.isDigit(key) == false) {
    		this.put("msg", key + "는(은) 숫자를 입력하세요.");
    	} else if (0 != min
    			&& this.getInt(key) < min) {
    		this.put("msg", key + "는(은) " + min + "보다 커야 합니다.");
    	} else if (0 != max
    			&& max < this.getInt(key)) {
    		this.put("msg", key + "는(은) " + max + "보다 작아야 합니다.");
    	} else {
    		b = false;
    	}
    	
    	return b;
    }
    
    /**
     * 유효하지 않은 값인지 체크 (int)
     * @param key
     * @param max
     * @return
     */
    public boolean isInvalidDigit(String key, int max) {
    	return isInvalidDigit(key, 0, max);
    }
    
    /**
     * 유효하지 않은 값인지 체크 (int)
     * @param key
     * @return
     */
    public boolean isInvalidDigit(String key) {
    	return isInvalidDigit(key, 0, 0);
    }
    
    /**
     * 숫자인가?
     * @param key
     * @return
     */
    public boolean isDigit(String key) {
    	String v = this.get(key).toString();
    	return StringUtils.isNumeric(v);
    }
    
    /**
	 * Map 병합
	 * @param map
	 */
	@SuppressWarnings("unchecked")
	public void addAll(Map map){

		Iterator i$ = map.entrySet().iterator();
		do {
			if (!i$.hasNext()){
				break;
			}
			java.util.Map.Entry entry = (java.util.Map.Entry) i$.next();
			Object value = entry.getValue();
			
			if (value != null){
				Object toadd;
				// request.getParameterMap() 각 값은 String[]이다
				if (value instanceof String[]) {
					String values[] = (String[]) (String[]) value;
					if (values.length > 1) {
						toadd = new ArrayList(Arrays.asList(values));
					} else {
						toadd = values[0];
					}
				} else {
					toadd = value;
				}
				
				this.put(((String)entry.getKey()), toadd);
			}
		} while (true);
	}
	
	/*
    public String getSafeString(String key) {
        return getString(key); // TODO
    }
    
    public String getSafeString(String key, int byteLength) {
        return getString(key, byteLength); // TODO
    }*/
    
	/**
	 * 기본값 넣기
	 * @param key
	 * @param defaultValue
	 */
	public void putDefaultValue(String key, String defaultValue){
		put(key, getString(key, defaultValue));
    }
	
    public String getString(String key){
        return getString(key, "");
    }

    public String getString(String key, int byteLength){
        return getString(key, byteLength, "");
    }

    public String getString(String key, int byteLength, String nullValue){
        return byteCut(getString(key, nullValue), byteLength);
    }

    public int getInt(String key, int def){
		Object obj = this.get(key);
		int ret = def;
		if (obj instanceof java.lang.Number){
			ret = ((Number) obj).intValue();
		} else {
			try {
				ret = Integer.parseInt(obj.toString());
			} catch (Exception ex){
				ret = def;
			}
		}
		return ret;
	}
    
	public int getInt(String key){
		return getInt(key, 0);
	}

    public long getLong(String key){
        Long returnValue = getLongObject(key);

        if(returnValue == null) return 0;
        else return returnValue.longValue();
    }

    public float getFloat(String key){
        Float returnValue = getFloatObject(key);

        if(returnValue == null) return 0;
        else return returnValue.floatValue();
    }

    public double getDouble(String key){
        Double returnValue = getDoubleObject(key);

        if(returnValue == null) return 0;
        else return returnValue.doubleValue();
    }
    
    public String getString(String key, String nullValue){
        Object value = (Object)this.get(key);
        
        try{
        	if("".equals(value.toString())){
        		return nullValue;
        	}
            return value.toString();
        }
        catch(Exception e){
            return nullValue;
        }
    }
    
    public String[] getStringArray(String key){
    	String[] arr = null;
    	if(this.containsKey(key)) {
    		if(this.get(key) instanceof String){
    			arr = this.getString(key).split(",");
    		} else if(this.get(key) instanceof ArrayList){
    			ArrayList array = (ArrayList)this.get(key);
    			arr = Arrays.copyOf(array.toArray(), array.size(), String[].class);
    		}
    	}
    	return arr;
    }
    
    public Integer getIntObject(String key){
        Object value = (Object)this.get(key);
    
        try{
            return Integer.valueOf(value.toString(), 10);
        }
        catch(Exception e){
            return null;
        }
    }

    public Long getLongObject(String key){
        Object value = (Object)this.get(key);
        
        try{
            return Long.valueOf(value.toString(), 10);
        }
        catch(Exception e){
            return null;
        }
    }

    public Float getFloatObject(String key){
        Object value = (Object)this.get(key);
        
        try{
            return Float.valueOf(value.toString());
        }
        catch(Exception e){
            return null;
        }
    }

    public Double getDoubleObject(String key){
        Object value = (Object)this.get(key);
        
        try{
            return Double.valueOf(value.toString());
        }
        catch(Exception e){
            return null;
        }
    }
    
    public boolean getBoolean(String key){
    	boolean b = false;
    	
    	try {
    		String s = this.getString(key);
    		b = Boolean.valueOf(s);
    	} catch(Exception e){
    	}
    	
    	return b;
    }
    
    public DataMap getDataMap(String key){
        Object value = (Object)this.get(key);
        
        try{
        	if (value instanceof JSONObject) {
        		return JsonUtils.toDataMap((JSONObject) value);
        	} else {
        		return (DataMap) value;
        	}
        }
        catch(Exception e){
            return null;
        }
    }

	public List<DataMap> getArrayDataMap(String key) {
		List<DataMap> result = null;

		try {
			Object value = (Object) this.get(key);

			if (value instanceof List) {
				result = (List) value;
			} else {
				result = JsonUtils.toArrayDataMap(value.toString());
			}
		} catch (Exception e) {}

		if (result == null) {
			result = new ArrayList<>();
		}

		return result;
	}

    public String getStringHtml(String key) {
    	return StringEscapeUtils.unescapeHtml4(this.getString(key));
    }
    
    public void unescapeHtmlValue(String... keys) {
    	for (String key : keys) {
    		this.put(key, this.getStringHtml(key));
    	}
    }

    public static String byteCut(String str, int bytelen){
        int i, lenCount=0;
        int strLen=str.length();
        
        for(i=0; i<strLen && lenCount<=bytelen ;i++){
            try{
                lenCount+=String.valueOf(str.charAt(i)).getBytes("UTF-8").length;
            }
            catch(UnsupportedEncodingException e){
                return str.substring(0, bytelen);
            }
        }
        
        if(lenCount>bytelen)
            return str.substring(0,i-1);
        else
            return str;
    }
    
    
    /**
     * 멀티파티 맵에 담기
     * @param multi
     */
    @SuppressWarnings("unchecked")
	public DataMap(MultipartRequest multi){
    	super();
		Enumeration keys = multi.getParameterNames();
		while(keys.hasMoreElements()){
			String key = (String)keys.nextElement();
			String[] val = multi.getParameterValues(key);
			
			if(val == null)
				this.put(key, val);
			else if(val.length == 1)
				this.put(key, val[0]);
			else
				this.put(key, new ArrayList(Arrays.asList(val)));
		}
    }
    
    /**
     * Json String 값을 파싱하여 put 함.
     * @param jsonStr
     */
    public void putJsonString(String jsonStr){
    	try {
			Map<String, Object> map = JsonUtils.toMap(jsonStr);
			
			for (String key : map.keySet()) {
				this.put(key, map.get(key));
			}
		} catch (JSONException e) {
		}
    }
    
}