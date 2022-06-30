package com.enliple.outviserbatch.common.bind.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CommondMap {

	/*
	 * 해당 옵션 사용안함
	boolean isPage() default false;
	int pageSize() default 0;
	 */
}