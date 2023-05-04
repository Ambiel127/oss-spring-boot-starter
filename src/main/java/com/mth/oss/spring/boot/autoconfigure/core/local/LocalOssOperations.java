package com.mth.oss.spring.boot.autoconfigure.core.local;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 本地对象存储服务操作
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.3
 */
public interface LocalOssOperations {

    // ------------------------------------------------------------
    // ----------------------- upload 上传 ------------------------
    // ------------------------------------------------------------

    /**
     * 通用上传文件
     * <p>
     * 默认前缀为 yyyyMMdd 日期文件夹；
     * 如 file 文件名为 temp.txt，则默认 Object 完整路径为 "yyyyMMdd/temp_yyyyMMddHHmmssSSS.txt"
     * <p>
     * 场景：通用业务上传，文件名默认添加时间戳，避免重名导致文件覆盖
     *
     * @param file 文件
     * @return 存储对象相对路径
     */
    String upload(File file);

    /**
     * 通用上传文件，指定 Object 完整路径
     *
     * @param file      文件
     * @param objectKey Object 完整路径，例如 exampleDir/exampleObject.txt
     * @return 存储对象相对路径
     */
    String upload(File file, String objectKey);

    /**
     * 流式上传
     *
     * @param inputStream 流对象
     *                    文件流：new FileInputStream("D:\\path\\exampleFile.txt");
     *                    网络流：new URL("https://www.example.com/").openStream();
     *                    字节数组：new ByteArrayInputStream("Hello OSS".getBytes())
     * @param objectKey   Object 完整路径，例如 exampleDir/exampleObject.txt
     * @return 存储对象相对路径
     */
    String upload(InputStream inputStream, String objectKey);

    /**
     * 文件上传并替换同名文件
     * <p>
     * 默认路径名为 file 文件名
     * <p>
     * 场景：使用手册、说明文件等，需要替换文件但不修改文件路径的场景（路径不变，访问的地址也就不变）
     *
     * @param file 文件
     * @return 存储对象相对路径
     */
    String replaceUpload(File file);

    /**
     * 文件上传并替换同名文件，指定路径名
     * <p>
     * 场景：使用手册、说明文件等，需要替换文件但不修改文件路径的场景（路径不变，访问的地址也就不变）
     *
     * @param file      文件
     * @param objectKey Object 完整路径，例如 exampleDir/exampleObject.txt
     * @return 存储对象相对路径
     */
    String replaceUpload(File file, String objectKey);


    // ------------------------------------------------------------
    // ---------------------- download 下载 -----------------------
    // ------------------------------------------------------------

    /**
     * 下载到指定 File 中
     *
     * @param objectKey    Object 完整路径
     * @param fullFilePath 指定下载文件的路径，如果本地存在该文件会覆盖，不存在则新建。
     * @return 下载成功true；否则false
     */
    boolean download(String objectKey, String fullFilePath);

    /**
     * 下载到指定 File 中
     *
     * @param objectKey Object 完整路径
     * @param file      指定下载的文件，如果本地存在该文件会覆盖，不存在则新建。
     * @return 下载成功true；否则false
     */
    boolean download(String objectKey, File file);

    /**
     * 下载 byte 数组
     *
     * @param objectKey Object 完整路径
     * @return byte数组
     */
    byte[] download(String objectKey);

    /**
     * 下载到指定输出流
     *
     * @param objectKey    Object 完整路径
     * @param outputStream 输出流
     */
    void download(String objectKey, OutputStream outputStream);


    // ------------------------------------------------------------
    // ------------------ object manage 文件管理 -------------------
    // ------------------------------------------------------------

    /**
     * 判断文件是否存在
     *
     * @param objectKey Object完整路径，不能包含Bucket名称
     * @return 存在true；不存在false
     */
    boolean objectExist(String objectKey);

    /**
     * 获取文件
     *
     * @param objectKey Object完整路径，不能包含Bucket名称
     * @return 文件对象
     */
    File getObject(String objectKey);


}
