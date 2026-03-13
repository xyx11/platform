package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysTodo;
import com.micro.platform.system.entity.SysTodoRecycleBin;
import com.micro.platform.system.mapper.SysTodoMapper;
import com.micro.platform.system.mapper.SysTodoRecycleBinMapper;
import com.micro.platform.system.service.SysTodoRecycleBinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 任务回收站服务实现
 */
@Service
public class SysTodoRecycleBinServiceImpl extends ServiceImplX<SysTodoRecycleBinMapper, SysTodoRecycleBin> implements SysTodoRecycleBinService {

    private static final Logger log = LoggerFactory.getLogger(SysTodoRecycleBinServiceImpl.class);

    private final SysTodoMapper todoMapper;

    public SysTodoRecycleBinServiceImpl(SysTodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveToRecycleBin(Long todoId) {
        // 获取待办信息
        SysTodo todo = todoMapper.selectById(todoId);
        if (todo == null) {
            throw new BusinessException("待办事项不存在");
        }

        // 创建回收站记录
        SysTodoRecycleBin recycleBin = new SysTodoRecycleBin();
        recycleBin.setTodoId(todoId);
        recycleBin.setUserId(todo.getUserId());
        recycleBin.setTodoTitle(todo.getTodoTitle());
        recycleBin.setTodoContent(todo.getTodoContent());
        recycleBin.setDeleteBy(SecurityUtil.getUserId());
        recycleBin.setDeleteByName(SecurityUtil.getUsername());
        recycleBin.setDeleteTime(LocalDateTime.now());
        recycleBin.setIsRecover(0);
        baseMapper.insert(recycleBin);

        // 逻辑删除待办事项
        todoMapper.deleteById(todoId);

        log.info("移动待办到回收站：{}, 标题：{}", todoId, todo.getTodoTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recoverTodo(Long recycleId) {
        SysTodoRecycleBin recycleBin = baseMapper.selectById(recycleId);
        if (recycleBin == null) {
            throw new BusinessException("回收站记录不存在");
        }

        if (recycleBin.getIsRecover() != null && recycleBin.getIsRecover() == 1) {
            throw new BusinessException("该待办已恢复");
        }

        // 恢复待办事项
        SysTodo todo = new SysTodo();
        todo.setTodoId(recycleBin.getTodoId());
        todo.setUserId(recycleBin.getUserId());
        todo.setTodoTitle(recycleBin.getTodoTitle());
        todo.setTodoContent(recycleBin.getTodoContent());
        todo.setDeleted(0);
        todoMapper.updateById(todo);

        // 更新回收站记录
        recycleBin.setIsRecover(1);
        recycleBin.setRecoverTime(LocalDateTime.now());
        baseMapper.updateById(recycleBin);

        log.info("恢复待办：{}", recycleBin.getTodoId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermanently(Long recycleId) {
        SysTodoRecycleBin recycleBin = baseMapper.selectById(recycleId);
        if (recycleBin == null) {
            throw new BusinessException("回收站记录不存在");
        }

        // 删除回收站记录
        baseMapper.deleteById(recycleId);

        log.info("永久删除待办：{}", recycleBin.getTodoId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearRecycleBin() {
        Long userId = SecurityUtil.getUserId();
        LambdaQueryWrapper<SysTodoRecycleBin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTodoRecycleBin::getUserId, userId);
        baseMapper.delete(wrapper);
        log.info("清空回收站，用户：{}", userId);
    }

    @Override
    public Page<SysTodoRecycleBin> getRecycleBinPage(Integer pageNum, Integer pageSize) {
        Page<SysTodoRecycleBin> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysTodoRecycleBin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTodoRecycleBin::getUserId, SecurityUtil.getUserId())
                .orderByDesc(SysTodoRecycleBin::getDeleteTime);
        return baseMapper.selectPage(page, wrapper);
    }
}