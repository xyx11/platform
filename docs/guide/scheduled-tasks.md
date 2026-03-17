# 定时任务配置

## 概述

系统支持多种定时任务调度方式：
- Spring `@Scheduled` 注解
- 数据库动态配置任务
- Quartz 分布式任务
- XXL-JOB 分布式任务调度

## Spring Scheduled

### 开启定时任务

```java
@Configuration
@EnableScheduling
public class ScheduleConfig {
    // 定时任务配置
}
```

### 定时任务示例

```java
@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    /**
     * 固定延迟执行
     * 上一次执行完成后延迟 5 秒再执行
     */
    @Scheduled(fixedDelay = 5000)
    public void fixedDelayTask() {
        log.info("固定延迟任务执行时间：{}", LocalDateTime.now());
    }

    /**
     * 固定间隔执行
     * 每隔 5 秒执行一次
     */
    @Scheduled(fixedRate = 5000)
    public void fixedRateTask() {
        log.info("固定间隔任务执行时间：{}", LocalDateTime.now());
    }

    /**
     * 初始延迟 + 固定间隔
     * 启动后延迟 10 秒执行，之后每隔 5 秒执行一次
     */
    @Scheduled(initialDelay = 10000, fixedRate = 5000)
    public void initialDelayTask() {
        log.info("初始延迟任务执行时间：{}", LocalDateTime.now());
    }

    /**
     * Cron 表达式
     * 每天凌晨 1 点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void cronTask() {
        log.info("Cron 任务执行时间：{}", LocalDateTime.now());
    }
}
```

### Cron 表达式说明

| 位置 | 含义 | 允许值 | 特殊字符 |
|------|------|--------|----------|
| 1 | 秒 | 0-59 | , - * / |
| 2 | 分 | 0-59 | , - * / |
| 3 | 时 | 0-23 | , - * / |
| 4 | 日 | 1-31 | , - * ? / L W |
| 5 | 月 | 1-12 | , - * / |
| 6 | 周 | 1-7 | , - * ? / L # |
| 7 | 年（可选） | 1970-2099 | , - * / |

### 常用 Cron 表达式

```
# 每秒执行
*/1 * * * * ?

# 每分钟执行
0 */1 * * * ?

# 每小时执行
0 0 */1 * * ?

# 每天零点执行
0 0 0 * * ?

# 每天早上 8 点执行
0 0 8 * * ?

# 每周一早上 8 点执行
0 0 8 ? * MON

# 每月 1 号零点执行
0 0 0 1 * ?

# 每年 1 月 1 日零点执行
0 0 0 1 1 ?

# 工作日（周一至周五）每天早上 9 点执行
0 0 9 ? * MON-FRI

# 每季度第一天零点执行
0 0 0 1 1,4,7,10 ?
```

## 动态配置任务

### 数据库表结构

```sql
CREATE TABLE sys_job (
    job_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务 ID',
    job_name VARCHAR(64) NOT NULL COMMENT '任务名称',
    job_group VARCHAR(64) NOT NULL COMMENT '任务组名',
    job_type VARCHAR(32) DEFAULT 'spring' COMMENT '任务类型（spring/quartz/xxl）',
    bean_name VARCHAR(128) COMMENT 'Bean 名称',
    method_name VARCHAR(64) COMMENT '方法名称',
    method_params VARCHAR(255) COMMENT '方法参数',
    cron_expression VARCHAR(64) COMMENT 'Cron 表达式',
    misfire_policy VARCHAR(20) DEFAULT '3' COMMENT '执行策略（1 立即 2 放弃 3 补做）',
    concurrent_policy VARCHAR(20) DEFAULT '1' COMMENT '并发策略（1 允许 0 禁止）',
    status VARCHAR(2) DEFAULT '0' COMMENT '状态（0 正常 1 暂停）',
    remark VARCHAR(500) COMMENT '备注信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by VARCHAR(64) COMMENT '创建者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by VARCHAR(64) COMMENT '更新者'
) COMMENT '定时任务配置表';
```

### 任务执行接口

```java
public interface JobExecutor {
    /**
     * 执行任务
     * @param job 任务配置
     * @throws Exception 执行异常
     */
    void execute(SysJob job) throws Exception;
}
```

### 任务执行器实现

```java
@Component
public class SpringJobExecutor implements JobExecutor {

    @Override
    public void execute(SysJob job) throws Exception {
        String beanName = job.getBeanName();
        String methodName = job.getMethodName();
        String params = job.getMethodParams();

        Object target = SpringUtils.getBean(beanName);
        Method method = null;

        if (StringUtils.isNotBlank(params)) {
            // 有参数执行
            method = target.getClass().getDeclaredMethod(methodName, String.class);
            method.invoke(target, params);
        } else {
            // 无参数执行
            method = target.getClass().getDeclaredMethod(methodName);
            method.invoke(target);
        }
    }
}
```

