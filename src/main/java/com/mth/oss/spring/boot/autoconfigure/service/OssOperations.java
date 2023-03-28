package com.mth.oss.spring.boot.autoconfigure.service;

import com.amazonaws.services.s3.model.*;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 对象存储服务接口
 * <p>
 * 包含 bucket管理、存储对象管理、上传、下载、URL 授权访问等常用操作
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.2
 */
public interface OssOperations {

    /**
     * 时间戳字符串格式化 yyyyMMddHHmmssSSS
     */
    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * 日期字符串格式化 yyyyMMdd
     */
    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");


    // ------------------------------------------------------------
    // ----------------------- bucket 管理 ------------------------
    // ------------------------------------------------------------

    /**
     * 列举存储空间
     *
     * @return 桶列表
     */
    List<Bucket> listBuckets();

    /**
     * 判断存储空间是否存在
     *
     * @return 存在true；不存在false
     */
    boolean bucketExist();

    /**
     * 判断存储空间是否存在
     *
     * @param bucketName 桶名称
     * @return 存在true；不存在false
     */
    boolean bucketExist(String bucketName);

    /**
     * 创建存储空间
     *
     * @param bucketName 桶名称
     * @return 是否创建成功，创建成功true；创建失败false
     */
    boolean createBucket(String bucketName);

    /**
     * 删除存储空间
     *
     * @param bucketName 桶名称
     * @return 是否删除成功，删除成功true；删除失败false
     */
    boolean deleteBucket(String bucketName);


    // ------------------------------------------------------------
    // ----------------------- upload 上传 ------------------------
    // ------------------------------------------------------------

    /**
     * 通用上传文件
     * <p>
     * 默认前缀为 yyyyMMdd 日期文件夹；
     * 如 file 文件名为 temp.txt，则默认 Object 完整路径为 "yyyyMMdd/temp_yyyyMMddHHmmssSSS.txt"
     * <p>
     * 场景：通用业务上传，Object 路径默认添加时间戳，避免重名导致文件覆盖
     *
     * @param file 文件
     * @return 存储对象完整路径
     */
    String upload(File file);

    /**
     * 通用上传文件，指定 Object 完整路径
     *
     * @param file      文件
     * @param objectKey Object 完整路径，不能包含Bucket名称，例如 exampleDir/exampleObject.txt
     * @return 存储对象完整路径
     */
    String upload(File file, String objectKey);

    /**
     * 流式上传
     *
     * @param inputStream 流对象
     *                    文件流：new FileInputStream("D:\\path\\exampleFile.txt");
     *                    网络流：new URL("https://www.example.com/").openStream();
     *                    字节数组：new ByteArrayInputStream("Hello OSS".getBytes())
     * @param objectKey   Object 完整路径，不能包含Bucket名称，例如 exampleDir/exampleObject.txt
     * @return 存储对象完整路径
     */
    String upload(InputStream inputStream, String objectKey);


    /**
     * 流式上传
     *
     * @param inputStream 流对象
     *                    文件流：new FileInputStream("D:\\path\\exampleFile.txt");
     *                    网络流：new URL("https://www.example.com/").openStream();
     *                    字节数组：new ByteArrayInputStream("Hello OSS".getBytes())
     * @param objectKey   Object 完整路径，不能包含Bucket名称，例如 exampleDir/exampleObject.txt
     * @return 存储对象完整路径
     */
    String upload(InputStream inputStream, String objectKey, String contentType);

    /**
     * 文件上传，用户可自行组装请求对象
     *
     * @param putObjectRequest 请求对象
     * @return 存储对象完整路径
     */
    String upload(PutObjectRequest putObjectRequest);

    /**
     * 文件上传并替换同名文件
     * <p>
     * 默认 Object 完整路径为 file 文件名
     * <p>
     * 场景：使用手册、说明文件等，需要替换文件但不修改 Object 路径的场景（路径不变、授权访问的 URL 也就不变）
     *
     * @param file 文件
     * @return 存储对象完整路径
     */
    String replaceUpload(File file);

    /**
     * 文件上传并替换同名文件，指定 Object 完整路径
     * <p>
     * 场景：使用手册、说明文件等，需要替换文件但不修改 Object 路径的场景（路径不变、授权访问的 URL 也就不变）
     *
     * @param file      文件
     * @param objectKey Object 完整路径，不能包含Bucket名称，例如 exampleDir/exampleObject.txt
     * @return 存储对象完整路径
     */
    String replaceUpload(File file, String objectKey);

    /**
     * 使用预签名 URL 上传对象
     *
     * @param objectKey Object 完整路径
     * @return 授权上传的 URL 对象
     */
    URL presignedUrlForUpload(String objectKey);

    /**
     * 使用预签名 URL 上传对象
     *
     * @param objectKey Object 完整路径
     * @param duration  链接有效时长
     * @param unit      时间单位
     * @return 授权上传的 URL 对象
     */
    URL presignedUrlForUpload(String objectKey, int duration, TimeUnit unit);


    // ------------------------------------------------------------
    // ---------------------- download 下载 -----------------------
    // ------------------------------------------------------------

    /**
     * 生成签名 URL 授权访问
     * <p>
     * 默认过期时间1小时
     *
     * @param objectKey Object 完整路径
     * @return 授权访问的 URL 对象
     */
    URL presignedUrlForAccess(String objectKey);

