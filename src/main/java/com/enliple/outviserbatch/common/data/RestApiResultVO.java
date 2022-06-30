package com.enliple.outviserbatch.common.data;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class RestApiResultVO {
	HttpStatus httpStatus;
	HttpHeaders httpHeaders;
	String body;
	String msg;
	
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}
	public HttpHeaders getHttpHeaders() {
		return httpHeaders;
	}
	public void setHttpHeaders(HttpHeaders httpHeaders) {
		this.httpHeaders = httpHeaders;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String string) {
		this.body = string;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
}