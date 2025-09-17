package com.renewsim.backend.shared.observability;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UseCaseLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(UseCaseLoggingAspect.class);

    @Around("execution(* com.renewsim.backend.user_service.application.service..*(..))")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            log.debug("➡️ Entering {}", pjp.getSignature());
            Object result = pjp.proceed();
            log.debug("⬅️ Exiting {} with result={}", pjp.getSignature(), result);
            return result;
        } finally {
            long ms = System.currentTimeMillis() - start;
            log.info("UseCase {} ejecutado en {} ms traceId={}",
                    pjp.getSignature(),
                    ms,
                    MDC.get("traceId"));
        }
    }
}
