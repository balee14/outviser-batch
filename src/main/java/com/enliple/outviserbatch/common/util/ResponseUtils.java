package com.enliple.outviserbatch.common.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enliple.outviserbatch.common.data.DataMap;

@SuppressWarnings("rawtypes")
public class ResponseUtils {

	private static final Logger logger = LoggerFactory.getLogger(ResponseUtils.class);
	
    public static void jsonString(HttpServletResponse response, String string) throws IOException {
    	response.setContentType("text/plain");
        response.setCharacterEncoding("utf-8");  
        response.getWriter().write(string);
    }
    
    public static void jsonBoolean(HttpServletResponse response, boolean bul) throws IOException {
        jsonString(response, bul?"true":"false");
    }
    
    public static void jsonMap(HttpServletResponse response, DataMap dataMap) throws Exception {
    	if (dataMap.getHttpStatus() != null) {
    		response.setStatus(dataMap.getHttpStatus().getCode());
    		
    		// rsMsg가 셋팅되어 있지 않으면, Http status code별 기본 메시지 적용
    		if (dataMap.containsKey("rsMsg") == false || "".equals(dataMap.getString("rsMsg"))) {
    			dataMap.put("rsMsg", dataMap.getHttpStatus().getMsg());
    		}
    		
			// json 리턴하는 경우 결과 로깅
    		if (dataMap.getString("rsCode").startsWith("S")) {    			
    			logger.debug("result : " + dataMap);
    		} else {
    			logger.warn("result : " + dataMap);
    		}
    	}
    	
        String data = (new JSONObject(dataMap)).toString();
        jsonString(response, data);
    }
    
    public static void jsonList(HttpServletResponse response, List<DataMap> list) throws Exception {
        String data = (new JSONArray(list.toArray())).toString();
        jsonString(response, data);
    }
    
    
    
    /// xml
    public static void xmlString(HttpServletResponse response, String string) throws IOException {
        response.setContentType("text/xml");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setContentLength(string.getBytes("utf-8").length);
        response.getWriter().write(string);
    }
    
    public static void xmlList(HttpServletResponse response, List<DataMap> list) throws Exception {
    	StringBuffer sb = new StringBuffer();
    	sb.append("<?xml version = '1.0' encoding = 'UTF-8'?>");
    	sb.append("<ROOT>");
    	writeFromList(sb, list);
    	sb.append("</ROOT>");
    	xmlString(response, sb.toString());
    }
    
    public static void xmlMap(HttpServletResponse response, DataMap dataMap) throws Exception {
    	
    	StringBuffer sb = new StringBuffer();
    	sb.append("<?xml version = '1.0' encoding = 'UTF-8'?>");
    	sb.append("<ROOT>");
    	writeFromMap(sb, dataMap);
    	sb.append("</ROOT>");
    	xmlString(response, sb.toString());
    }

    private static void writeFromMap(StringBuffer sb, Map map) {
    	 
        for(Object str : map.keySet()) {
            Object v = map.get(str);
            sb.append("<" + str + ">");
            if(v instanceof Map) {
                writeFromMap(sb, (Map) v);
            }else if(v instanceof List) {
                writeFromList(sb, (List) v);
            }else {
                writeFromData(sb, v);
            }
            sb.append("</" + str + ">");
        }
    }
 
    private static void writeFromList(StringBuffer sb, List list) {
 
        for(Object v : list) {
            sb.append("<item>");
            if(v instanceof Map) {
                writeFromMap(sb, (Map)v);
            }else if(v instanceof List) {
                writeFromList(sb, (List) v);
            }else {
                writeFromData(sb, v);
            }
            sb.append("</item>");
        }
    }
 
    private static void writeFromData(StringBuffer sb, Object data) {
        sb.append(escapeXml(data+""));
    }
 
    private static String escapeXml(String src) {
        src = src.replace("&", "&amp;");
        src = src.replace("<", "&lt;");
        src = src.replace(">", "&gt;");
        src = src.replace("\"", "&quot;");
        
        return src;
    }    
}