### 任务调度器

```java
@Component
public class ScheduleManager {

    @Autowired
    private ScheduledTaskRegistrar taskRegistrar;

    @Autowired
    private JobExecutor jobExecutor;

    /**
     * 添加任务
     */
    public void addTask(SysJob job) {
        taskRegistrar.addTriggerTask(triggerContext -> {
            CronExpression cronExpression = new CronExpression(job.getCronExpression());
            return cronExpression.getNextValidTimeAfter(triggerContext.lastActualExecutionTime());
        }, task -> {
            try {
                jobExecutor.execute(job);
            } catch (Exception e) {
                log.error("任务执行失败：{}", job.getJobName(), e);
            }
        });
    }

    /**
     * 删除任务
     */
    public void removeTask(Long jobId) {
        // 实现任务删除逻辑
    }

    /**
     * 暂停任务
     */
    public void pauseTask(Long jobId) {
        // 实现任务暂停逻辑
    }

    /**
     * 恢复任务
     */
    public void resumeTask(Long jobId) {
        // 实现任务恢复逻辑
    }

    /**
     * 立即执行一次
     */
    public void runTaskOnce(SysJob job) {
        new Thread(() -> {
            try {
                jobExecutor.execute(job);
            } catch (Exception e) {
                log.error("任务立即执行失败：{}", job.getJobName(), e);
            }
        }).start();
    }
}
```

## Quartz 集成

### 依赖配置

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
```

### 配置类

```java
@Configuration
public class QuartzConfig {

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        scheduler.start();
        return scheduler;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource) {
        SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setOverwriteExistingJobs(true);
        factoryBean.setAutoStartup(true);
        factoryBean.setWaitForJobsToCompleteOnShutdown(true);
        return factoryBean;
    }
}
```

### Quartz 配置（application.yml）

```yaml
spring:
  quartz:
    job-store-type: jdbc
    jdbc:
      initialize-schema: never  # 手动创建表
    properties:
      org:
        quartz:
          scheduler:
            instanceName: DefaultScheduler
            instanceId: AUTO
          jobStore:
            class: org.quartz.impl.jdbcjobstore.JobStoreTX
            driverDelegateClass: org.quartz.impl.jdbcjobstore.StdJDBCDelegate
            tablePrefix: QRTZ_
            isClustered: true
            clusterCheckinInterval: 10000
            useProperties: false
          threadPool:
            class: org.quartz.simpl.SimpleThreadPool
            threadCount: 10
            threadPriority: 5
            threadsInheritedContextClassLoaderEnabled: true
```

### Quartz 工具类

```java
@Component
public class QuartzJobManager {

    @Autowired
    private Scheduler scheduler;

    /**
     * 添加任务
     */
    public void addJob(Class<? extends Job> jobClass, String jobName, String jobGroup, String cronExpression) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobName, jobGroup)
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "_trigger", jobGroup)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * 暂停任务
     */
    public void pauseJob(String jobName, String jobGroup) throws SchedulerException {
        scheduler.pauseJob(new JobKey(jobName, jobGroup));
    }

    /**
     * 恢复任务
     */
    public void resumeJob(String jobName, String jobGroup) throws SchedulerException {
        scheduler.resumeJob(new JobKey(jobName, jobGroup));
    }

    /**
     * 删除任务
     */
    public void deleteJob(String jobName, String jobGroup) throws SchedulerException {
        scheduler.deleteJob(new JobKey(jobName, jobGroup));
    }

    /**
     * 立即执行任务
     */
    public void triggerJob(String jobName, String jobGroup) throws SchedulerException {
        scheduler.triggerJob(new JobKey(jobName, jobGroup));
    }

    /**
     * 更新任务时间
     */
    public void modifyJobTime(String jobName, String jobGroup, String cronExpression) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName + "_trigger", jobGroup);
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

        if (trigger != null) {
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
            trigger = trigger.getTriggerBuilder()
                    .withIdentity(triggerKey)
                    .withSchedule(scheduleBuilder)
                    .build();
            scheduler.rescheduleJob(triggerKey, trigger);
        }
    }
}
```

## XXL-JOB 集成

### 依赖配置

```xml
<dependency>
    <groupId>com.xuxueli</groupId>
    <artifactId>xxl-job-core</artifactId>
    <version>2.4.0</version>
</dependency>
```

### 配置文件

```yaml
xxl:
  job:
    admin:
      addresses: http://127.0.0.1:8080/xxl-job-admin
    executor:
      appname: xxl-job-executor
      address:
      ip:
      port: 9999
      logpath: /data/applogs/xxl-job/jobhandler
      logretentiondays: 30
    accessToken:
