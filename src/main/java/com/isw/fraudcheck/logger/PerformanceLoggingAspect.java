package com.isw.fraudcheck.logger;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class PerformanceLoggingAspect {

    @Around("execution(public * com.isw.fraudcheck.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String className  = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();

        Object[] maskedArgs = LogUtil.maskArgs(paramNames, joinPoint.getArgs());

        log.info("[START] {}.{}() | args: {}", className, methodName, maskedArgs);

        long startNs = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            double durationMs = (System.nanoTime() - startNs) / 1_000_000.0;

            log.info("[END] {}.{}() | duration: {} ms | result: {}",
                    className, methodName,
                    String.format("%.3f", durationMs), result);

            return result;

        } catch (Throwable ex) {
            double durationMs = (System.nanoTime() - startNs) / 1_000_000.0;

            log.error("[ERROR] {}.{}() | duration: {} ms | exception: {} | message: {}",
                    className, methodName,
                    String.format("%.3f", durationMs),
                    ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }
}