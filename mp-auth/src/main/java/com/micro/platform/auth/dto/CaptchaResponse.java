package com.micro.platform.auth.dto;

import java.io.Serializable;

/**
 * 验证码响应 DTO
 */
public class CaptchaResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 验证码 key（用于验证）
     */
    private String captchaKey;

    /**
     * 验证码图片（Base64）
     */
    private String captchaImg;

    /**
     * 过期时间（秒）
     */
    private Long expiresIn;

    public String getCaptchaKey() {
        return captchaKey;
    }

    public void setCaptchaKey(String captchaKey) {
        this.captchaKey = captchaKey;
    }

    public String getCaptchaImg() {
        return captchaImg;
    }

    public void setCaptchaImg(String captchaImg) {
        this.captchaImg = captchaImg;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}