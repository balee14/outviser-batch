package com.enliple.outviserbatch.common.aop;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggerAspect {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Around("execution(* com.enliple.outviserbatch..*Controller.*(..)) or "
            + "execution(* com.enliple.outviserbatch..*JobLauncher.*(..)) or "
            + "execution(* com.enliple.outviserbatch..*BatchJob.*(..)) or "
            + "execution(* com.enliple.outviserbatch..*BatcTasklet.*(..)) or "
            + "execution(* com.enliple.outviserbatch..*Service.*(..)) or "
            + "execution(* com.enliple.outviserbatch..*Mapper.*(..))")
    public Object printLog(ProceedingJoinPoint joinPoint) throws Throwable {
        
        String type = "";
        String name = joinPoint.getSignature().getDeclaringTypeName();
        
        if (name.contains("Controller") == true) {
            type = "Controller ===> ";
        } else if (name.contains("JobLauncher") == true) {
            type = "JobLauncher ===> ";
        } else if (name.contains("BatchJob") == true) {
            type = "BatchJob ===> ";
        } else if (name.contains("BatcTasklet") == true) {
            type = "BatcTasklet ===> ";
        } else if (name.contains("Service") == true) {
            type = "Service ===> ";
        } else if (name.contains("Mapper") == true) {
            type = "Mapper ===> ";
        }
        
        logger.info("aop ==> " + type + name + "." + joinPoint.getSignature().getName() + "()");
        return joinPoint.proceed();
        
    }

}