```

### 配置类

```java
@Configuration
public class XxlJobConfig {

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Value("${xxl.job.executor.appname}")
    private String appname;

    @Value("${xxl.job.executor.port}")
    int port;

    @Value("${xxl.job.accessToken:}")
    private String accessToken;

    @Value("${xxl.job.executor.logpath}")
    private String logPath;

    @Value("${xxl.job.executor.logretentiondays}")
    private int logRetentionDays;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appname);
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);
        return xxlJobSpringExecutor;
    }
}
```

### 任务示例

```java
@Component
public class XxlJobTasks {

    /**
     * 简单任务
     */
    @XxlJob("simpleJobHandler")
    public void simpleJobHandler() throws Exception {
        XxlJobHelper.log("任务开始执行，时间：{}", LocalDateTime.now());
        // 业务逻辑
        XxlJobHelper.log("任务执行完成");
    }

    /**
     * 带参数任务
     */
    @XxlJob("paramJobHandler")
    public void paramJobHandler() throws Exception {
        String param = XxlJobHelper.getJobParam();
        XxlJobHelper.log("任务参数：{}", param);
        // 业务逻辑
    }

    /**
     * 分片任务
     */
    @XxlJob("shardingJobHandler")
    public void shardingJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        XxlJobHelper.log("分片参数：index={}, total={}", shardIndex, shardTotal);

        // 根据分片参数处理数据
        // SELECT * FROM table WHERE id % #{shardTotal} = #{shardIndex}
    }

    /**
     * 脚本任务
     */
    @XxlJob("scriptJobHandler")
    public void scriptJobHandler() throws Exception {
        String script = XxlJobHelper.getJobParam();
        XxlJobHelper.log("脚本内容：{}", script);
        // 执行脚本逻辑
    }
}
```

## 任务执行日志

### 日志记录

```java
@Aspect
@Component
public class JobLogAspect {

    @Autowired
    private JobLogMapper jobLogMapper;

    @Around("@annotation(jobAnnotation)")
    public Object around(ProceedingJoinPoint pjp, Scheduled jobAnnotation) throws Throwable {
        long startTime = System.currentTimeMillis();
        JobLog jobLog = new JobLog();
        jobLog.setJobName(getJobName(pjp));
        jobLog.setStartTime(new Date());
        jobLog.setStatus("running");

        try {
            Object result = pjp.proceed();
            jobLog.setStatus("success");
            return result;
        } catch (Exception e) {
            jobLog.setStatus("failed");
            jobLog.setErrorMsg(ExceptionUtil.getMessage(e));
            throw e;
        } finally {
            jobLog.setEndTime(new Date());
            jobLog.setDuration(System.currentTimeMillis() - startTime);
            jobLogMapper.insert(jobLog);
        }
    }
}
```

## 最佳实践

### 1. 避免任务重叠执行

```java
@Component
public class SafeScheduledTasks {

    @Autowired
    private RedisDistributedLock redisLock;

    @Scheduled(cron = "0 */5 * * * ?")
    public void safeTask() {
        String lockKey = "job:safe-task";
        String clientId = UUID.randomUUID().toString();

        try {
            boolean locked = redisLock.tryLock(lockKey, clientId, 300, 3000, 100);
            if (locked) {
                // 执行业务逻辑
                doBusiness();
            } else {
                log.warn("获取锁失败，跳过执行");
            }
        } finally {
            redisLock.unlock(lockKey, clientId);
        }
    }
}
```

### 2. 异常处理

```java
@Scheduled(cron = "0 0 * * * ?")
@Async
public void asyncTask() {
    try {
        // 业务逻辑
    } catch (Exception e) {
        log.error("定时任务执行异常", e);
        // 发送告警
        alertService.sendAlert("定时任务异常", e.getMessage());
    }
}
```

### 3. 任务监控

```java
@RestController
@RequestMapping("/monitor/job")
public class JobMonitorController {

    @Autowired
    private Scheduler scheduler;

    @GetMapping("/list")
    public List<JobInfo> listJobs() throws SchedulerException {
        List<JobInfo> jobInfos = new ArrayList<>();

        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                JobInfo info = new JobInfo();
                info.setJobName(jobKey.getName());
                info.setJobGroup(jobKey.getGroup());

                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                if (!triggers.isEmpty()) {
                    Trigger trigger = triggers.get(0);
                    info.setNextFireTime(trigger.getNextFireTime());
                    info.setPrevFireTime(trigger.getPreviousFireTime());
                    info.setTriggerState(scheduler.getTriggerState(trigger.getKey()));
                }
                jobInfos.add(info);
            }
        }
        return jobInfos;
    }
}
```