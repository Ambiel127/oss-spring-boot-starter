package com.mth.oss.spring.boot.autoconfigure.aliyun;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import com.mth.oss.spring.boot.autoconfigure.service.UploadService;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Aliyun oss 上传文件、生成签名 URL 授权访问
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.0
 */
public class AliyunUploadService implements UploadService {

    private final OssProperties.Aliyun ossProperties;

    private final AliyunBucketService aliyunBucketService;

    public AliyunUploadService(OssProperties.Aliyun ossProperties, AliyunBucketService aliyunBucketService) {
        this.ossProperties = ossProperties;
        this.aliyunBucketService = aliyunBucketService;
    }

    @Override
    public String upload(File file) {
        return upload(file, null);
    }

    @Override
    public String upload(File file, String path) {
        // 重新命名后的 Object 完整路径
        String objectKey = getDefaultObjectKey(file, path);

        // 创建 PutObjectRequest 对象
        PutObjectRequest putObjectRequest = new PutObjectRequest(ossProperties.getBucketName(), objectKey, file);

        return upload(putObjectRequest);
    }

    @Override
    public String upload(InputStream inputStream, String objectKey) {
        // 创建 PutObjectRequest 对象
        PutObjectRequest putObjectRequest = new PutObjectRequest(ossProperties.getBucketName(), objectKey, inputStream);

        return upload(putObjectRequest);
    }

    @Override
    public String uploadAndOverwrite(File file) {
        return uploadAndOverwrite(file, null);
    }

    @Override
    public String uploadAndOverwrite(File file, String path) {
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

        return upload(putObjectRequest);
    }

    @Override
    public String generatePresignedUrl(String objectKey) {
        return generatePresignedUrl(objectKey, null);
    }

    @Override
    public String generatePresignedUrl(String objectKey, int duration, TimeUnit unit) {
        return generatePresignedUrl(objectKey, unit.toMinutes(duration));
    }

    /**
     * 上传文件
     * <p>
     * 其余上传的封装方法最终都是调用此方法，如其他方法不适用业务，可自行组装请求对象调用此方法
     *
     * @param putObjectRequest 请求对象
     * @return Object 完整路径
     */
    public String upload(PutObjectRequest putObjectRequest) {
        if (!ossProperties.getEnable()) {
            return null;
        }

        OSS ossClient = getClient();
        try {
            // 判断 bucket 是否存在
            if (!aliyunBucketService.bucketExist()) {
                aliyunBucketService.createBucket(ossProperties.getBucketName());
            }

            // 上传文件
            ossClient.putObject(putObjectRequest);

            return putObjectRequest.getKey();
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 生成签名 URL 授权访问
     *
     * @param objectKey  Object 完整路径
     * @param expiration 签名 url 过期时长，单位分钟
     * @return 授权访问 URL 对象
     */
    public String generatePresignedUrl(String objectKey, Long expiration) {
        if (!ossProperties.getEnable()) {
            return null;
        }

        // 过期时间为空，则默认1小时
        long expiry = Objects.isNull(expiration) ? ossProperties.getExpiration() : expiration;
        // 转换为毫秒
        long expiryMillis = TimeUnit.MINUTES.toMillis(expiry);

        // 过期date
        Date expirationDate = new Date(System.currentTimeMillis() + expiryMillis);

        OSS ossClient = getClient();
        try {
            // 判断 bucket 是否存在
            if (!aliyunBucketService.bucketExist()) {
                aliyunBucketService.createBucket(ossProperties.getBucketName());
            }

            // 处理路径前面分隔符 /
            String key = StringUtils.trimLeadingCharacter(objectKey, '/');

            URL url = ossClient.generatePresignedUrl(ossProperties.getBucketName(), key, expirationDate);
            return url.toString();
        } finally {
            ossClient.shutdown();
        }

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
