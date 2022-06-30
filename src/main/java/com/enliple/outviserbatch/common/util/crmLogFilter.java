package com.enliple.outviserbatch.common.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class crmLogFilter extends Filter<ILoggingEvent> {
	
	@Override
	public FilterReply decide(ILoggingEvent event) {
		if (event.getMessage().contains("crm")) {
			return FilterReply.ACCEPT;
		} else {
			return FilterReply.DENY;
		}
	}

}
