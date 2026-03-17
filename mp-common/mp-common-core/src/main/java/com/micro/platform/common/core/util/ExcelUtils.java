package com.micro.platform.common.core.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.*;

/**
 * Excel 工具类
 * 基于 Apache POI
 */
public class ExcelUtils {

    /**
     * 导出 Excel
     *
     * @param list 数据列表
     * @param clazz 实体类
     * @param response HTTP 响应
     * @param filename 文件名
     */
    public static <T> void exportExcel(List<T> list, Class<T> clazz,
                                       HttpServletResponse response, String filename) throws IOException {
        Workbook workbook = new SXSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        Field[] fields = clazz.getDeclaredFields();
        int colIndex = 0;
        List<String> exportFields = new ArrayList<>();

        for (Field field : fields) {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            if (annotation != null) {
                Cell cell = headerRow.createCell(colIndex);
                cell.setCellValue(annotation.header());
                cell.setCellStyle(createHeaderStyle(workbook));
                exportFields.add(field.getName());
                colIndex++;
            }
        }

        // 填充数据
        int rowIndex = 1;
        for (T obj : list) {
            Row row = sheet.createRow(rowIndex++);
            colIndex = 0;
            for (String fieldName : exportFields) {
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(obj);

                    Cell cell = row.createCell(colIndex++);
                    if (value != null) {
                        if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else if (value instanceof Date) {
                            cell.setCellValue(DateUtils.format((Date) value));
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }
                } catch (Exception e) {
                    // 忽略异常
                }
            }
        }

        // 自动调整列宽
        for (int i = 0; i < colIndex; i++) {
            sheet.autoSizeColumn(i);
        }

        // 下载
        download(workbook, response, filename);
    }

    /**
     * 导出 Excel（通用）
     */
    public static void exportExcel(List<Map<String, Object>> list, List<String> headers,
                                   List<String> fields, HttpServletResponse response,
                                   String filename) throws IOException {
        Workbook workbook = new SXSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(createHeaderStyle(workbook));
        }

        // 填充数据
        int rowIndex = 1;
        for (Map<String, Object> obj : list) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < fields.size(); i++) {
                Cell cell = row.createCell(i);
                Object value = obj.get(fields.get(i));
                if (value != null) {
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof Date) {
                        cell.setCellValue(DateUtils.format((Date) value));
                    } else {
                        cell.setCellValue(value.toString());
                    }
                }
            }
        }

        // 自动调整列宽
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // 下载
        download(workbook, response, filename);
    }

    /**
     * 创建表头样式
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 创建普通单元格样式
     */
    private static CellStyle createCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 下载 Excel
     */
    private static void download(Workbook workbook, HttpServletResponse response,
                                 String filename) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        filename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + filename + ".xlsx");

        try (OutputStream os = response.getOutputStream()) {
            workbook.write(os);
            os.flush();
        }
        workbook.close();
    }

    /**
     * 导入 Excel（返回列表）
     */
    public static <T> List<T> importExcel(Workbook workbook, Class<T> clazz) throws Exception {
        List<T> result = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(0);

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return result;
        }

        // 解析表头
        Map<Integer, Field> fieldMap = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String header = headerRow.getCell(i).getStringCellValue();
            for (Field field : fields) {
                ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
                if (annotation != null && annotation.header().equals(header)) {
                    field.setAccessible(true);
                    fieldMap.put(i, field);
                    break;
                }
            }
        }

        // 解析数据
        for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            T obj = clazz.newInstance();
            for (Map.Entry<Integer, Field> entry : fieldMap.entrySet()) {
                Cell cell = row.getCell(entry.getKey());
                if (cell != null) {
                    Field field = entry.getValue();
                    Object value = getCellValue(cell, field.getType());
                    if (value != null) {
                        field.set(obj, value);
                    }
                }
            }
            result.add(obj);
        }

        workbook.close();
        return result;
    }

    /**
     * 获取单元格值
     */
    private static Object getCellValue(Cell cell, Class<?> type) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                double num = cell.getNumericCellValue();
                if (type == Integer.class || type == int.class) {
                    return (int) num;
                } else if (type == Long.class || type == long.class) {
                    return (long) num;
                } else if (type == Double.class || type == double.class) {
                    return num;
                } else if (type == Float.class || type == float.class) {
                    return (float) num;
                }
                return num;
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    /**
     * 读取 Excel 为 Map 列表
     */
    public static List<Map<String, Object>> readExcel(Workbook workbook) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(0);

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return result;
        }

        // 解析表头
        List<String> headers = new ArrayList<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            headers.add(cell != null ? cell.getStringCellValue() : "");
        }

        // 解析数据
        for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Map<String, Object> rowData = new HashMap<>();
            for (int j = 0; j < headers.size(); j++) {
                Cell cell = row.getCell(j);
                rowData.put(headers.get(j), getCellValue(cell, Object.class));
            }
            result.add(rowData);
        }

        workbook.close();
        return result;
    }

    /**
     * 合并单元格
     */
    public static void mergeCells(Sheet sheet, int firstRow, int lastRow,
                                  int firstCol, int lastCol) {
        CellRangeAddress range = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
        sheet.addMergedRegion(range);
    }

    /**
     * 创建 Sheet 页
     */
    public static Sheet createSheet(Workbook workbook, String sheetName) {
        return workbook.createSheet(sheetName);
    }

    /**
     * 设置列宽
     */
    public static void setColumnWidth(Sheet sheet, int column, int width) {
        sheet.setColumnWidth(column, width * 256);
    }

    /**
     * 设置行高
     */
    public static void setRowHeight(Row row, short height) {
        row.setHeight(height);
    }

    /**
     * 创建Workbook
     */
    public static Workbook createWorkbook(boolean xlsx) {
        return xlsx ? new SXSSFWorkbook() : new HSSFWorkbook();
    }

    /**
     * 读取 Workbook
     */
    public static Workbook readWorkbook(OutputStream os) throws IOException {
        return new XSSFWorkbook();
    }
}