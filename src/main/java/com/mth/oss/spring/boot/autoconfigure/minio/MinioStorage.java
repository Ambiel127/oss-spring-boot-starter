package com.mth.oss.spring.boot.autoconfigure.minio;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import com.mth.oss.spring.boot.autoconfigure.service.OssStorage;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 对象存储服务的 minio 实现
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.2
 */
public class MinioStorage implements OssStorage {

    private final OssProperties.Minio ossProperties;

    public MinioStorage(OssProperties.Minio ossProperties) {
        this.ossProperties = ossProperties;
    }

    /**
     * 构建 oss 客户端
     *
     * @return 客户端对象
     */
    public MinioClient getClient() {
        return MinioClient.builder()
                .credentials(ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret())
                .endpoint(ossProperties.getEndpoint())
                .build();
    }


    // ------------------------------------------------------------
    // ----------------------- bucket 管理 ------------------------
    // ------------------------------------------------------------

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



    // ------------------------------------------------------------
    // ----------------------- upload 上传 ------------------------
    // ------------------------------------------------------------

    @Override
    public String upload(File file) {
        return upload(file, null);
    }

    @Override
    public String upload(File file, String path) {
        // 重新命名后的 Object 完整路径
        String objectKey = getDefaultObjectKey(file, path);

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            // 创建 PutObjectArgs 对象
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(ossProperties.getBucketName())
                    .object(objectKey)
                    .stream(fileInputStream, fileInputStream.available(), -1)
                    .build();
            return upload(args);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "an error occurred in oss spring boot starter";
    }

    @Override
    public String upload(InputStream inputStream, String objectKey) {
        PutObjectArgs args;
        try {
            args = PutObjectArgs.builder()
                    .bucket(ossProperties.getBucketName())
                    .object(objectKey)
                    .stream(inputStream, inputStream.available(), -1)
                    .build();
            return upload(args);

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return "an error occurred in oss spring boot starter";
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

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            // 创建 PutObjectArgs 对象
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(ossProperties.getBucketName())
                    .object(objectKey)
                    .stream(fileInputStream, fileInputStream.available(), -1)
                    .build();
            return upload(args);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "an error occurred in oss spring boot starter";
    }

    @Override
    public String generatePresignedUrl(String objectKey) {
        return generatePresignedUrl(objectKey, null);
    }

    @Override
    public String generatePresignedUrl(String objectKey, int duration, TimeUnit unit) {
        return generatePresignedUrl(objectKey, unit.toSeconds(duration));
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
     * @param expiration 签名 url 过期时长，单位秒
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

            // 处理路径前面分隔符 /
            String key = StringUtils.trimLeadingCharacter(objectKey, '/');

            GetPresignedObjectUrlArgs urlArgs = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(ossProperties.getBucketName())
                    .object(key)
                    .expiry(expiry, TimeUnit.SECONDS)
                    .build();

            return minioClient.getPresignedObjectUrl(urlArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "an error occurred in oss spring boot starter";
    }



    // ------------------------------------------------------------
    // ---------------------- download 下载 -----------------------
    // ------------------------------------------------------------

    @Override
    public void download(String objectKey, File file) {
        if (!ossProperties.getEnable()) {
            return;
        }

        MinioClient minioClient = getClient();

        GetObjectArgs args = GetObjectArgs.builder()
                .bucket(ossProperties.getBucketName())
                .object(objectKey)
                .build();

        // 以流的形式下载，再写入到文件
        try (GetObjectResponse response = minioClient.getObject(args);
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {

            IOUtils.copy(response, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // ------------------------------------------------------------
    // ------------------ object manage 文件管理 -------------------
    // ------------------------------------------------------------

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

        // 获取文件元信息
        StatObjectArgs args = StatObjectArgs.builder()
                .bucket(bucketName)
                .object(objectKey)
                .build();

        try {
            StatObjectResponse result = minioClient.statObject(args);
            // 响应不为空
            boolean resultNonNull = Objects.nonNull(result);
            // 响应文件信息与入参相同
            boolean sameObject = Objects.equals(bucketName, result.bucket()) && Objects.equals(objectKey, result.object());

            return resultNonNull && sameObject;
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
    public Iterable<Result<Item>> listObjects() {
        return listObjects(ListObjectsArgs.builder()
                                   .bucket(ossProperties.getBucketName())
                                   .maxKeys(100)
                                   .recursive(true)
                                   .build());
    }

    @Override
    public Iterable<Result<Item>> listObjects(int maxKeys) {
        return listObjects(ListObjectsArgs.builder()
                                   .bucket(ossProperties.getBucketName())
                                   .maxKeys(maxKeys)
                                   .recursive(true)
                                   .build());
    }

    @Override
    public Iterable<Result<Item>> listObjects(String prefix) {
        return listObjects(ListObjectsArgs.builder()
                                   .bucket(ossProperties.getBucketName())
                                   .prefix(prefix)
                                   .recursive(true)
                                   .build());
    }

    @Override
    public Iterable<Result<Item>> listObjects(String prefix, int maxKeys) {
        return listObjects(ListObjectsArgs.builder()
                                   .bucket(ossProperties.getBucketName())
                                   .prefix(prefix)
                                   .maxKeys(maxKeys)
                                   .recursive(true)
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
            return !objectExist(objectKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Iterable<Result<DeleteError>> deleteObjects(List<String> objectKeys) {
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

}
