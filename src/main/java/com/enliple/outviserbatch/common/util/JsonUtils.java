package com.enliple.outviserbatch.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.JdbcUtils;

import com.enliple.outviserbatch.common.data.DataMap;

public class JsonUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
	
	/**
	 * Map을 JSONObject로 변환
	 * @param map
	 * @return
	 */
	public static JSONObject toJSONObject(Map<String, ?> map) {
		return new JSONObject(map);
	}
	
	/**
	 * Map을 Json String으로 변환
	 * @param map
	 * @return
	 */
	public static String toString(Map<String, ?> map) {
		return toJSONObject(map).toString();
	}
	
	/**
	 * DataMap을 Json String으로 변환
	 * @param dataMap
	 * @return
	 */
	public static String toString(DataMap dataMap) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		for (Object key : dataMap.keySet()) {
			String strKey = key.toString();
			if ("deviceType|userAgent|connectIp".indexOf(strKey) == -1) {	// RequestUtils에서 삽입하는 값들은 제외
				map.put(strKey, dataMap.get(key));
			}
		}
		
		return toJSONObject(map).toString();
	}
	
	/**
	 * List<List<Object>>를 Json String으로 변환
	 * @param map
	 * @return
	 */
	public static String toString(List<List<Object>> list) {
		JSONArray jsonArr = new JSONArray();
		
		for (List<Object> objList : list) {
			JSONArray jsonObj = new JSONArray();
			for (Object obj : objList) {
				jsonObj.put(obj);
			}
			jsonArr.put(jsonObj);
		}
		
		return jsonArr.toString();
	}
	
	/**
	 * Json String을 JSONObject로 변환
	 * @param jsonStr
	 * @return
	 * @throws JSONException
	 */
	public static JSONObject toJSONObject(String jsonStr) throws JSONException {
		return new JSONObject(jsonStr);
	}
	
	/**
	 * JSONObject를 Map으로 변환
	 * @param object
	 * @return
	 * @throws JSONException
	 */
	public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();
        Iterator<?> keys = object.keys();
        
        while (keys.hasNext()) {
            String key = (String)keys.next();
            Object value = object.get(key);
            
            if (value instanceof Map) {
            	map.put(key, toMap((JSONObject)value));
            } else {
            	map.put(key, value);
            }
        }
        
        jsonObjValueNullCheck(map);
        return map;
    }
	
	/**
	 * JSONObject를 DataMap으로 변환
	 * @param object
	 * @return
	 * @throws JSONException
	 */
	public static DataMap toDataMap(JSONObject object) throws JSONException {
		DataMap map = new DataMap();
		Iterator<?> keys = object.keys();
		
		while (keys.hasNext()) {
			String key = (String)keys.next();
			Object value = object.get(key);
			
			if (value instanceof Map) {
				map.put(key, toMap((JSONObject)value));
			} else {
				map.put(key, value);
			}
		}
		
		jsonObjValueNullCheck(map);
		return map;
	}
	
	/**
	 * Json String을 DataMap으로 변환
	 * @param jsonStr
	 * @return
	 * @throws JSONException
	 */
	public static DataMap toDataMap(String jsonStr) throws JSONException {
		return toDataMap(new JSONObject(jsonStr));
	}
	
	/**
	 * Json String을 Map으로 변환
	 * @param jsonStr
	 * @return
	 * @throws JSONException
	 */
	public static Map<String, Object> toMap(String jsonStr) throws JSONException {
		return toMap(new JSONObject(jsonStr));
	}
	
	/**
	 * Json Array String을 List<Map<String, Object>>으로 변환
	 * @param jsonArrStr
	 * @return
	 * @throws JSONException
	 */
	public static List<Map<String, Object>> toArrayMap(String jsonArrStr) throws JSONException {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		JSONArray jsonArr = new JSONArray(jsonArrStr);
		for (int i = 0; i < jsonArr.length(); i++) {
			JSONObject jsonObj = jsonArr.getJSONObject(i);
			list.add(toMap(jsonObj));
		}
		
		return list;
	}
	
	/**
	 * Json Array String을 List<DataMap>으로 변환
	 * @param jsonArrStr
	 * @return
	 * @throws JSONException
	 */
	public static List<DataMap> toListDataMap(String jsonArrStr) throws JSONException {
		List<DataMap> list = new ArrayList<DataMap>();
		
		JSONArray jsonArr = new JSONArray(jsonArrStr);
		for (int i = 0; i < jsonArr.length(); i++) {
			JSONObject jsonObj = jsonArr.getJSONObject(i);
			list.add(toDataMap(jsonObj));
		}
		
		return list;
	}
	
	/**
	 * Json Array String을 DataMap으로 변환
	 * @param jsonArrStr
	 * @return
	 * @throws JSONException
	 */
	public static List<DataMap> toArrayDataMap(String jsonArrStr) throws JSONException {
		List<DataMap> list = new ArrayList<DataMap>();
		
		JSONArray jsonArr = new JSONArray(jsonArrStr);
		for (int i = 0; i < jsonArr.length(); i++) {
			JSONObject jsonObj = jsonArr.getJSONObject(i);
			list.add(toDataMap(jsonObj));
		}
		
		return list;
	}
	
	/**
	 * List<Map<String, Object>> 구조를 Json Array String으로 변환 (Key는 Camel case로 변환)
	 * @param listMap
	 * @return
	 * @throws JSONException
	 */
	public static String toArrayString(List<Map<String,Object>> listMap) {
		return toArrayString(listMap, true);
	}
	
	/**
	 * List<Map<String, Object>> 구조를 Json Array String으로 변환
	 * @param listMap
	 * @param isCamelCaseKey 키가 underscores면 Camel case로 변환할 것인가? (false : 현재 상태 유지)
	 * @return
	 * @throws JSONException
	 */
	public static String toArrayString(List<Map<String,Object>> listMap, boolean isCamelCaseKey) {
		JSONArray jsonArr = new JSONArray();

		try {
			for (Map<String, Object> dm : listMap) {

				if (isCamelCaseKey) {
					Map<String, Object> newMap = new HashMap<String, Object>();

					for (Object key : dm.keySet()) {
						String strKey = key.toString();

						if (strKey.indexOf("_") > -1 || StringUtils.isAllUpperCase(strKey)) {
							strKey = JdbcUtils.convertUnderscoreNameToPropertyName(strKey);
						}

						newMap.put(strKey, dm.get(key));
					}

					jsonArr.put(newMap);
				} else {
					jsonArr.put(dm);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return jsonArr.toString();
	}

	/**
	 * Map 객체내 값이 NULL 이 존재하는 경우 공백으로 치환
	 *  > mybatis 파라미터 넘길때 NULL 값이 존재하는 경우 에러발생을 방지하기 위함
	 * @param dataMap
	 */
	public static void jsonObjValueNullCheck(Map<?, Object> map) {

		if (map instanceof Map) {
			try {
				for (Entry<?, Object> e : map.entrySet()) {
					if (e.getValue() == JSONObject.NULL) {
						e.setValue("");
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

}