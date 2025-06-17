package com.example.shopapp.components.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Aspect
@Component
public class PerformanceAspect {

    private static final Logger logger = Logger.getLogger(PerformanceAspect.class.getName());

    private String getMethodName(JoinPoint joinPoint) {
        return joinPoint.getSignature().getName();
    }

    // Pointcut for all methods in REST controllers
    @Pointcut("within(com.example.shopapp.controllers.*)")
    //@Pointcut("within(com.example.shopapp.controllers.CategoryController)")
    //@Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

    @Before("controllerMethods()")
    public void beforeMethodExecution(JoinPoint joinPoint) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info(String.format("⏳ Starting execution of %s", getMethodName(joinPoint)));
        }
    }

    @After("controllerMethods()")
    public void afterMethodExecution(JoinPoint joinPoint) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info(String.format("✅ Finished execution of %s", getMethodName(joinPoint)));
        }
    }

    @Around("controllerMethods()")
    public Object measureControllerMethodExecutionTime(ProceedingJoinPoint proceedingJoinPoint)
            throws Throwable {

        long start = System.nanoTime();
        Object returnValue = proceedingJoinPoint.proceed();
        long end = System.nanoTime();

        if (logger.isLoggable(Level.INFO)) {
            String methodName = proceedingJoinPoint.getSignature().getName();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(end - start);
            logger.info(String.format("⏱️ Execution of %s took %d ms", methodName, durationMs));
        }

        return returnValue;
    }
}
