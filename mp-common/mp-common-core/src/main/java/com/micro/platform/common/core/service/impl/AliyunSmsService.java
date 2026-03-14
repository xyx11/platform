package com.micro.platform.common.core.service.impl;

import com.micro.platform.common.core.config.SmsProperties;
import com.micro.platform.common.core.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 阿里云短信服务实现
 */
@Service
@ConditionalOnProperty(prefix = "sms", name = "provider", havingValue = "aliyun")
public class AliyunSmsService implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsService.class);

    private final SmsProperties smsProperties;

    public AliyunSmsService(SmsProperties smsProperties) {
        this.smsProperties = smsProperties;
    }

    @Override
    public boolean sendCode(String phone, String code) {
        if (!smsProperties.isEnabled()) {
            log.warn("短信服务未启用，验证码：{}", code);
            return false;
        }

        try {
            // TODO: 集成阿里云短信 SDK
            // 示例代码：
            // DefaultProfile profile = DefaultProfile.getProfile(
            //     smsProperties.getAliyunRegionId(),
            //     smsProperties.getAccessKeyId(),
            //     smsProperties.getAccessKeySecret()
            // );
            // IAcsClient client = new DefaultAcsClient(profile);
            // SendSmsRequest request = new SendSmsRequest();
            // request.setPhoneNumbers(phone);
            // request.setSignName(smsProperties.getSignName());
            // request.setTemplateCode(smsProperties.getTemplateCode());
            // request.setTemplateParam("{\"code\":\"" + code + "\"}");
            // SendSmsResponse response = client.getAcsResponse(request);
            // return "OK".equals(response.getCode());

            log.info("[阿里云短信] 发送验证码到：{}，验证码：{}", phone, code);
            return true;
        } catch (Exception e) {
            log.error("[阿里云短信] 发送失败：{}", e.getMessage());
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
            // 实际项目中需要调用阿里云短信 API
            log.info("[阿里云短信] 发送到：{}，内容：{}", phone, content);
            return true;
        } catch (Exception e) {
            log.error("[阿里云短信] 发送失败：{}", e.getMessage());
            return false;
        }
    }
}