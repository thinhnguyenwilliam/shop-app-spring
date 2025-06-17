package com.example.shopapp.components.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.logging.Logger;

@Component
@Aspect
public class UserActivityLogger {

    private static final Logger logger = Logger.getLogger(UserActivityLogger.class.getName());

    // Named pointcut targeting all classes annotated with @RestController
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

    // Intercept UserController methods within a REST controller
    @Around("controllerMethods() && execution(* com.example.shopapp.controllers.UserController.*(..))")
    public Object logUserActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        // Get client IP address
        String remoteAddress = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes())
                .getRequest()
                .getRemoteAddr();

        // Log before execution
        if (logger.isLoggable(java.util.logging.Level.INFO)) {
            logger.info(String.format("🔍 User activity started: %s, IP address: %s", methodName, remoteAddress));
        }

        // Proceed with the actual method
        Object result = joinPoint.proceed();

        // Log after execution
        if (logger.isLoggable(java.util.logging.Level.INFO)) {
            logger.info(String.format("✅ User activity finished: %s", methodName));
        }


        return result;
    }
}
