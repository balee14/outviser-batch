package com.enliple.outviserbatch.common.util;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.data.RestApiResultVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RestApiUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(RestApiUtils.class);
	
	private static MediaType getContentType(String contentType) {
		if ("JSON".equals(contentType)) {
			return MediaType.APPLICATION_JSON;
		} else if ("MULTI".equals(contentType)) {
			return MediaType.MULTIPART_FORM_DATA;
		} else {
			return MediaType.APPLICATION_FORM_URLENCODED;
		}
	}
	
	/**
	 * Rest API 호출
	 * @param url
	 * @param httpMethod GET, POST, PUT, DELETE 중 입력
	 * @param contentType 파라미터 전달 방식 (application/x-www-form-urlencoded이면 FORM, multipart/form-data면 MULTI, application/json이면 JSON 입력)
	 * @param requestJson 파라미터 (json 형태의 String) 
	 * @param paramHeader 요청 헤더에 값을 넣어야 하는 경우 사용
	 * @return
	 */
	public static RestApiResultVO callRestApi(String url, String httpMethod, String contentType, String requestJson, Map<String, String> paramHeader) {
		RestApiResultVO result = new RestApiResultVO();
		
		logger.debug("[" + httpMethod + "] " + url + " (" + getContentType(contentType).toString() + ")");
		logger.debug("param : " + requestJson);
		
		try	{
			RestTemplate restTemplate = new RestTemplate();
            
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setConnectTimeout( 60 * 1000 );	//타임아웃 설정 10초 -> 60초 20220117 타임아웃 시간 늘림
            factory.setReadTimeout( 60 * 1000 );		//타임아웃 설정 10초 -> 60초 20220117 타임아웃 시간 늘림
            restTemplate.setRequestFactory(factory);
			
            /*
			// 한글 처리 ver 1
            restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
            
            // 한글 처리 ver 2
            // json 파싱 오류 발생으로 폐기
            
            // 한글 처리 ver 3
            for(HttpMessageConverter<?> messageConverter : restTemplate.getMessageConverters()) {
            	if(messageConverter instanceof AllEncompassingFormHttpMessageConverter) {
            		((AllEncompassingFormHttpMessageConverter) messageConverter).setCharset(Charset.forName("UTF-8"));
            	}
            }
            */
            
			// 한글처리 ver 4
            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
            formHttpMessageConverter.setCharset(Charset.forName("UTF-8"));

            List<HttpMessageConverter<?>> partConverters = new ArrayList<HttpMessageConverter<?>>();
            partConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
            partConverters.add(new ResourceHttpMessageConverter());
            partConverters.add(new ByteArrayHttpMessageConverter());

            formHttpMessageConverter.setPartConverters(partConverters);

            messageConverters.add(formHttpMessageConverter);
            messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));

            restTemplate.setMessageConverters(messageConverters);
            
            
            HttpHeaders header = new HttpHeaders();
            header.setContentType(getContentType(contentType));
            
            // 헤더 값이 있으면 추가
            if (paramHeader != null) {
            	for (String key : paramHeader.keySet()) {
            		header.add(key, paramHeader.get(key).toString());
            	}
        		logger.debug("header : " + JsonUtils.toString(paramHeader));
            }
            
            HttpEntity<?> entity;
            
            /*
             파라미터 처리 방식이 다 다르다;; ㅠ
             1. HttpMethod가 GET이면 querystring 형식으로 or GET이 아니어도 param이 없으면
             2. HttpMethod가 GET이 아니면서, ContentType이 application/x-www-form-urlencoded 또는 multipart/form-data이면 MultiValueMap 형식으로
             3. HttpMethod가 GET이 아니면서, ContentType이 application/json이면 Json String 그대로
             */
            if ("GET".equals(httpMethod) || StringUtils.isBlank(requestJson)) {
            	url += toQueryString(requestJson);
            	entity = new HttpEntity<>(header);
            }
            else if ("FORM".equals(contentType) || "MULTI".equals(contentType)) {
        		MultiValueMap <String, String> param = toMultiValueMap(requestJson);
        		entity = new HttpEntity<>(param, header);
            }
            else {
            	entity = new HttpEntity<>(requestJson, header);
            }
            
            logger.debug("reqFullUrl : " + url);
            ResponseEntity<String> resultMap = restTemplate.exchange(url, HttpMethod.valueOf(httpMethod), entity, String.class);
            result.setHttpStatus(resultMap.getStatusCode());	// http status code
            result.setHttpHeaders(resultMap.getHeaders());		// header
            result.setBody(resultMap.getBody());				// body
		}
		catch (HttpClientErrorException he) {
			result.setHttpStatus(he.getStatusCode());
			result.setBody(he.getResponseBodyAsString());
		}
		catch (RestClientException rce) {
			if(rce.getRootCause() instanceof SocketTimeoutException
					|| rce.getRootCause() instanceof ConnectTimeoutException){
				result.setHttpStatus(HttpStatus.REQUEST_TIMEOUT);	// 타임아웃 발생시 예외 처리
			} else {
				result.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			}
			result.setMsg(rce.getMessage());
			rce.printStackTrace();
		}
		catch (Exception e) {
			result.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			result.setMsg(e.getMessage());
			e.printStackTrace();
		}
		finally {
			logger.debug("result : [" + result.getHttpStatus().value() + "] " + result.getBody());
		}
		
		return result;
	}
	
	public static RestApiResultVO callRestApi(String url, String httpMethod, String contentType, String requestJson) {
		return callRestApi(url, httpMethod, contentType, requestJson, null);
	}
	
	public static RestApiResultVO callRestApi(String url, String httpMethod, String contentType, DataMap param, Map<String, String> paramHeader) {
		String requestJson = JsonUtils.toString(param);
		return callRestApi(url, httpMethod, contentType, requestJson, paramHeader);
	}
	
	public static RestApiResultVO callRestApi(String url, String httpMethod, String contentType, DataMap param) {
		return callRestApi(url, httpMethod, contentType, param, null);
	}
	
	/**
	 * Json String을 QueryString으로 변환
	 * @param map
	 * @return
	 */
	public static String toQueryString(String jsonStr) throws JSONException {
		String queryString = "";
		
		if (StringUtils.isNotBlank(jsonStr)) {
			Map<String, Object> map = JsonUtils.toMap(jsonStr);
			for (String key : map.keySet()) {
				if ("".equals(queryString) == false) {
					queryString += "&";
				}
				
				String value = map.get(key).toString();
				
				try {
					queryString += key + "=" + URLEncoder.encode(value, "UTF-8");
				} catch (UnsupportedEncodingException e) {
				}
			}
		}
		
		if (StringUtils.isNotBlank(queryString)) {
			queryString = "?" + queryString;
		}
		
		return queryString;
	}
	
	/**
	 * Json String을 MultiValueMap으로 변환
	 * @param jsonStr
	 * @return
	 * @throws JSONException
	 */
	private static MultiValueMap <String, String> toMultiValueMap(String jsonStr) throws JSONException {
		MultiValueMap <String, String> map = new LinkedMultiValueMap<>();
		
		Map<String, Object> tempMap = JsonUtils.toMap(jsonStr);
		for (String key : tempMap.keySet()) {
			String value = tempMap.get(key).toString();
			map.add(key, value);
		}
		
		return map;
	}
	
}