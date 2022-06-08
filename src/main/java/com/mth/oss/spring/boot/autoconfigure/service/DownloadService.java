package com.mth.oss.spring.boot.autoconfigure.service;

import java.io.File;

/**
 * oss 下载文件
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.1
 */
public interface DownloadService {


    /**
     * 下载到指定 File 中
     *
     * @param objectKey Object 完整路径
     * @param file      指定的本地路径，如果指定的本地文件存在会覆盖，不存在则新建。
     */
    void download(String objectKey, File file);

}
