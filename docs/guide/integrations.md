# 第三方服务集成

## 短信服务

### 阿里云短信

#### 配置

```yaml
sms:
  provider: aliyun
  access-key-id: your-access-key-id
  access-key-secret: your-access-key-secret
  sign-name: 您的签名名称
  default-template-code: your-template-code
```

#### 使用示例

```java
@Autowired
private SmsService smsService;

// 发送验证码
public void sendVerificationCode(String phone) {
    String code = IdGenerator.nextVerificationCode();
    // 缓存验证码（5 分钟过期）
    redisTemplate.opsForValue().set("sms:code:" + phone, code, 5, TimeUnit.MINUTES);

    Map<String, String> params = new HashMap<>();
    params.put("code", code);
    smsService.sendSms(phone, params);
}

// 发送通知短信
public void sendNotification(String phone, String message) {
    smsService.sendSms(phone, message);
}
```

### 腾讯云短信

#### 配置

```yaml
sms:
  provider: tencent
  secret-id: your-secret-id
  secret-key: your-secret-key
  app-id: your-app-id
  sign-name: 您的签名名称
  template-id: your-template-id
```

## 邮件服务

### 配置

```yaml
spring:
  mail:
    host: smtp.example.com
    port: 587
    username: no-reply@example.com
    password: your-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
```

### 使用示例

```java
@Autowired
private EmailNotificationService emailService;

// 发送简单邮件
public void sendSimpleEmail(String to, String subject, String content) {
    emailService.sendSimpleEmail(to, subject, content);
}

// 发送 HTML 邮件
public void sendHtmlEmail(String to, String subject, String htmlContent) {
    emailService.sendHtmlEmail(to, subject, htmlContent);
}

// 发送带附件的邮件
public void sendEmailWithAttachment(String to, String subject, String content, File attachment) {
    emailService.sendEmailWithAttachment(to, subject, content, attachment);
}

// 发送模板邮件
public void sendTemplateEmail(String to, String subject, Map<String, Object> params) {
    emailService.sendTemplateEmail(to, subject, params);
}
```

## 对象存储（OSS）

### 阿里云 OSS 配置

```yaml
aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key-id: your-access-key-id
    access-key-secret: your-access-key-secret
    bucket-name: your-bucket-name
```

### 使用示例

```java
@Service
public class FileUploadService {

    @Autowired
    private OSS ossClient;

    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String objectName = UUID.randomUUID().toString() + "." + getExtension(file.getOriginalFilename());
        ossClient.putObject(bucketName, objectName, file.getInputStream());
        return "https://" + bucketName + "." + endpoint + "/" + objectName;
    }

    /**
     * 删除文件
     */
    public void deleteFile(String objectName) {
        ossClient.deleteObject(bucketName, objectName);
    }

    /**
     * 获取文件 URL（带签名）
     */
    public String getFileUrl(String objectName, int expiration) {
        Date expirationDate = new Date(System.currentTimeMillis() + expiration * 1000L);
        URL url = ossClient.generatePresignedUrl(bucketName, objectName, expirationDate);
        return url.toString();
    }
}
```

## 微信支付

### 配置

```yaml
wechat:
  pay:
    app-id: wx8888888888888888
    mch-id: 1234567890
    api-key: your-api-key
    notify-url: https://your-domain.com/api/pay/wechat/notify
    key-path: /path/to/apiclient_key.pem
    cert-path: /path/to/apiclient_cert.pem
```

### 使用示例

```java
@Service
public class WechatPayService {

    /**
     * 创建 JSAPI 支付
     */
    public Map<String, String> createJsapiPay(String openId, BigDecimal amount, String orderId) {
        // 统一下单
        Map<String, String> params = new HashMap<>();
        params.put("appid", appId);
        params.put("mch_id", mchId);
        params.put("nonce_str", IdGenerator.nextRandomString(32));
        params.put("body", "商品描述");
        params.put("out_trade_no", orderId);
        params.put("total_fee", amount.multiply(new BigDecimal("100")).intValue());
        params.put("spbill_create_ip", getClientIp());
        params.put("notify_url", notifyUrl);
        params.put("trade_type", "JSAPI");
        params.put("openid", openId);
        params.put("sign", generateSign(params));

        String xml = HttpUtils.post("https://api.mch.weixin.qq.com/pay/unifiedorder", mapToXml(params));
        Map<String, String> result = xmlToMap(xml);

        if ("SUCCESS".equals(result.get("return_code"))) {
            Map<String, String> payParams = new HashMap<>();
            payParams.put("appId", appId);
            payParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
            payParams.put("nonceStr", IdGenerator.nextRandomString(32));
            payParams.put("package", "prepay_id=" + result.get("prepay_id"));
            payParams.put("signType", "MD5");
            payParams.put("paySign", generateSign(payParams));
            return payParams;
        }
        throw new BusinessException("创建订单失败");
    }

    /**
     * 查询订单
     */
    public Map<String, String> queryOrder(String orderId) {
        // 实现订单查询逻辑
    }

    /**
     * 退款
     */
    public Map<String, String> refund(String orderId, BigDecimal amount) {
        // 实现退款逻辑
    }
}
```

