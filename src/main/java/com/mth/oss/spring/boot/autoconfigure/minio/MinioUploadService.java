package com.mth.oss.spring.boot.autoconfigure.minio;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import com.mth.oss.spring.boot.autoconfigure.service.UploadService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Minio oss 上传文件、生成签名 URL 授权访问
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.1
 */
public class MinioUploadService implements UploadService {

    private final OssProperties.Minio ossProperties;

    private final MinioBucketService minioBucketService;

    public MinioUploadService(OssProperties.Minio ossProperties, MinioBucketService aliyunBucketService) {
        this.ossProperties = ossProperties;
        this.minioBucketService = aliyunBucketService;
    }

    @Override
    public String upload(File file) {
        return upload(file, null);
    }

    @Override
    public String upload(File file, String path) {
        // 重新命名后的 Object 完整路径
        String objectKey = getDefaultObjectKey(file, path);

        // 创建 PutObjectArgs 对象
        PutObjectArgs args = null;

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            args = PutObjectArgs.builder()
                    .bucket(ossProperties.getBucketName())
                    .object(objectKey)
                    .stream(fileInputStream, fileInputStream.available(), -1)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return upload(args);
    }

    @Override
    public String upload(InputStream inputStream, String objectKey) {
        PutObjectArgs args = null;
        try {
            args = PutObjectArgs.builder()
                    .bucket(ossProperties.getBucketName())
                    .object(objectKey)
                    .stream(inputStream, inputStream.available(), -1)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return upload(args);
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

        // 创建 PutObjectArgs 对象
        PutObjectArgs args = null;

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            args = PutObjectArgs.builder()
                    .bucket(ossProperties.getBucketName())
                    .object(objectKey)
                    .stream(fileInputStream, fileInputStream.available(), -1)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return upload(args);
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
     * @param putObjectArgs 请求对象
     * @return Object 完整路径
     */
    public String upload(PutObjectArgs putObjectArgs) {
        if (!ossProperties.getEnable()) {
            return null;
        }

        MinioClient minioClient = getClient();
        try {
            // 判断 bucket 是否存在
            if (!minioBucketService.bucketExist()) {
                minioBucketService.createBucket(ossProperties.getBucketName());
            }

            // 上传文件
            minioClient.putObject(putObjectArgs);

            return putObjectArgs.object();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "an error occurred in oss spring boot starter";
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
        int expiry = Objects.isNull(expiration) ? ossProperties.getExpiration() : expiration.intValue();

        MinioClient minioClient = getClient();
        try {
            // 判断 bucket 是否存在
            if (!minioBucketService.bucketExist()) {
                minioBucketService.createBucket(ossProperties.getBucketName());
            }

            // 处理路径前面分隔符 /
            String key = StringUtils.trimLeadingCharacter(objectKey, '/');

            GetPresignedObjectUrlArgs urlArgs = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(ossProperties.getBucketName())
                    .object(key)
                    .expiry(expiry, TimeUnit.MINUTES)
                    .build();

            return minioClient.getPresignedObjectUrl(urlArgs);
        } catch (Exception e){
            e.printStackTrace();
        }

        return "an error occurred in oss spring boot starter";
    }

    /**
     * 构建 oss 客户端
     */
    private MinioClient getClient() {
        return MinioClient.builder()
                .credentials(ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret())
                .endpoint(ossProperties.getEndpoint())
                .build();
    }
}
