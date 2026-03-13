package com.micro.platform.system.job;

import com.micro.platform.system.entity.SysTodo;
import com.micro.platform.system.entity.SysUser;
import com.micro.platform.system.service.EmailNotificationService;
import com.micro.platform.system.service.SysTodoService;
import com.micro.platform.system.service.SysUserService;
import com.micro.platform.system.service.WebSocketNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 待办事项提醒定时任务
 */
@Component
@ConditionalOnProperty(prefix = "spring.mail", name = "host", matchIfMissing = false)
public class TodoReminderJob {

    private static final Logger log = LoggerFactory.getLogger(TodoReminderJob.class);

    private final SysTodoService sysTodoService;
    private final SysUserService sysUserService;
    private final WebSocketNotificationService webSocketNotificationService;

    @Autowired(required = false)
    private EmailNotificationService emailNotificationService;

    public TodoReminderJob(SysTodoService sysTodoService,
                           SysUserService sysUserService,
                           WebSocketNotificationService webSocketNotificationService) {
        this.sysTodoService = sysTodoService;
        this.sysUserService = sysUserService;
        this.webSocketNotificationService = webSocketNotificationService;
    }

    /**
     * 每天早上 9 点发送待办提醒
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendMorningReminder() {
        log.info("开始执行待办事项晨间提醒任务");

        // 获取所有用户的待办统计
        // 这里简化处理，实际应该遍历所有用户
        List<SysTodo> overdueTodos = sysTodoService.getOverdueTodos();

        if (!overdueTodos.isEmpty() && emailNotificationService != null) {
            for (SysTodo todo : overdueTodos) {
                SysUser user = sysUserService.getById(todo.getUserId());
                if (user != null && user.getEmail() != null) {
                    // 发送 WebSocket 通知
                    webSocketNotificationService.sendTodoNotification(
                            todo.getUserId(),
                            todo.getTodoTitle(),
                            "您有待处理的逾期事项",
                            "OVERDUE"
                    );

                    // 发送邮件通知
                    emailNotificationService.sendTodoReminderEmail(
                            user.getEmail(),
                            todo.getTodoTitle(),
                            todo.getTodoContent(),
                            todo.getPlanTime().toString()
                    );
                }
            }
        }

        log.info("待办事项晨间提醒任务执行完成");
    }

    /**
     * 每小时检查即将到期的待办
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkExpiringTodos() {
        log.info("开始执行即将到期待办检查任务");

        // 获取 3 天内到期的待办
        List<SysTodo> expiringTodos = sysTodoService.getExpiringTodos(3);

        for (SysTodo todo : expiringTodos) {
            SysUser user = sysUserService.getById(todo.getUserId());
            if (user != null && user.getEmail() != null) {
                // 发送 WebSocket 通知
                webSocketNotificationService.sendTodoNotification(
                        todo.getUserId(),
                        todo.getTodoTitle(),
                        "您有待处理的事项即将到期",
                        "EXPIRING"
                );
            }
        }

        log.info("即将到期待办检查任务执行完成");
    }
}