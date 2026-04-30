package com.isw.fraudcheck.logger;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceLoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(PerformanceLoggingAspect.class);

    /**
     * Example: log execution time of all public methods in TransactionService
     */
    @Around("execution(public * com.isw.fraudcheck.service.FraudEngineService.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startNs = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long durationNs = System.nanoTime() - startNs;
            double durationMs = durationNs / 1_000_000.0;

            log.info("Method {}.{} took {} ms ({} ns)",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    String.format("%.3f", durationMs),
                    durationNs);
        }
    }
}
