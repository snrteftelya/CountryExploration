package org.example.aspect;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Aspect
@Component
public class CountrySearchAspect {
    private final Logger logger = LoggerFactory.getLogger(CountrySearchAspect.class);

    @Pointcut("execution(* org.example.controller.*.*(..))")
    public void callControllers() {
    }

    @Before("callControllers()")
    public void beforeCallMethod(final JoinPoint jp) {
        String args = Arrays.stream(jp.getArgs())
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(","));
        if (logger.isInfoEnabled()) {
            logger.info("Before {}, args={}", jp, args);
        }
    }

    @AfterReturning("callControllers()")
    public void afterReturningCallMethod(final JoinPoint jp) {
        if (logger.isInfoEnabled()) {
            logger.info("After returning {}", jp);
        }
    }

    @AfterThrowing(value = "callControllers()", throwing = "exception")
    public void afterThrowingCallMethod(final JoinPoint jp, final Exception exception) {
        if (logger.isErrorEnabled()) {
            logger.error("After throwing {}, exception: {}", jp, exception.getMessage());
        }
    }

    @PostConstruct
    public void initAspect() {
        if (logger.isInfoEnabled()) {
            logger.info("Aspect is initialized");
        }
    }

    @PreDestroy
    public void destroyAspect() {
        if (logger.isInfoEnabled()) {
            logger.info("Aspect is destroyed");
        }
    }

    private String getRequestUrl(JoinPoint jp) {
        try {

            Class<?> targetClass = jp.getTarget().getClass();
            String methodName = jp.getSignature().getName();
            Class<?>[] parameterTypes = ((org.aspectj.lang.reflect.MethodSignature)
                    jp.getSignature()).getParameterTypes();


            String basePath = "";
            RequestMapping classMapping = targetClass.getAnnotation(RequestMapping.class);
            if (classMapping != null && classMapping.value().length > 0) {
                basePath = normalizePath(classMapping.value()[0]);
            }


            Method targetMethod = targetClass.getDeclaredMethod(methodName, parameterTypes);
            if (targetMethod == null) {
                logger.warn("Could not find method {} in class {}",
                        methodName, targetClass.getName());
                return "/unknown";
            }


            String methodPath = getMethodPath(targetMethod);
            String fullPath;

            if (methodPath.isEmpty() && !basePath.isEmpty()) {
                fullPath = basePath;
            } else if (!methodPath.isEmpty()) {
                fullPath = basePath.isEmpty() ? methodPath :
                        basePath + methodPath;
            } else {
                fullPath = "/";
            }

            return fullPath;
        } catch (NoSuchMethodException e) {
            logger.error("Method not found for {}", jp, e);
            return "/unknown";
        } catch (Exception e) {
            logger.error("Error determining URL for {}", jp, e);
            return "/unknown";
        }
    }

    private String getMethodPath(Method method) {

        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null && requestMapping.value().length > 0) {
            return normalizePath(requestMapping.value()[0]);
        }

        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        if (getMapping != null) {
            return normalizePath(getMapping.value().length > 0 ? getMapping.value()[0] : "");
        }

        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        if (postMapping != null) {
            return normalizePath(postMapping.value().length > 0 ? postMapping.value()[0] : "");
        }

        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        if (putMapping != null) {
            return normalizePath(putMapping.value().length > 0 ? putMapping.value()[0] : "");
        }

        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        if (deleteMapping != null) {
            return normalizePath(deleteMapping.value().length > 0 ? deleteMapping.value()[0] : "");
        }

        return "";
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
}