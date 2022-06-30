package com.enliple.outviserbatch.common.bind;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.enliple.outviserbatch.common.data.DataMap;
import com.enliple.outviserbatch.common.util.RequestUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AnnotationDataMapArgumentResolver implements HandlerMethodArgumentResolver{

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return DataMap.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

		DataMap dataMap = RequestUtils.getParamMap(request);
		dataMap.setContentType(request.getContentType());	// 현재 요청의 ContentType을 DataMap에 저장

//		log.debug("[" + request.getRemoteAddr() + "] [" + request.getMethod() + "] " + request.getRequestURL() + " (" + request.getContentType() + ")");
//		log.debug("param : " + dataMap);

		return dataMap; 
	}
}