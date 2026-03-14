package com.micro.platform.common.core.service.impl;

import com.micro.platform.common.core.config.SmsProperties;
import com.micro.platform.common.core.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 腾讯云短信服务实现
 */
@Service
@ConditionalOnProperty(prefix = "sms", name = "provider", havingValue = "tencent")
public class TencentSmsService implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(TencentSmsService.class);

    private final SmsProperties smsProperties;

    public TencentSmsService(SmsProperties smsProperties) {
        this.smsProperties = smsProperties;
    }

    @Override
    public boolean sendCode(String phone, String code) {
        if (!smsProperties.isEnabled()) {
            log.warn("短信服务未启用，验证码：{}", code);
            return false;
        }

        try {
            // ========== 腾讯云短信 SDK 集成示例 ==========
            // 1. 添加依赖 (pom.xml):
            //    <dependency>
            //        <groupId>com.tencentcloudapi</groupId>
            //        <artifactId>tencentcloud-sdk-java-sms</artifactId>
            //        <version>3.1.0</version>
            //    </dependency>
            // 2. 取消下面代码注释并配置 application.yml 中的短信参数
            // 示例代码：
            // Credential cred = new Credential(
            //     smsProperties.getAccessKeyId(),
            //     smsProperties.getAccessKeySecret()
            // );
            // ClientProfile clientProfile = new ClientProfile();
            // clientProfile.setHttpProfile(new HttpProfile());
            // clientProfile.setSignMethod("HmacSHA256");
            // SmsClient client = new SmsClient(cred, smsProperties.getTencentRegion(), clientProfile);
            // SendSmsRequest req = new SendSmsRequest();
            // req.setPhoneNumberSet(new String[]{phone});
            // req.setSmsSdkAppId(smsProperties.getTencentAppId());
            // req.setSignName(smsProperties.getSignName());
            // req.setTemplateId(smsProperties.getTemplateCode());
            // req.setTemplateParamSet(new String[]{"\"" + code + "\""});
            // SendSmsResponse response = client.SendSms(req);

            log.info("[腾讯云短信] 发送验证码到：{}，验证码：{}", phone, code);
            return true;
        } catch (Exception e) {
            log.error("[腾讯云短信] 发送失败：{}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean send(String phone, String content) {
        if (!smsProperties.isEnabled()) {
            log.warn("短信服务未启用");
            return false;
        }

        try {
            // 实际项目中需要调用腾讯云短信 API
            log.info("[腾讯云短信] 发送到：{}，内容：{}", phone, content);
            return true;
        } catch (Exception e) {
            log.error("[腾讯云短信] 发送失败：{}", e.getMessage());
            return false;
        }
    }
}