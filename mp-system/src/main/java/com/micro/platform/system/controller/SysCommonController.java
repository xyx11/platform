package com.micro.platform.system.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.micro.platform.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 通用控制器（导入导出等）
 */
@Tag(name = "通用功能", description = "导入导出等通用功能")
@RestController
@RequestMapping("/common")
public class SysCommonController {

    @Operation(summary = "通用导入接口")
    @PostMapping("/import")
    public Result<Map<String, Object>> importData(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) throws IOException {

        Map<String, Object> result = new HashMap<>();

        try (InputStream inputStream = file.getInputStream()) {
            // 使用 Hutool Excel 读取
            List<Map<String, Object>> dataList = ExcelUtil.getReader(inputStream).readAll();

            result.put("count", dataList.size());
            result.put("data", dataList);

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    @Operation(summary = "通用导出接口")
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportData(
            @RequestBody List<Map<String, Object>> data,
            @RequestParam("fileName") String fileName) {

        try {
            // 使用 Hutool Excel 写入
            ExcelWriter writer = ExcelUtil.getWriter(true);
            writer.write(data);

            // 写入到 ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            writer.flush(outputStream);
            writer.close();

            byte[] dataBytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",
                new String((fileName + ".xlsx").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

            return new ResponseEntity<>(dataBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "获取上传模板")
    @GetMapping("/template/{type}")
    public ResponseEntity<byte[]> getTemplate(@PathVariable String type) {
        try {
            ExcelWriter writer = ExcelUtil.getWriter(true);

            // 根据类型创建不同的模板
            List<Map<String, Object>> templateData = new ArrayList<>();
            if ("user".equals(type)) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("用户名", "admin");
                row.put("昵称", "管理员");
                row.put("部门 ID", "100");
                row.put("手机号", "13800138000");
                row.put("邮箱", "admin@example.com");
                templateData.add(row);
            } else if ("dict".equals(type)) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("字典标签", "男");
                row.put("字典值", "1");
                row.put("排序", "1");
                templateData.add(row);
            }

            writer.write(templateData);

            // 写入到 ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            writer.flush(outputStream);
            writer.close();

            byte[] dataBytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",
                new String((type + "_template.xlsx").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

            return new ResponseEntity<>(dataBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        Map<String, String> result = new HashMap<>();

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : "";
        String filename = UUID.randomUUID().toString().replace("-", "") + extension;

        // 保存到本地临时目录（实际项目应该使用 OSS 等）
        String savePath = System.getProperty("java.io.tmpdir") + "/" + filename;
        file.transferTo(new java.io.File(savePath));

        result.put("fileName", filename);
        result.put("originalName", originalFilename);
        result.put("url", "/common/download/" + filename);

        return Result.success(result);
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> download(@PathVariable String filename) {
        try {
            java.io.File file = new java.io.File(System.getProperty("java.io.tmpdir") + "/" + filename);
            if (!file.exists()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            byte[] dataBytes = java.nio.file.Files.readAllBytes(file.toPath());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",
                new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

            return new ResponseEntity<>(dataBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}