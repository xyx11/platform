package com.micro.platform.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysDictType;
import com.micro.platform.system.mapper.SysDictTypeMapper;
import com.micro.platform.system.service.SysDictTypeService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.util.List;

/**
 * 字典类型服务实现
 */
@Service
public class SysDictTypeServiceImpl extends ServiceImplX<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    @Override
    public Page<SysDictType> selectDictTypePage(SysDictType dictType, Integer pageNum, Integer pageSize) {
        Page<SysDictType> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dictType.getDictName()), SysDictType::getDictName, dictType.getDictName())
                .like(StringUtils.hasText(dictType.getDictType()), SysDictType::getDictType, dictType.getDictType())
                .eq(dictType.getStatus() != null, SysDictType::getStatus, dictType.getStatus())
                .orderByDesc(SysDictType::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SysDictType> selectDictTypeList(SysDictType dictType) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dictType.getDictName()), SysDictType::getDictName, dictType.getDictName())
                .like(StringUtils.hasText(dictType.getDictType()), SysDictType::getDictType, dictType.getDictType())
                .eq(dictType.getStatus() != null, SysDictType::getStatus, dictType.getStatus())
                .orderByDesc(SysDictType::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public void exportDictType(HttpServletResponse response, SysDictType dictType) {
        try {
            List<SysDictType> list = selectDictTypeList(dictType);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("字典类型数据", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), SysDictType.class)
                    .sheet("字典类型")
                    .doWrite(list);
        } catch (Exception e) {
            throw new RuntimeException("导出字典类型数据失败：" + e.getMessage());
        }
    }
}