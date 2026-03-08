package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysTodo;
import com.micro.platform.system.mapper.SysTodoMapper;
import com.micro.platform.system.service.SysTodoService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 待办事项服务实现类
 */
@Service
public class SysTodoServiceImpl extends ServiceImplX<SysTodoMapper, SysTodo> implements SysTodoService {

    @Override
    public Page<SysTodo> selectTodoPage(SysTodo todo, Integer pageNum, Integer pageSize) {
        Page<SysTodo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysTodo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTodo::getUserId, SecurityUtil.getUserId())
                .like(StringUtils.hasText(todo.getTodoTitle()), SysTodo::getTodoTitle, todo.getTodoTitle())
                .eq(StringUtils.hasText(todo.getTodoType()), SysTodo::getTodoType, todo.getTodoType())
                .eq(todo.getStatus() != null, SysTodo::getStatus, todo.getStatus())
                .eq(todo.getPriority() != null, SysTodo::getPriority, todo.getPriority())
                .orderByDesc(SysTodo::getPriority)
                .orderByDesc(SysTodo::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<SysTodo> getMyTodos(Integer pageNum, Integer pageSize) {
        Page<SysTodo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysTodo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTodo::getUserId, SecurityUtil.getUserId())
                .ne(SysTodo::getStatus, 2) // 排除已取消的
                .orderByAsc(SysTodo::getPlanTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public void completeTodo(Long todoId) {
        SysTodo todo = new SysTodo();
        todo.setTodoId(todoId);
        todo.setStatus(1);
        todo.setActualTime(LocalDateTime.now());
        updateById(todo);
    }

    @Override
    public void cancelTodo(Long todoId) {
        SysTodo todo = new SysTodo();
        todo.setTodoId(todoId);
        todo.setStatus(2);
        updateById(todo);
    }

    @Override
    public Map<String, Object> getTodoStats() {
        Long userId = SecurityUtil.getUserId();
        Map<String, Object> stats = new HashMap<>();

        // 统计总数
        long totalCount = count(new LambdaQueryWrapper<SysTodo>()
                .eq(SysTodo::getUserId, userId)
                .ne(SysTodo::getStatus, 2));
        stats.put("totalCount", totalCount);

        // 统计待处理
        long pendingCount = count(new LambdaQueryWrapper<SysTodo>()
                .eq(SysTodo::getUserId, userId)
                .eq(SysTodo::getStatus, 0));
        stats.put("pendingCount", pendingCount);

        // 统计已完成
        long completedCount = count(new LambdaQueryWrapper<SysTodo>()
                .eq(SysTodo::getUserId, userId)
                .eq(SysTodo::getStatus, 1));
        stats.put("completedCount", completedCount);

        // 统计今日待办
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long todayCount = count(new LambdaQueryWrapper<SysTodo>()
                .eq(SysTodo::getUserId, userId)
                .eq(SysTodo::getStatus, 0)
                .between(SysTodo::getPlanTime, startOfDay, endOfDay));
        stats.put("todayCount", todayCount);

        // 统计紧急待办（优先级为 1 且未完成）
        long urgentCount = count(new LambdaQueryWrapper<SysTodo>()
                .eq(SysTodo::getUserId, userId)
                .eq(SysTodo::getStatus, 0)
                .eq(SysTodo::getPriority, 1));
        stats.put("urgentCount", urgentCount);

        // 统计逾期待办
        long overdueCount = count(new LambdaQueryWrapper<SysTodo>()
                .eq(SysTodo::getUserId, userId)
                .eq(SysTodo::getStatus, 0)
                .lt(SysTodo::getPlanTime, LocalDateTime.now()));
        stats.put("overdueCount", overdueCount);

        // 按类型统计
        Map<String, Long> typeStats = new HashMap<>();
        LambdaQueryWrapper<SysTodo> workQuery = new LambdaQueryWrapper<SysTodo>()
                .eq(SysTodo::getUserId, userId).eq(SysTodo::getTodoType, "1");
        LambdaQueryWrapper<SysTodo> meetingQuery = new LambdaQueryWrapper<SysTodo>()
                .eq(SysTodo::getUserId, userId).eq(SysTodo::getTodoType, "2");
        LambdaQueryWrapper<SysTodo> reminderQuery = new LambdaQueryWrapper<SysTodo>()
                .eq(SysTodo::getUserId, userId).eq(SysTodo::getTodoType, "3");
        LambdaQueryWrapper<SysTodo> otherQuery = new LambdaQueryWrapper<SysTodo>()
                .eq(SysTodo::getUserId, userId).eq(SysTodo::getTodoType, "4");

        typeStats.put("work", count(workQuery));
        typeStats.put("meeting", count(meetingQuery));
        typeStats.put("reminder", count(reminderQuery));
        typeStats.put("other", count(otherQuery));
        stats.put("typeStats", typeStats);

        return stats;
    }
}