package com.enliple.outviserbatch.common.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CommonExceptionEntity {
    
    private String bodyTimestamp;
    private String bodyCode;
    private String bodyError;
    private String bodyMessage;
    
    @Builder
    public CommonExceptionEntity(String bodyTimestamp, String bodyCode, 
            String bodyError, String bodyMessage){
        this.bodyTimestamp = bodyTimestamp;
        this.bodyCode = bodyCode;
        this.bodyError = bodyError;
        this.bodyMessage = bodyMessage;
    }
    
}
