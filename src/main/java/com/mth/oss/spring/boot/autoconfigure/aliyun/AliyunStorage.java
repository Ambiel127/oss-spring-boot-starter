package com.mth.oss.spring.boot.autoconfigure.aliyun;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.LogUtils;
import com.aliyun.oss.model.*;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import com.mth.oss.spring.boot.autoconfigure.service.OssStorage;
import org.apache.commons.logging.Log;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.aliyun.oss.internal.OSSConstants.URL_ENCODING;

/**
 * 对象存储服务的 aliyun oss 实现
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.2
 */
public class AliyunStorage implements OssStorage {

    private final OssProperties.Aliyun ossProperties;

    public AliyunStorage(OssProperties.Aliyun ossProperties) {
        this.ossProperties = ossProperties;
    }

    /**
     * 构建 oss 客户端
     *
     * @return 客户端对象
     */
    public OSS getClient() {
        return new OSSClientBuilder()
                .build(ossProperties.getEndpoint(),
                       ossProperties.getAccessKeyId(),
                       ossProperties.getAccessKeySecret());
    }


    // ------------------------------------------------------------
    // ----------------------- bucket 管理 ------------------------
    // ------------------------------------------------------------

    @Override
    public List<Bucket> listBuckets() {
        if (!ossProperties.getEnable()) {
            return new ArrayList<>();
        }

        OSS ossClient = getClient();
        try {
            return ossClient.listBuckets();
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public boolean bucketExist() {
        if (!ossProperties.getEnable()) {
            return false;
        }

        OSS ossClient = getClient();
        try {
            return ossClient.doesBucketExist(ossProperties.getBucketName());
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public boolean bucketExist(String bucketName) {
        if (!ossProperties.getEnable()) {
            return false;
        }

        OSS ossClient = getClient();
        try {
            return ossClient.doesBucketExist(bucketName);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public boolean createBucket(String bucketName) {
        if (!ossProperties.getEnable()) {
            return false;
        }

        OSS ossClient = getClient();
        try {
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
            createBucketRequest.setStorageClass(StorageClass.Standard);
            createBucketRequest.setCannedACL(CannedAccessControlList.Private);
            createBucketRequest.setLogEnabled(true);
            // 创建存储空间。
            ossClient.createBucket(createBucketRequest);

            return ossClient.doesBucketExist(bucketName);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public boolean deleteBucket(String bucketName) {
        if (!ossProperties.getEnable()) {
            return false;
        }

        OSS ossClient = getClient();
        try {
            ossClient.deleteBucket(bucketName);

            return !ossClient.doesBucketExist(bucketName);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 创建存储空间
     *
     * @param createBucketRequest 请求对象
     * @return 是否创建成功，创建成功true；创建失败false
     */
    public boolean createBucket(CreateBucketRequest createBucketRequest) {
        if (!ossProperties.getEnable()) {
            return false;
        }

        OSS ossClient = getClient();
        try {
            // 创建存储空间。
            ossClient.createBucket(createBucketRequest);

            return ossClient.doesBucketExist(createBucketRequest.getBucketName());
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 获取存储空间地域
     *
     * @param bucketName 桶名称
     * @return 地域字符串
     */
    public String getBucketLocation(String bucketName) {
        if (!ossProperties.getEnable()) {
            return "oss spring boot starter is disable";
        }

        OSS ossClient = getClient();
        try {
            return ossClient.getBucketLocation(bucketName);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 获取存储空间信息
     *
     * @param bucketName 桶名称
     * @return 桶信息对象
     */
    public BucketInfo getBucketInfo(String bucketName) {
        if (!ossProperties.getEnable()) {
            return new BucketInfo();
        }

        OSS ossClient = getClient();
        try {
            return ossClient.getBucketInfo(bucketName);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 管理存储空间访问权限
     *
     * @param bucketName 桶名称
     * @param access     访问控制权限
     * @return 当前访问控制权限信息
     */
    public AccessControlList setBucketAcl(String bucketName, CannedAccessControlList access) {
        if (!ossProperties.getEnable()) {
            return new AccessControlList();
        }

        OSS ossClient = getClient();
        try {
            ossClient.setBucketAcl(bucketName, access);
            return ossClient.getBucketAcl(bucketName);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 管理存储空间访问权限
     *
     * @param bucketName 桶名称
     * @return 当前访问控制权限信息
     */
    public AccessControlList getBucketAcl(String bucketName) {
        if (!ossProperties.getEnable()) {
            return new AccessControlList();
        }

        OSS ossClient = getClient();
        try {
            return ossClient.getBucketAcl(bucketName);
        } finally {
            ossClient.shutdown();
        }
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
        return generatePresignedUrl(objectKey, unit.toSeconds(duration));
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
     * @param expiration 签名 url 过期时长，单位秒
     * @return 授权访问 URL 对象
     */
    public String generatePresignedUrl(String objectKey, Long expiration) {
        if (!ossProperties.getEnable()) {
            return null;
        }

        // 过期时间为空，则默认1小时
        long expiry = Objects.isNull(expiration) ? ossProperties.getExpiration() : expiration;
        // 转换为毫秒
        long expiryMillis = TimeUnit.SECONDS.toMillis(expiry);

        // 过期date
        Date expirationDate = new Date(System.currentTimeMillis() + expiryMillis);

        OSS ossClient = getClient();
        try {

            // 处理路径前面分隔符 /
            String key = StringUtils.trimLeadingCharacter(objectKey, '/');

            URL url = ossClient.generatePresignedUrl(ossProperties.getBucketName(), key, expirationDate);
            return url.toString();
        } finally {
            ossClient.shutdown();
        }

    }



    // ------------------------------------------------------------
    // ---------------------- download 下载 -----------------------
    // ------------------------------------------------------------

    @Override
    public void download(String objectKey, File file) {
        if (!ossProperties.getEnable()) {
            return;
        }

        OSS ossClient = getClient();
        try {
            ossClient.getObject(new GetObjectRequest(ossProperties.getBucketName(), objectKey), file);
        } finally {
            ossClient.shutdown();
        }
    }



    // ------------------------------------------------------------
    // ------------------ object manage 文件管理 -------------------
    // ------------------------------------------------------------

    @Override
    public boolean objectExist(String objectKey) {
        if (!ossProperties.getEnable()) {
            return false;
        }

        OSS ossClient = getClient();
        try {
            return ossClient.doesObjectExist(ossProperties.getBucketName(), objectKey);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public boolean objectExist(String bucketName, String objectKey) {
        if (!ossProperties.getEnable()) {
            return false;
        }

        OSS ossClient = getClient();
        try {
            return ossClient.doesObjectExist(bucketName, objectKey);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public ObjectMetadata getObjectMetadata(String objectKey) {
        if (!ossProperties.getEnable()) {
            return new ObjectMetadata();
        }

        OSS ossClient = getClient();
        try {
            return ossClient.getObjectMetadata(ossProperties.getBucketName(), objectKey);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public Iterable<OSSObjectSummary> listObjects() {
        return listObjects(new ListObjectsRequest(ossProperties.getBucketName(), null, null, null, null));
    }

    @Override
    public Iterable<OSSObjectSummary> listObjects(int maxKeys) {
        return listObjects(new ListObjectsRequest(ossProperties.getBucketName(), null, null, null, maxKeys));
    }

    @Override
    public Iterable<OSSObjectSummary> listObjects(String prefix) {
        String trimPathPrefix = prefix;

        // 处理路径前后分隔符 /
        if (Objects.nonNull(prefix)) {
            trimPathPrefix = StringUtils.trimTrailingCharacter(
                    StringUtils.trimLeadingCharacter(prefix, '/'), '/');
        }
        return listObjects(new ListObjectsRequest(ossProperties.getBucketName(), trimPathPrefix, null, null, null));
    }

    @Override
    public Iterable<OSSObjectSummary> listObjects(String prefix, int maxKeys) {
        String trimPathPrefix = prefix;

        // 处理路径前后分隔符 /
        if (Objects.nonNull(prefix)) {
            trimPathPrefix = StringUtils.trimTrailingCharacter(
                    StringUtils.trimLeadingCharacter(prefix, '/'), '/');
        }
        return listObjects(new ListObjectsRequest(ossProperties.getBucketName(), trimPathPrefix, null, null, maxKeys));
    }

    @Override
    public boolean deleteObject(String objectKey) {
        if (!ossProperties.getEnable()) {
            return false;
        }

        OSS ossClient = getClient();
        try {
            ossClient.deleteObject(ossProperties.getBucketName(), objectKey);

            return !ossClient.doesObjectExist(ossProperties.getBucketName(), objectKey);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public Iterable<String> deleteObjects(List<String> objectKeys) {
        if (!ossProperties.getEnable()) {
            return new ArrayList<>();
        }

        OSS ossClient = getClient();
        try {
            DeleteObjectsResult deleteResult = ossClient.deleteObjects(
                    new DeleteObjectsRequest(ossProperties.getBucketName())
                            .withQuiet(true)
                            .withKeys(objectKeys)
                            .withEncodingType(URL_ENCODING));
            // 删除失败的文件列表
            return deleteResult.getDeletedObjects();
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public boolean copyObject(String sourceKey, String destinationKey) {
        // 创建CopyObjectRequest对象。
        CopyObjectRequest request = new CopyObjectRequest(ossProperties.getBucketName(), sourceKey,
                                                          ossProperties.getBucketName(), destinationKey);
        return copyObject(request);
    }

    @Override
    public boolean copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) {
        // 创建CopyObjectRequest对象。
        CopyObjectRequest request = new CopyObjectRequest(sourceBucketName, sourceKey,
                                                          destinationBucketName, destinationKey);
        return copyObject(request);
    }

    /**
     * 列举文件
     *
     * @param listObjectsRequest 请求对象
     * @return 集合文件对象
     */
    public Iterable<OSSObjectSummary> listObjects(ListObjectsRequest listObjectsRequest) {
        if (!ossProperties.getEnable()) {
            return new ArrayList<>();
        }

        OSS ossClient = getClient();
        try {
            ObjectListing objectList = ossClient.listObjects(listObjectsRequest);
            return objectList.getObjectSummaries();
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 删除指定目录及目录下的文件
     * <p>
     * 警告！！如果以下示例代码中前缀prefix的值为空字符串或者NULL，将会删除整个Bucket内的所有文件，请谨慎使用。
     *
     * @param prefix 指定前缀
     * @return 删除失败的文件列表
     */
    public List<String> deleteObjects(String prefix) {
        if (!ossProperties.getEnable()) {
            return new ArrayList<>();
        }

        // 日志对象
        Log log = LogUtils.getLog();
        List<String> result = new ArrayList<>();

        OSS ossClient = getClient();
        String nextMarker = null;
        ObjectListing objectListing;

        // 处理路径前后分隔符 /
        String trimPathPrefix = prefix;
        if (Objects.nonNull(prefix)) {
            trimPathPrefix = StringUtils.trimTrailingCharacter(
                    StringUtils.trimLeadingCharacter(prefix, '/'), '/');
        }
        try {
            do {
                // 查询指定目录下的文件
                ListObjectsRequest listObjectsRequest = new ListObjectsRequest(ossProperties.getBucketName())
                        .withPrefix(trimPathPrefix)
                        .withMarker(nextMarker);
                objectListing = ossClient.listObjects(listObjectsRequest);

                if (objectListing.getObjectSummaries().size() > 0) {
                    List<String> keys = new ArrayList<>();
                    for (OSSObjectSummary s : objectListing.getObjectSummaries()) {
                        keys.add(s.getKey());
                        log.info("list object - key name: " + s.getKey());
                    }

                    // 删除文件
                    DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(ossProperties.getBucketName())
                            .withQuiet(true)
                            .withKeys(keys)
                            .withEncodingType(URL_ENCODING);
                    DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(deleteObjectsRequest);

                    List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();
                    try {
                        for (String obj : deletedObjects) {
                            String deleteObj = URLDecoder.decode(obj, "UTF-8");
                            log.info("unsuccessfully deleted object - key name: " + deleteObj);
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    result.addAll(deletedObjects);
                }

                nextMarker = objectListing.getNextMarker();
            } while (objectListing.isTruncated());

            return result;

        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 拷贝文件
     * <p>
     * 将源Bucket中的文件（Object）复制到同一地域下相同或不同目标Bucket中
     *
     * @param request 请求对象
     * @return 是否拷贝成功，拷贝成功true；拷贝失败false
     */
    public boolean copyObject(CopyObjectRequest request) {
        if (!ossProperties.getEnable()) {
            return false;
        }

        OSS ossClient = getClient();
        try {
            // 拷贝对象
            ossClient.copyObject(request);

            // 查询拷贝后对象是否存在
            return objectExist(request.getDestinationBucketName(), request.getDestinationKey());
        } finally {
            ossClient.shutdown();
        }
    }

}
