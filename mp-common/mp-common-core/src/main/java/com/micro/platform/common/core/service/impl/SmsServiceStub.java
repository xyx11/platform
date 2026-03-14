package com.micro.platform.common.core.service.impl;

import com.micro.platform.common.core.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 短信服务空实现（用于开发环境）
 */
public class SmsServiceStub implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsServiceStub.class);

    @Override
    public boolean sendCode(String phone, String code) {
        log.info("[短信服务] 发送验证码到手机号：{}，验证码：{}", phone, code);
        // 开发环境下只打印日志，不实际发送
        return true;
    }

    @Override
    public boolean send(String phone, String content) {
        log.info("[短信服务] 发送短信到手机号：{}，内容：{}", phone, content);
        // 开发环境下只打印日志，不实际发送
        return true;
    }
}