package com.micro.platform.common.core.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.micro.platform.common.core.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理系统各类异常，返回标准化的响应结果
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常：[{}] {}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleValidationException(Exception e) {
        String message;
        if (e instanceof MethodArgumentNotValidException ex) {
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("; "));
        } else {
            BindException ex = (BindException) e;
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("; "));
        }
        log.warn("参数校验失败：{}", message);
        return Result.error(400, message);
    }

    /**
     * 权限异常 - 没有权限
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限异常：{}", e.getMessage());
        return Result.error(403, "没有权限执行此操作");
    }

    /**
     * Sa-Token 权限异常 - 没有权限
     */
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleNotPermissionException(NotPermissionException e) {
        log.warn("权限不足：{}", e.getMessage());
        return Result.error(403, "没有权限执行此操作");
    }

    /**
     * Sa-Token 角色异常 - 没有角色
     */
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleNotRoleException(NotRoleException e) {
        log.warn("角色不足：{}", e.getMessage());
        return Result.error(403, "没有足够的角色权限执行此操作");
    }

    /**
     * Sa-Token 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleNotLoginException(NotLoginException e) {
        // 仅记录警告级别日志，不打印完整堆栈
        String reason = switch (e.getType()) {
            case NotLoginException.NOT_TOKEN -> "未提供 token";
            case NotLoginException.INVALID_TOKEN -> "token 无效";
            case NotLoginException.TOKEN_TIMEOUT -> "token 已过期";
            case NotLoginException.BE_REPLACED -> "账号在其他地方登录";
            case NotLoginException.KICK_OUT -> "账号已被踢出";
            default -> e.getMessage();
        };
        log.warn("登录状态异常：{}", reason);
        return Result.error(401, "登录状态失效，请重新登录");
    }

    /**
     * 资源未找到异常
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNoResourceFoundException(NoResourceFoundException e) {
        log.debug("资源未找到：{}", e.getMessage());
        return Result.error(404, "资源未找到");
    }

    /**
     * 重复键异常（数据库唯一约束）
     */
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("数据已存在：{}", e.getMessage());
        return Result.error(400, "数据已存在，请勿重复添加");
    }

    /**
     * 其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("系统异常：{}", e.getMessage(), e);
        return Result.error("系统异常，请联系管理员");
    }
}