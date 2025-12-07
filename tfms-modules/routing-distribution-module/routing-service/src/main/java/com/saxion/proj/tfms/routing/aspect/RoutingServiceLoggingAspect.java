package com.saxion.proj.tfms.routing.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Arrays;

/**
 * Cross-cutting concern for method-level logging.
 * Reduces boilerplate logging code throughout the service layer.
 */
@Aspect
@Component
public class RoutingServiceLoggingAspect {
    
    private static final Logger log = LoggerFactory.getLogger(RoutingServiceLoggingAspect.class);

    @Around("@annotation(com.saxion.proj.tfms.routing.annotation.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        try {
            log.info("{}::{} - Start with args: {}", className, methodName, Arrays.toString(joinPoint.getArgs()));
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("{}::{} - Completed in {}ms", className, methodName, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("{}::{} - Failed after {}ms: {}", className, methodName, duration, e.getMessage(), e);
            throw e;
        }
    }
}
