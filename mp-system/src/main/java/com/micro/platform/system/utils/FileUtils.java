package com.micro.platform.system.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件处理工具类
 */
public class FileUtils {

    private static final String[] FILE_SIZE_UNITS = {"B", "KB", "MB", "GB", "TB"};

    /**
     * 格式化文件大小
     *
     * @param size 字节数
     * @return 格式化后的大小字符串
     */
    public static String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups))
                + " " + FILE_SIZE_UNITS[digitGroups];
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（不含点）
     */
    public static String getExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase();
    }

    /**
     * 获取文件名（不含扩展名）
     *
     * @param filename 文件名
     * @return 不含扩展名的文件名
     */
    public static String getFileNameWithoutExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0) {
            return filename;
        }
        return filename.substring(0, lastDot);
    }

    /**
     * 检查文件是否存在
     *
     * @param path 文件路径
     * @return 是否存在
     */
    public static boolean exists(String path) {
        return Files.exists(Paths.get(path));
    }

    /**
     * 创建目录（如果不存在）
     *
     * @param dir 目录路径
     * @return 是否成功
     */
    public static boolean createDir(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return true;
    }

    /**
     * 删除文件
     *
     * @param path 文件路径
     * @return 是否成功
     */
    public static boolean deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    /**
     * 删除目录（递归）
     *
     * @param dir 目录路径
     * @return 是否成功
     */
    public static boolean deleteDir(String dir) {
        File directory = new File(dir);
        if (!directory.exists()) {
            return true;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDir(file.getAbsolutePath());
                } else {
                    file.delete();
                }
            }
        }
        return directory.delete();
    }

    /**
     * 读取文件内容
     *
     * @param path 文件路径
     * @return 文件内容
     */
    public static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    /**
     * 写入文件
     *
     * @param path    文件路径
     * @param content 文件内容
     */
    public static void writeFile(String path, String content) throws IOException {
        Files.write(Paths.get(path), content.getBytes());
    }

    /**
     * 复制文件
     *
     * @param source 源文件
     * @param target 目标文件
     */
    public static void copyFile(String source, String target) throws IOException {
        Files.copy(Paths.get(source), Paths.get(target), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 移动文件
     *
     * @param source 源文件
     * @param target 目标文件
     */
    public static void moveFile(String source, String target) throws IOException {
        Files.move(Paths.get(source), Paths.get(target), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 文件重命名
     *
     * @param path    文件路径
     * @param newName 新文件名
     * @return 是否成功
     */
    public static boolean renameFile(String path, String newName) {
        File oldFile = new File(path);
        File newFile = new File(oldFile.getParent(), newName);
        return oldFile.renameTo(newFile);
    }

    /**
     * 压缩文件为 ZIP
     *
     * @param sourceFile 源文件
     * @param zipFile    ZIP 文件
     */
    public static void zipFile(String sourceFile, String zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
             FileInputStream fis = new FileInputStream(sourceFile)) {

            ZipEntry entry = new ZipEntry(new File(sourceFile).getName());
            zos.putNextEntry(entry);

            byte[] buffer = new byte[4096];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
        }
    }

    /**
     * 压缩目录为 ZIP
     *
     * @param sourceDir 源目录
     * @param zipFile   ZIP 文件
     */
    public static void zipDir(String sourceDir, String zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            zipDirRecursive(sourceDir, zos, "");
        }
    }

    private static void zipDirRecursive(String dir, ZipOutputStream zos, String path) throws IOException {
        File directory = new File(dir);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                String entryPath = path + file.getName();
                if (file.isDirectory()) {
                    zipDirRecursive(file.getAbsolutePath(), zos, entryPath + "/");
                } else {
                    ZipEntry entry = new ZipEntry(entryPath);
                    zos.putNextEntry(entry);

                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    /**
     * 获取文件最后修改时间
     *
     * @param path 文件路径
     * @return 最后修改时间戳
     */
    public static long getLastModifiedTime(String path) {
        return new File(path).lastModified();
    }

    /**
     * 检查是否是文本文件
     *
     * @param path 文件路径
     * @return 是否是文本文件
     */
    public static boolean isTextFile(String path) {
        String ext = getExtension(path);
        return "txt".equals(ext) || "log".equals(ext) || "md".equals(ext) ||
                "java".equals(ext) || "xml".equals(ext) || "json".equals(ext) ||
                "yml".equals(ext) || "yaml".equals(ext) || "properties".equals(ext);
    }
}