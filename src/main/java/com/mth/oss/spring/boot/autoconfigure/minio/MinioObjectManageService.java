package com.mth.oss.spring.boot.autoconfigure.minio;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import com.mth.oss.spring.boot.autoconfigure.service.ObjectManageService;
import io.minio.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Minio oss 管理文件
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.1
 */
public class MinioObjectManageService implements ObjectManageService {

    private final OssProperties.Minio ossProperties;

    public MinioObjectManageService(OssProperties.Minio ossProperties) {
        this.ossProperties = ossProperties;
    }

    @Override
    public boolean objectExist(String objectKey) {
        return objectExist(ossProperties.getBucketName(), objectKey);
    }

    @Override
    public boolean objectExist(String bucketName, String objectKey) {
        if (!ossProperties.getEnable()) {
            return false;
        }

        MinioClient minioClient = getClient();

        // 下载文件
        GetObjectArgs args = GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectKey)
                .build();

        try (GetObjectResponse response = minioClient.getObject(args)) {
            // todo [matianhao] 如何判断存在？
            return Objects.nonNull(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Object getObjectMetadata(String objectKey) {
        if (!ossProperties.getEnable()) {
            return "oss spring boot starter is disable";
        }

        MinioClient minioClient = getClient();

        StatObjectArgs args = StatObjectArgs.builder()
                .bucket(ossProperties.getBucketName())
                .object(objectKey)
                .build();
        try {
            return minioClient.statObject(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "an error occurred in oss spring boot starter";
    }

    @Override
    public Iterable<?> listObjects() {
        return listObjects(ListObjectsArgs.builder()
                                   .bucket(ossProperties.getBucketName())
                                   .build());
    }

    @Override
    public Iterable<?> listObjects(int maxKeys) {
        return listObjects(ListObjectsArgs.builder()
                                   .bucket(ossProperties.getBucketName())
                                   .maxKeys(maxKeys)
                                   .build());
    }

    @Override
    public Iterable<?> listObjects(String prefix) {
        return listObjects(ListObjectsArgs.builder()
                                   .bucket(ossProperties.getBucketName())
                                   .prefix(prefix)
                                   .build());
    }

    @Override
    public Iterable<?> listObjects(String prefix, int maxKeys) {
        return listObjects(ListObjectsArgs.builder()
                                   .bucket(ossProperties.getBucketName())
                                   .prefix(prefix)
                                   .maxKeys(maxKeys)
                                   .build());
    }

    @Override
    public boolean deleteObject(String objectKey) {
        if (!ossProperties.getEnable()) {
            return false;
        }

        MinioClient minioClient = getClient();

        RemoveObjectArgs args = RemoveObjectArgs.builder()
                .bucket(ossProperties.getBucketName())
                .object(objectKey)
                .build();
        try {
            // 删除对象
            minioClient.removeObject(args);

            // 查询是否存在
            return objectExist(objectKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Iterable<?> deleteObjects(List<String> objectKeys) {
        if (!ossProperties.getEnable()) {
            return new ArrayList<>();
        }

        MinioClient minioClient = getClient();

        // 处理objectKeys
        List<DeleteObject> deleteObjectList = objectKeys.stream()
                .map(DeleteObject::new)
                .collect(Collectors.toList());

        RemoveObjectsArgs args = RemoveObjectsArgs.builder()
                .bucket(ossProperties.getBucketName())
                .objects(deleteObjectList)
                .build();
        try {
            return minioClient.removeObjects(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    @Override
    public boolean copyObject(String sourceKey, String destinationKey) {
        CopyObjectArgs args = CopyObjectArgs.builder()
                .bucket(ossProperties.getBucketName())
                .object(destinationKey)
                .source(CopySource.builder()
                                .bucket(ossProperties.getBucketName())
                                .object(sourceKey)
                                .build())
                .build();
        return copyObject(args);
    }

    @Override
    public boolean copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) {
        CopyObjectArgs args = CopyObjectArgs.builder()
                .bucket(destinationBucketName)
                .object(destinationKey)
                .source(CopySource.builder()
                                .bucket(sourceBucketName)
                                .object(sourceKey)
                                .build())
                .build();
        return copyObject(args);
    }

    /**
     * 列举文件
     *
     * @param listObjectsArgs 请求对象
     * @return 集合文件对象
     */
    public Iterable<Result<Item>> listObjects(ListObjectsArgs listObjectsArgs) {
        if (!ossProperties.getEnable()) {
            return new ArrayList<>();
        }

        MinioClient minioClient = getClient();
        try {
            return minioClient.listObjects(listObjectsArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * 拷贝文件
     * <p>
     * 将源Bucket中的文件（Object）复制到同一地域下相同或不同目标Bucket中
     *
     * @param args 请求对象
     * @return 是否拷贝成功，拷贝成功true；拷贝失败false
     */
    public boolean copyObject(CopyObjectArgs args) {
        if (!ossProperties.getEnable()) {
            return false;
        }

        MinioClient minioClient = getClient();
        try {
            // 拷贝对象
            minioClient.copyObject(args);

            // 查询拷贝后对象是否存在
            return objectExist(args.bucket(), args.object());
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
