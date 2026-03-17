# 通用工具类参考

## Common Core 工具类

### IdGenerator - ID 生成器

基于雪花算法的分布式 ID 生成器。

```java
// 生成分布式 ID（长整型）
long id = IdGenerator.getInstance().nextId();

// 生成雪花 ID（字符串）
String snowflakeId = IdGenerator.nextSnowflakeId();

// 生成 UUID（32 位，不含横杠）
String uuid = IdGenerator.nextUUID();

// 生成随机字符串
String randomStr = IdGenerator.nextRandomString(16);

// 生成随机数字
String randomNum = IdGenerator.nextRandomNumeric(6);

// 生成验证码
String code = IdGenerator.nextVerificationCode();      // 6 位
String code4 = IdGenerator.nextVerificationCode4();    // 4 位

// 生成订单号
String orderId = IdGenerator.nextOrderId();

// 生成交易流水号
String transactionId = IdGenerator.nextTransactionId();

// 生成支付流水号
String paymentId = IdGenerator.nextPaymentId();

// 生成退款单号
String refundId = IdGenerator.nextRefundId();
```

### CollectionUtils - 集合工具

```java
// 空值检查
CollectionUtils.isEmpty(list);
CollectionUtils.isNotEmpty(list);
CollectionUtils.isEmpty(map);

// 获取元素
CollectionUtils.first(list);
CollectionUtils.last(list);
CollectionUtils.get(list, 5);
CollectionUtils.get(map, "key", "default");

// 列表操作
CollectionUtils.subList(list, 0, 10);
CollectionUtils.limit(list, 10);
CollectionUtils.skip(list, 5);
CollectionUtils.reverse(list);
CollectionUtils.shuffle(list);
CollectionUtils.sort(collection);

// 分块
List<List<T>> chunks = CollectionUtils.chunk(list, 100);

// 去重（基于字段）
List<T> distinct = CollectionUtils.distinctBy(list, User::getId);

// 合并
List<T> merged = CollectionUtils.concat(list1, list2);

// 集合运算
CollectionUtils.intersect(list1, list2);     // 交集
CollectionUtils.difference(list1, list2);    // 差集
CollectionUtils.union(list1, list2);         // 并集
CollectionUtils.hasIntersection(list1, list2); // 是否有交集

// 统计
CollectionUtils.sumInt(intList);
CollectionUtils.sumLong(longList);
CollectionUtils.sumDouble(doubleList);
CollectionUtils.averageInt(intList);
CollectionUtils.min(collection);
CollectionUtils.max(collection);

// 创建集合
CollectionUtils.of("a", "b", "c");
CollectionUtils.emptyList();
CollectionUtils.singletonList(item);
```

### HttpUtils - HTTP 工具

```java
// GET 请求
String result = HttpUtils.get("https://api.example.com/data");
String result = HttpUtils.get(url, params);
String result = HttpUtils.get(url, params, headers);

// POST 请求
String result = HttpUtils.post(url, body);
String result = HttpUtils.post(url, body, headers);

// POST 表单
Map<String, String> params = new HashMap<>();
params.put("key", "value");
String result = HttpUtils.postForm(url, params);

// PUT 请求
String result = HttpUtils.put(url, body);

// DELETE 请求
String result = HttpUtils.delete(url);

// URL 编码/解码
String encoded = HttpUtils.encode("中文");
String decoded = HttpUtils.decode(encoded);

// 检查 URL 是否可访问
boolean reachable = HttpUtils.isReachable(url);

// 获取状态码
int statusCode = HttpUtils.getStatusCode(url);

// 下载文件
HttpUtils.downloadFile(fileUrl, savePath);

// 上传文件
String response = HttpUtils.uploadFile(url, "file", file, params);
```

### StringUtils - 字符串工具