    /**
     * 生成签名 URL 授权访问
     *
     * @param objectKey Object 完整路径
     * @param duration  链接有效时长
     * @param unit      时间单位
     * @return 授权访问的 URL 对象
     */
    URL presignedUrlForAccess(String objectKey, int duration, TimeUnit unit);

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
    byte[] download(String objectKey) throws IOException;

    /**
     * 下载到指定输出流
     *
     * @param objectKey    Object 完整路径
     * @param outputStream 输出流
     * @return 下载成功true；否则false
     */
    void download(String objectKey, OutputStream outputStream) throws IOException;


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
     * 判断文件是否存在
     *
     * @param bucketName 存储空间名称
     * @param objectKey  Object完整路径，不能包含Bucket名称
     * @return 存在true；不存在false
     */
    boolean objectExist(String bucketName, String objectKey);

    /**
     * 获取文件
     *
     * @param objectKey Object完整路径，不能包含Bucket名称
     * @return 文件对象
     */
    S3Object getObject(String objectKey);

    /**
     * 获取文件
     *
     * @param bucketName 存储空间名称
     * @param objectKey Object完整路径，不能包含Bucket名称
     * @return 文件对象
     */
    S3Object getObject(String bucketName, String objectKey);

    /**
     * 列举文件
     *
     * @return 集合文件对象
     */
    List<S3ObjectSummary> listObjects();

    /**
     * 列举文件
     *
     * @param maxKeys 最大个数
     * @return 集合文件对象
     */
    List<S3ObjectSummary> listObjects(int maxKeys);

    /**
     * 列举文件
     *
     * @param prefix 指定路径前缀
     * @return 集合文件对象
     */
    List<S3ObjectSummary> listObjects(String prefix);

    /**
     * 列举文件
     *
     * @param prefix  指定路径前缀
     * @param maxKeys 最大个数
     * @return 集合文件对象
     */
    List<S3ObjectSummary> listObjects(String prefix, int maxKeys);

    /**
     * 列举文件
     *
     * @param request 请求对象
     * @return 集合文件对象
     */
    List<S3ObjectSummary> listObjects(ListObjectsV2Request request);

    /**
     * 删除单个文件
     * <p>
     * 如果要删除目录，目录必须为空
     *
     * @param objectKey Object完整路径，不能包含Bucket名称
     * @return 是否删除成功，删除成功true；删除失败false
     */
    boolean deleteObject(String objectKey);

    /**
     * 删除指定的多个文件
     *
     * @param objectKeys Object完整路径集合，不能包含Bucket名称
     * @return 删除失败的文件列表
     */
    List<DeleteObjectsResult.DeletedObject> deleteObjects(List<String> objectKeys);

    /**
     * 拷贝文件
     * <p>
     * 将源Bucket中的文件（Object）复制到同一地域下同一目标Bucket中
     *
     * @param sourceKey      源Object完整路径
     * @param destinationKey 目标Object完整路径
     * @return 是否拷贝成功，拷贝成功true；拷贝失败false
     */
    boolean copyObject(String sourceKey, String destinationKey);

    /**
     * 拷贝文件
     * <p>
     * 将源Bucket中的文件（Object）复制到同一地域下相同或不同目标Bucket中
     *
     * @param sourceBucketName      源存储空间名称
     * @param sourceKey             源Object完整路径
     * @param destinationBucketName 目标存储空间名称
     * @param destinationKey        目标Object完整路径
     * @return 是否拷贝成功，拷贝成功true；拷贝失败false
     */
    boolean copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey);


    // ------------------------------------------------------------
    // ----------------------- 默认方法实现 ------------------------
    // ------------------------------------------------------------

    /**
     * 获取时间戳字符串 yyyyMMddHHmmssSSS
     *
     * @return 时间戳字符串
     */
    default String getDateTimeStr() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    /**
     * 获取默认路径前缀 yyyyMMdd
     *
     * @return 日期字符串
     */
    default String getDefaultPathPrefix() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * 获取默认 object key 完整路径
     * <p>
     * 默认前缀为 yyyyMMdd 日期文件夹；
     * 如 file 文件名为 temp.txt，则默认 Object 完整路径为 "yyyyMMdd/temp_yyyyMMddHHmmssSSS.txt"
     *
     * @param file 文件
     * @return object 完整路径
     */
    default String getDefaultObjectKey(File file) {

        // 获取文件名称和扩展名
        String fileName = file.getName();
        String extra = fileName.substring(fileName.lastIndexOf("."));
        String name = fileName.substring(0, fileName.lastIndexOf("."));

        // 路径前缀
        String pathPrefix = getDefaultPathPrefix();

        // 时间戳字符串
        String dateTimeStr = getDateTimeStr();

        // 重新命名后的 Object 完整路径
        return pathPrefix + "/" + name + "_" + dateTimeStr + extra;
    }

    /**
     * 去除路径前后 / 字符
     *
     * @param path 路径字符串
     * @return string
     */
    default String trimPathCharacter(String path) {
        return StringUtils.trimTrailingCharacter(
                StringUtils.trimLeadingCharacter(path, '/'), '/');
    }


}