## 支付宝

### 配置

```yaml
alipay:
  app-id: 2021000000000000
  private-key: your-private-key
  alipay-public-key: alipay-public-key
  gateway: https://openapi.alipay.com/gateway.do
  notify-url: https://your-domain.com/api/pay/alipay/notify
  return-url: https://your-domain.com/pay/return
```

### 使用示例

```java
@Service
public class AlipayService {

    /**
     * 手机网站支付
     */
    public String wapPay(String orderId, BigDecimal amount, String subject) throws Exception {
        AlipayClient alipayClient = new DefaultAlipayClient(
            gateway, appId, privateKey, "json", "UTF-8", alipayPublicKey, "RSA2");

        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setReturnUrl(returnUrl);
        request.setNotifyUrl(notifyUrl);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);
        bizContent.put("total_amount", amount.setScale(2, RoundingMode.HALF_UP));
        bizContent.put("subject", subject);
        bizContent.put("product_code", "QUICK_WAP_WAY");
        request.setBizContent(bizContent.toString());

        AlipayTradeWapPayResponse response = alipayClient.pageExecute(request);
        return response.getBody();
    }

    /**
     * 查询订单
     */
    public AlipayTradeQueryResponse queryOrder(String orderId) throws Exception {
        // 实现订单查询逻辑
    }

    /**
     * 退款
     */
    public AlipayTradeRefundResponse refund(String orderId, BigDecimal amount) throws Exception {
        // 实现退款逻辑
    }
}
```

## 七牛云存储

### 配置

```yaml
qiniu:
  access-key: your-access-key
  secret-key: your-secret-key
  bucket: your-bucket
  domain: https://your-domain.com
```

### 使用示例

```java
@Service
public class QiniuService {

    @Autowired
    private UploadManager uploadManager;

    @Autowired
    private BucketManager bucketManager;

    /**
     * 上传文件
     */
    public String uploadFile(byte[] data, String fileName) throws QiniuException {
        String token = Auth.create(accessKey, secretKey).uploadToken(bucket);
        Response response = uploadManager.put(data, fileName, token);
        ResBody ret = response.jsonToObject(ResBody.class);
        return domain + "/" + ret.key;
    }

    /**
     * 删除文件
     */
    public void deleteFile(String key) throws QiniuException {
        bucketManager.delete(bucket, key);
    }
}
```

## 快递鸟物流查询

### 配置

```yaml
kuaidiniao:
  api-key: your-api-key
  customer: your-customer-code
```

### 使用示例

```java
@Service
public class LogisticsService {

    /**
     * 物流轨迹查询
     */
    public Map<String, Object> queryTrack(String orderCode, String expCode, String expNo) {
        Map<String, String> params = new HashMap<>();
        params.put("RequestType", "1002");
        params.put("EBusinessID", customer);
        params.put("RequestData", "{\"OrderCode\":\"" + orderCode + "\",\"ShipperCode\":\"" + expCode + "\",\"LogisticCode\":\"" + expNo + "\"}");
        params.put("DataSign", encrypt(params.get("RequestData")));
        params.put("DataType", "2");

        String result = HttpUtils.post("http://api.kdniao.com/Ebusiness/EbusinessOrderHandle.aspx", params);
        return JsonUtils.fromJson(result, Map.class);
    }
}
```

## 容联云通讯

### 配置

```yaml
ronglian:
  server-ip: app.cloopen.com
  server-port: 8883
  account-sid: your-account-sid
  account-token: your-account-token
  app-id: your-app-id
```

### 使用示例

```java
@Service
public class CloudCommunicationService {

    /**
     * 发送模板短信
     */
    public String sendTemplateSms(String to, String templateId, String... datas) {
        // 实现短信发送逻辑
    }

    /**
     * 语音验证码
     */
    public String voiceVerify(String verifyCode, String playTimes, String to) {
        // 实现语音验证码逻辑
    }
}
```