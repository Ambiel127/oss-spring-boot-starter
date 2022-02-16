package com.mth.oss.spring.boot.autoconfigure.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * oss 上传文件、生成签名 URL 授权访问
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.0
 */
public class OssUploadService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OssProperties ossProperties;

    private final OssBucketService ossBucketService;

    public OssUploadService(OssProperties ossProperties, OssBucketService ossBucketService) {
        this.ossProperties = ossProperties;
        this.ossBucketService = ossBucketService;
    }


    /**
     * 上传文件
     * <p>
     * 根方法，其余封装方法最终都是调用此方法，如其他方法不适用业务，可自行组装请求对象调用此方法
     *
     * @param putObjectRequest 请求对象
     * @param urlExpiration    签名 url 过期时间
     * @return 授权访问 URL 对象
     */
    public URL upload(PutObjectRequest putObjectRequest, Long urlExpiration) {
        if (!ossProperties.getEnable()) {
            return null;
        }

        OSS ossClient = getClient();
        try {
            // 判断 bucket 是否存在
            if (!ossBucketService.bucketExist()) {
                ossBucketService.createBucket(ossProperties.getBucketName());
            }

            // 上传文件
            ossClient.putObject(putObjectRequest);

            // 生成授权访问 URL
            return generatePresignedUrl(putObjectRequest.getKey(), urlExpiration);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 文件上传并覆盖同名文件
     * <p>
     * 默认 Object 完整路径为 file 文件名
     * 场景：使用手册、说明文件等，需要替换文件但不修改 Object 路径的场景（路径不变、授权访问的 URL 也就不变）
     *
     * @param file          文件
     * @param urlExpiration 签名 url 过期时间
     * @return 授权访问 URL 对象
     */
    public URL uploadAndOverwrite(File file, Long urlExpiration) {
        return uploadAndOverwrite(file, null, urlExpiration);
    }

    /**
     * 指定路径，文件上传并覆盖同名文件
     * <p>
     * 如 path 为 dir，file 文件名为 temp.txt，则默认 Object 完整路径为 dir/temp.txt
     * 场景：使用手册、说明文件等，需要替换文件但不修改 Object 路径的场景（路径不变、授权访问的 URL 也就不变）
     *
     * @param file          文件
     * @param path          Object 路径前缀
     * @param urlExpiration 签名 url 过期时间
     * @return 授权访问 URL 对象
     */
    public URL uploadAndOverwrite(File file, String path, Long urlExpiration) {
        // 没有路径前缀
        String objectKey = file.getName();

        // 存在路径前缀
        if (Objects.nonNull(path)) {
            // 处理路径前后分隔符 /
            String trimPathPrefix = StringUtils.trimTrailingCharacter(
                    StringUtils.trimLeadingCharacter(path, '/'), '/');
            objectKey = trimPathPrefix + "/" + file.getName();
        }

        // 创建 PutObjectRequest 对象
        PutObjectRequest putObjectRequest = new PutObjectRequest(ossProperties.getBucketName(), objectKey, file);

        return upload(putObjectRequest, urlExpiration);
    }

    /**
     * 通用文件上传
     * <p>
     * 如 file 文件名为 temp.txt，则默认 Object 完整路径为 "temp_yyyyMMddHHmmssSSS.txt"
     * 场景：通用业务上传，Object 路径默认添加时间戳，避免重名导致文件覆盖
     *
     * @param file          文件
     * @param urlExpiration 签名 url 过期时间
     * @return 授权访问 URL 对象
     */
    public URL upload(File file, Long urlExpiration) {
        return upload(file, null, urlExpiration);
    }

    /**
     * 通用文件上传
     * <p>
     * - 如 file 文件名为 temp.txt，则默认 Object 完整路径为 "temp_yyyyMMddHHmmssSSS.txt"
     * - path 路径为 null 时，前缀默认为 yyyyMMdd 日期文件夹
     * 场景：通用业务上传，Object 路径默认添加时间戳，避免重名导致文件覆盖
     *
     * @param file          文件
     * @param path          Object 路径前缀
     * @param urlExpiration 签名 url 过期时间
     * @return 授权访问 URL 对象
     */
    public URL upload(File file, String path, Long urlExpiration) {

        // 获取文件名称和扩展名
        String fileName = file.getName();
        String extra = fileName.substring(fileName.lastIndexOf("."));
        String name = fileName.substring(0, fileName.lastIndexOf("."));

        // 路径前缀
        String trimPathPrefix = getDefaultPathPrefix();
        if (Objects.nonNull(path)) {
            // 去除路径前后 / 字符
            trimPathPrefix = StringUtils.trimTrailingCharacter(
                    StringUtils.trimLeadingCharacter(path, '/'), '/');
        }

        // 时间戳字符串
        String dateTimeStr = getDateTimeStr();

        // 重新命名后的 Object 完整路径
        String objectKey = trimPathPrefix + "/" + name + "_" + dateTimeStr + extra;

        // 创建 PutObjectRequest 对象
        PutObjectRequest putObjectRequest = new PutObjectRequest(ossProperties.getBucketName(), objectKey, file);

        return upload(putObjectRequest, urlExpiration);
    }

    /**
     * 流式上传
     *
     * @param inputStream   流对象
     *                      文件流：new FileInputStream("D:\\localpath\\examplefile.txt");
     *                      网络流：new URL("https://www.aliyun.com/").openStream();
     *                      字节数组：new ByteArrayInputStream("Hello OSS".getBytes())
     * @param key           Object 完整路径
     * @param urlExpiration 签名 url 过期时间
     * @return 授权访问 URL 对象
     */
    public URL upload(InputStream inputStream, String key, Long urlExpiration) {

        // 创建 PutObjectRequest 对象
        PutObjectRequest putObjectRequest = new PutObjectRequest(ossProperties.getBucketName(), key, inputStream);

        return upload(putObjectRequest, urlExpiration);
    }

    // todo [matianhao] 进度条，前端需要何种方式  {@link https://help.aliyun.com/document_detail/84796.html}

    /**
     * 生成签名 URL 授权访问
     * <p>
     * 默认过期时间 100 年
     *
     * @param key Object 完整路径
     * @return 授权访问 URL 对象
     */
    public URL generatePresignedUrl(String key) {
        return generatePresignedUrl(key, (Long) null);
    }

    /**
     * 生成签名 URL 授权访问
     *
     * @param key        Object 完整路径
     * @param expiration 签名 url 过期时间
     * @return 授权访问 URL 对象
     */
    public URL generatePresignedUrl(String key, Long expiration) {
        // 过期时间为空，则默认 100 年 (｀・ω・´)
        return generatePresignedUrl(key, Objects.isNull(expiration) ?
                new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 100) : new Date(expiration));
    }

    /**
     * 生成签名 URL 授权访问
     *
     * @param key        Object 完整路径
     * @param expiration 签名 url 过期时间
     * @return 授权访问 URL 对象
     */
    public URL generatePresignedUrl(String key, Date expiration) {
        if (!ossProperties.getEnable()) {
            return null;
        }

        OSS ossClient = getClient();
        try {
            // 判断 bucket 是否存在
            if (!ossBucketService.bucketExist()) {
                ossBucketService.createBucket(ossProperties.getBucketName());
            }

            // 处理路径前面分隔符 /
            String objectKey = StringUtils.trimLeadingCharacter(key, '/');

            return ossClient.generatePresignedUrl(ossProperties.getBucketName(), objectKey, expiration);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 获取时间戳字符串 yyyyMMddHHmmssSSS
     */
    private String getDateTimeStr() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    /**
     * 获取默认路径前缀 yyyyMMdd
     */
    private String getDefaultPathPrefix() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * 构建 oss 客户端
     */
    private OSS getClient() {
        return new OSSClientBuilder()
                .build(ossProperties.getEndpoint(),
                       ossProperties.getAccessKeyId(),
                       ossProperties.getAccessKeySecret());
    }

}
