package com.micro.platform.common.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短信服务配置属性
 */
@Component
@ConfigurationProperties(prefix = "sms")
public class SmsProperties {

    /**
     * 短信服务商：aliyun, tencent, huawei
     */
    private String provider = "aliyun";

    /**
     * Access Key ID
     */
    private String accessKeyId;

    /**
     * Access Key Secret
     */
    private String accessKeySecret;

    /**
     * 签名名称
     */
    private String signName;

    /**
     * 模板 ID（验证码模板）
     */
    private String templateCode;

    /**
     * 是否启用短信服务
     */
    private boolean enabled = false;

    // ==================== 阿里云配置 ====================

    /**
     * 阿里云 Region ID
     */
    private String aliyunRegionId = "cn-hangzhou";

    // ==================== 腾讯云配置 ====================

    /**
     * 腾讯云短信 SDK AppID
     */
    private String tencentAppId;

    /**
     * 腾讯云短信地域，如 ap-shanghai
     */
    private String tencentRegion = "ap-shanghai";

    // Getters and Setters
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getSignName() {
        return signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAliyunRegionId() {
        return aliyunRegionId;
    }

    public void setAliyunRegionId(String aliyunRegionId) {
        this.aliyunRegionId = aliyunRegionId;
    }

    public String getTencentAppId() {
        return tencentAppId;
    }

    public void setTencentAppId(String tencentAppId) {
        this.tencentAppId = tencentAppId;
    }

    public String getTencentRegion() {
        return tencentRegion;
    }

    public void setTencentRegion(String tencentRegion) {
        this.tencentRegion = tencentRegion;
    }
}