```java
// 空值检查
StringUtils.isBlank("  ");        // true
StringUtils.isNotBlank("hello");  // true
StringUtils.isEmpty("");          // true
StringUtils.isNotEmpty("abc");    // true

// 修剪
StringUtils.trim("  abc  ");      // "abc"
StringUtils.trimToNull("  ");     // null

// 大小写转换
StringUtils.capitalize("hello");   // "Hello"
StringUtils.uncapitalize("Hello"); // "hello"

// 命名转换
StringUtils.camelToUnderline("userName");    // "user_name"
StringUtils.underlineToCamel("user_name");   // "userName"
StringUtils.underlineToPascal("user_name");  // "UserName"
StringUtils.camelToKebab("userName");        // "user-name"
StringUtils.kebabToCamel("user-name");       // "userName"

// 填充
StringUtils.padRight("123", 5, '0');  // "12300"
StringUtils.padLeft("123", 5, '0');   // "00123"

// 截取
StringUtils.left("hello world", 5);   // "hello"
StringUtils.right("hello world", 5);  // "world"
StringUtils.truncate("hello world", 8); // "hello..."

// 类型转换
StringUtils.toInt("123", 0);           // 123
StringUtils.toIntOrNull("abc");        // null
StringUtils.toLong("123", 0L);
StringUtils.toDouble("3.14", 0.0);
StringUtils.toBoolean("true");         // true

// 分割/连接
StringUtils.split("a,b,c", ",");       // ["a", "b", "c"]
StringUtils.splitByComma("a,b,c");
StringUtils.join(list, ",");

// 替换
StringUtils.replaceOnce("abcabc", "abc", "xyz");  // "xyzabc"
StringUtils.replaceIgnoreCase("ABC", "abc", "xyz"); // "xyz"
StringUtils.remove("hello", "l");      // "heo"
StringUtils.removeWhitespace("a b c"); // "abc"

// 验证
StringUtils.isNumeric("123.45");       // true
StringUtils.isAlpha("abc");            // true
StringUtils.isAlphanumeric("abc123");  // true
StringUtils.isChinese("中文");          // true

// 其他
StringUtils.reverse("abc");            // "cba"
StringUtils.countMatches("abcabc", "abc"); // 2
StringUtils.escapeHtml("<div>");       // "&lt;div&gt;"
StringUtils.randomString(10);
StringUtils.randomNumeric(6);
StringUtils.isIn("a", "a", "b", "c");  // true
```

### Arith - 数学计算工具

```java
// 精确运算（避免浮点精度丢失）
Arith.add(0.1, 0.2);           // 0.3
Arith.sub(1.0, 0.1);           // 0.9
Arith.mul(3.14, 2);            // 6.28
Arith.div(1.0, 3, 4);          // 0.3333

// 四舍五入
Arith.round(3.14159, 2);       // 3.14

// 平方根
Arith.sqrt(16, 4);             // 4.0

// 幂运算
Arith.pow(2, 10);              // 1024.0

// 百分比
Arith.percentage(50, 200);     // 25.0

// 累加/平均
Arith.sum(1.1, 2.2, 3.3);      // 6.6
Arith.average(1.0, 2.0, 3.0);  // 2.5

// 金额格式化
Arith.formatMoney(123.456);    // 123.46

// 限制范围
Arith.clamp(15, 0, 10);        // 10
```

### JsonUtils - JSON 工具

```java
// 对象转 JSON
String json = JsonUtils.toJson(obj);
String pretty = JsonUtils.toPrettyJson(obj);

// JSON 转对象
User user = JsonUtils.fromJson(json, User.class);
List<User> users = JsonUtils.fromJson(json, new TypeReference<List<User>>() {});
Map<String, Object> map = JsonUtils.toMap(json);

// JsonNode 操作
JsonNode node = JsonUtils.readTree(json);
String name = JsonUtils.getString(node, "name");
Integer age = JsonUtils.getInt(node, "age");
Boolean active = JsonUtils.getBoolean(node, "active");
JsonNode arr = JsonUtils.getArray(node, "items");

// 创建 JSON
ObjectNode obj = JsonUtils.createObjectNode();
obj.put("name", "张三");
obj.put("age", 25);

ArrayNode arr = JsonUtils.createArrayNode();
arr.add("item1");
arr.add("item2");

// JSON 验证
JsonUtils.isValidJson(json);        // true
JsonUtils.isJsonObject(json);       // true
JsonUtils.isJsonArray(json);        // false

// JSON 操作
JsonUtils.mergeJson(json1, json2);
JsonUtils.removeField(json, "fieldName");
JsonUtils.addToArray(jsonArray, element);
```

### DictUtils - 字典工具

```java
// 设置字典数据
Map<String, String> dict = new HashMap<>();
dict.put("0", "女");
dict.put("1", "男");
DictUtils.setDictData("user_sex", dict);

// 获取字典标签
String label = DictUtils.getDictLabel("user_sex", "1");  // "男"

// 获取字典值
String value = DictUtils.getDictValue("user_sex", "男"); // "1"

// 获取字典选项（用于下拉框）
List<Map<String, String>> options = DictUtils.getDictOptions("user_sex");

// 检查是否包含
DictUtils.containsValue("user_sex", "1");
DictUtils.containsLabel("user_sex", "男");

// 批量操作
DictUtils.addDictItem("status", "1", "启用");
DictUtils.clearDict("user_sex");
DictUtils.clearAll();
```

### RedisDistributedLock - 分布式锁

```java
@Autowired
private RedisDistributedLock redisLock;

// 尝试获取锁
boolean locked = redisLock.tryLock("lock:key", "clientId", 300);

// 带等待时间的锁
boolean locked = redisLock.tryLock("lock:key", "clientId", 300, 5000, 100);

// 释放锁
redisLock.unlock("lock:key", "clientId");

// 执行加锁操作
redisLock.executeWithLock("lock:key", 300, () -> {
    // 业务逻辑
    return null;
});

// 尝试执行（获取锁失败返回 null）
redisLock.tryExecuteWithLock("lock:key", 300, () -> {
    // 业务逻辑
    return result;
});

// 续期锁
redisLock.renewLock("lock:key", "clientId", 300);

// 检查是否持有锁
boolean isLocked = redisLock.isLocked("lock:key", "clientId");
```

