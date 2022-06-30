package com.enliple.outviserbatch.common.exception;

import java.util.Arrays;

import lombok.Getter;

@SuppressWarnings("serial")
@Getter
public class CommonException extends RuntimeException {

	private Object parameter;

	public CommonException() {
		super();
	}

	public CommonException(String message) {

		super(message);
	}

	public CommonException(Throwable cause) {

		super(cause);
	}

	public CommonException(Object parameter) {

		this();
		this.setParameter(parameter);
	}

	public CommonException(String message, Throwable cause) {

		super(message, cause);
	}

	public CommonException(String message, Object parameter) {

		this(message);
		this.setParameter(parameter);
	}

	public CommonException(String message, Object parameter, Throwable cause) {

		this(message, cause);
		this.setParameter(parameter);
	}

	public CommonException(Object parameter, Throwable cause) {

		this(cause);
		this.setParameter(parameter);
	}

	/**
	 * 
	 * @param parameter
	 */
	private void setParameter(Object parameter) {

		// Object type is array
		if (parameter.getClass().isArray()) {
			parameter = Arrays.toString((Object[]) parameter);
		}

		this.parameter = parameter;
	}
}
