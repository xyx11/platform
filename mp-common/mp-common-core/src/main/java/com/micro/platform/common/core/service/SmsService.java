package com.micro.platform.common.core.service;

/**
 * 短信服务接口
 */
public interface SmsService {

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 是否发送成功
     */
    boolean sendCode(String phone, String code);

    /**
     * 发送短信
     *
     * @param phone   手机号
     * @param content 短信内容
     * @return 是否发送成功
     */
    boolean send(String phone, String content);
}