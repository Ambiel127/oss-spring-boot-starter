package com.mth.oss.spring.boot.autoconfigure.handler;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * object key 默认实现
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.3
 */
public interface DefaultObjectKeyHandler {

    /**
     * 时间戳字符串格式化 yyyyMMddHHmmssSSS
     */
    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * 日期字符串格式化 yyyyMMdd
     */
    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");


    /**
     * 获取默认的完整存储路径
     * <p>
     * 默认前缀为 yyyyMMdd 日期文件夹；
     * 如 file 文件名为 temp.txt，则默认 Object 完整路径为 "yyyyMMdd/temp_yyyyMMddHHmmssSSS.txt"
     *
     * @param file 文件
     * @return 存储对象完整路径
     */
    default String getDefaultObjectKey(File file) {
        // 获取文件名称和扩展名
        String fileName = file.getName();

        String extra = FilenameUtils.getExtension(fileName);
        String name = FilenameUtils.getBaseName(fileName);

        // 路径前缀
        String pathPrefix = LocalDate.now().format(DATE_FORMATTER);

        // 时间戳字符串
        String dateTimeStr = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        // 重新命名后的 Object 完整路径
        return pathPrefix + "/" + name + "_" + dateTimeStr + "." + extra;
    }

}
