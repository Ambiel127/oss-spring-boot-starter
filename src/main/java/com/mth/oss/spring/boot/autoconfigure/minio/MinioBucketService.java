package com.mth.oss.spring.boot.autoconfigure.minio;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import com.mth.oss.spring.boot.autoconfigure.service.BucketService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import io.minio.messages.Bucket;

import java.util.ArrayList;
import java.util.List;

/**
 * Minio 存储空间 bucket 操作
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.1
 */
public class MinioBucketService implements BucketService {

    private final OssProperties.Minio ossProperties;

    public MinioBucketService(OssProperties.Minio ossProperties) {
        this.ossProperties = ossProperties;
    }


    @Override
    public List<Bucket> listBuckets() {
        if (!ossProperties.getEnable()) {
            return new ArrayList<>();
        }

        MinioClient minioClient = getClient();
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    @Override
    public boolean bucketExist() {
        if (!ossProperties.getEnable()) {
            return false;
        }

        MinioClient minioClient = getClient();
        try {
            BucketExistsArgs args = BucketExistsArgs.builder()
                    .bucket(ossProperties.getBucketName())
                    .build();
            return minioClient.bucketExists(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean bucketExist(String bucketName) {
        if (!ossProperties.getEnable()) {
            return false;
        }

        MinioClient minioClient = getClient();
        try {
            BucketExistsArgs args = BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build();
            return minioClient.bucketExists(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean createBucket(String bucketName) {
        if (!ossProperties.getEnable()) {
            return false;
        }

        MinioClient minioClient = getClient();
        try {
            // 创建存储空间
            MakeBucketArgs args = MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build();
            minioClient.makeBucket(args);

            // 查询是否存在
            BucketExistsArgs existsArgs = BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build();
            return minioClient.bucketExists(existsArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteBucket(String bucketName) {
        if (!ossProperties.getEnable()) {
            return false;
        }

        MinioClient minioClient = getClient();
        try {
            // 删除存储空间
            RemoveBucketArgs args = RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build();
            minioClient.removeBucket(args);

            // 查询是否存在
            BucketExistsArgs existsArgs = BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build();
            return !minioClient.bucketExists(existsArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
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
