package dev.ioexception.community.log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* dev.ioexception.community.controller.ArticleController.searchArticle(..))")
    public void searchArticle() { }

    @Around("searchArticle()")
    public Object logSearchArticle(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String queryField = (String) args[0];
        String queryKeyword = (String) args[1];

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;

        String logMessage = String.format(
                "ES_METRIC query.requests=%s, query.question=%s, query.execution.time=%d, timestamp=%d",
                queryField, queryKeyword, executionTime, System.currentTimeMillis()
        );

        logger.info(logMessage);

        return result;
    }
}