### ExcelUtils - Excel 工具

```java
// 定义实体类
public class User {
    @ExcelColumn(header = "用户 ID", width = 10)
    private Long userId;

    @ExcelColumn(header = "用户名", width = 20)
    private String userName;

    @ExcelColumn(header = "邮箱", width = 25)
    private String email;

    @ExcelColumn(header = "创建时间", dateFormat = "yyyy-MM-dd")
    private Date createTime;
}

// 导出 Excel
List<User> users = ...;
ExcelUtils.exportExcel(users, User.class, response, "用户列表");

// 通用导出
List<Map<String, Object>> data = ...;
List<String> headers = Arrays.asList("姓名", "年龄", "性别");
List<String> fields = Arrays.asList("name", "age", "sex");
ExcelUtils.exportExcel(data, headers, fields, response, "数据导出");

// 导入 Excel
List<User> users = ExcelUtils.importExcel(workbook, User.class);
List<Map<String, Object>> rows = ExcelUtils.readExcel(workbook);
```

## 前端工具函数

### 存储工具

```javascript
// localStorage
storage.set('key', { data: 'value' })
const data = storage.get('key')
storage.remove('key')
storage.clear()

// sessionStorage
session.set('key', 'value')
const val = session.get('key')
session.remove('key')
```

### 设备检测

```javascript
isMobile()      // 是否移动设备
isIOS()         // 是否 iOS
isAndroid()     // 是否 Android
isWeChat()      // 是否微信浏览器
getDeviceInfo() // 获取设备信息
```

### 文件处理

```javascript
// 文件转 Base64
const base64 = await fileToBase64(file)

// Base64 转 File
const file = base64ToFile(base64, 'image.png', 'image/png')

// 压缩图片
const blob = await compressImage(file, 0.7)

// 懒加载/预加载图片
lazyLoadImage(imgElement, src)
await preloadImages([url1, url2])

// 获取文件信息
getFileExtension('file.txt')      // 'txt'
getFileNameWithoutExtension('file.txt') // 'file'
```

### 数据格式化

```javascript
// 金额
formatMoney(1234.5)        // ¥1,234.50
formatMoney(1234.5, '$')   // $1,234.50

// 数字
formatNumber(1234567)      // 1,234,567

// 百分比
formatPercentage(50, 200)  // 25.00%

// 字节
formatBytes(1048576)       // 1 MB
formatBytes(1048576, 'GB') // 0.00 GB

// 时间
formatTime(new Date())     // 刚刚/5 分钟前/1 小时前
formatRelativeTime(date)   // 3 天前
parseTime(date, '{y}-{m}-{d}') // 2024-01-01

// 进度条颜色
getProgressColor(75)       // '#faad14'
```

### 数据脱敏

```javascript
maskPhone('13800138000')    // 138****8000
maskIdCard('110101199001011234') // 110101********1234
maskEmail('test@example.com') // t**t@example.com
```

### Cookie 操作

```javascript
parseCookies()              // 解析 Cookie
setCookie('token', 'abc', 7) // 设置 Cookie
deleteCookie('token')       // 删除 Cookie
```

### 对象操作

```javascript
// 深拷贝
const copy = deepClone(obj)

// 深度合并
const merged = deepMerge(obj1, obj2)

// 深度比较
const equal = deepEqual(obj1, obj2)

// 对象转 URL 参数
toQueryString({a: 1, b: 2}) // ?a=1&b=2
```

### 数组操作

```javascript
// 数组转树
const tree = arrayToTree(array, 'id', 'parentId')

// 树转数组
const flat = treeToArray(tree)

// 分块
const chunks = chunk(array, 10)

// 去重
const unique = uniqueBy(array, 'id')
```

### URL 操作

```javascript
// 获取参数
getUrlParam('id')
getUrlParams()

// 添加/移除参数
addUrlParams(url, {a: 1})
removeUrlParams(url, 'a')
```

### 其他工具

```javascript
// 复制到剪贴板
const success = await copyToClipboard(text)
await copyWithMessage(text, '复制成功', '复制失败')

// 下载
downloadExcel(blob, '文件名.xlsx')
downloadJson(data, 'data.json')
exportTableToExcel(data, 'export.xlsx')

// 延迟/重试
await sleep(1000)
await delay(1000)
const result = await retry(fn, 3, 1000)
await withTimeout(promise, 5000)

// 并发控制
const results = await runWithConcurrency(tasks, 5)

// 防抖/节流
const debouncedFn = debounce(fn, 500)
const throttledFn = throttle(fn, 500)

// 滚动
scrollToTop(300)
getScrollPosition()

// 全屏
toggleFullscreen()
enterFullscreen()
exitFullscreen()
isFullscreen()
```