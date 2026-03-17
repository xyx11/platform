package com.micro.platform.common.core.aspect;

import com.micro.platform.common.core.annotation.AsyncExecute;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.*;

/**
 * 异步执行切面
 * 用于拦截带有 @AsyncExecute 注解的方法，实现异步执行
 */
@Aspect
@Component
@Order(2)
@Slf4j
public class AsyncExecuteAspect {

    /**
     * 默认线程池
     */
    private final ExecutorService defaultExecutor = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r);
        thread.setName("async-executor-" + thread.getId());
        thread.setDaemon(true);
        return thread;
    });

    @Pointcut("@annotation(com.micro.platform.common.core.annotation.AsyncExecute)")
    public void asyncExecutePointcut() {
    }

    @Around("asyncExecutePointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Object[] args = point.getArgs();

        // 获取注解
        AsyncExecute asyncExecute = method.getAnnotation(AsyncExecute.class);
        if (asyncExecute == null) {
            return point.proceed();
        }

        // 如果是 void 返回类型，直接异步执行
        if (method.getReturnType() == void.class) {
            executeAsync(point, asyncExecute);
            return null;
        }

        // 否则同步执行并返回结果（因为调用者需要返回值）
        // 注意：对于需要返回值的方法，@AsyncExecute 注解实际上不会生效
        // 建议对于需要返回值的方法，使用 Future 或 CompletableFuture 包装
        log.warn("方法 {} 需要返回值，@AsyncExecute 注解将不会异步执行，建议改用 CompletableFuture", method.getName());
        return point.proceed();
    }

    /**
     * 异步执行方法
     */
    private void executeAsync(ProceedingJoinPoint point, AsyncExecute config) {
        ExecutorService executor = getExecutor(config.executor());
        int retries = config.retries();
        long retryDelay = config.retryDelay();
        long timeout = config.timeout();
        boolean recordException = config.recordException();

        executor.submit(() -> {
            int attempt = 0;
            Throwable lastException = null;

            while (attempt <= retries) {
                try {
                    if (timeout > 0) {
                        executeWithTimeout(point, timeout);
                    } else {
                        point.proceed();
                    }
                    return; // 成功执行，退出
                } catch (Throwable e) {
                    lastException = e;
                    attempt++;
                    log.warn("异步执行失败，第 {} 次重试，方法：{}，错误：{}", attempt, point.getSignature().getName(), e.getMessage());

                    if (attempt <= retries) {
                        try {
                            Thread.sleep(retryDelay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }

            // 所有重试失败
            if (recordException && lastException != null) {
                log.error("异步执行最终失败，方法：{}，错误：{}", point.getSignature().getName(), lastException);
            }
        });
    }

    /**
     * 带超时执行
     */
    private void executeWithTimeout(ProceedingJoinPoint point, long timeoutMs) throws Throwable {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            try {
                point.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });

        try {
            future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.error("异步执行超时，方法：{}，超时时间：{}ms", point.getSignature().getName(), timeoutMs);
            throw e;
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * 获取执行器
     */
    private ExecutorService getExecutor(String executorName) {
        if ("taskExecutor".equals(executorName) || "default".equals(executorName)) {
            return defaultExecutor;
        }
        // TODO: 支持从 Spring 容器中获取自定义线程池
        return defaultExecutor;
    }
}