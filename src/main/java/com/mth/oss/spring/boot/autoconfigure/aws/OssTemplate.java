package com.mth.oss.spring.boot.autoconfigure.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import com.mth.oss.spring.boot.autoconfigure.service.OssOperations;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 对象存储服务的 Amazon S3 实现
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.3
 */
@RequiredArgsConstructor
public class OssTemplate implements OssOperations {

    private final OssProperties ossProperties;

    @Resource
    private AmazonS3 client;


    @Override
    public List<Bucket> listBuckets() {
        return client.listBuckets();
    }

    @Override
    public boolean bucketExist() {
        return bucketExist(ossProperties.getBucketName());
    }

    @Override
    public boolean bucketExist(String bucketName) {
        return client.doesBucketExistV2(bucketName);
    }

    @Override
    public boolean createBucket(String bucketName) {
        if (bucketExist(bucketName)) {
            return false;
        }

        Bucket bucket = client.createBucket(bucketName);
        return Objects.nonNull(bucket.getName());
    }

    @Override
    public boolean deleteBucket(String bucketName) {
        client.deleteBucket(bucketName);
        return !bucketExist(bucketName);
    }

    @Override
    public String upload(File file) {
        // 重新命名后的 Object 完整路径
        String objectKey = getDefaultObjectKey(file);

        return upload(file, objectKey);
    }

    @Override
    public String upload(File file, String objectKey) {
        objectKey = trimPathCharacter(objectKey);

        PutObjectRequest putObjectRequest = new PutObjectRequest(ossProperties.getBucketName(), objectKey, file);
        return upload(putObjectRequest);
    }

    @Override
    public String upload(InputStream inputStream, String objectKey) {
        return upload(inputStream, objectKey, MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    @Override
    public String upload(InputStream inputStream, String objectKey, String contentType) {
        objectKey = trimPathCharacter(objectKey);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);

        PutObjectRequest putObjectRequest = new PutObjectRequest(ossProperties.getBucketName(), objectKey, inputStream, metadata);
        return upload(putObjectRequest);
    }

    @Override
    public String replaceUpload(File file) {
        String objectKey = file.getName();
        return replaceUpload(file, objectKey);
    }

    @Override
    public String replaceUpload(File file, String objectKey) {
        objectKey = trimPathCharacter(objectKey);

        // 创建 PutObjectRequest 对象
        PutObjectRequest putObjectRequest = new PutObjectRequest(ossProperties.getBucketName(), objectKey, file);

        return upload(putObjectRequest);
    }

    @Override
    public String upload(PutObjectRequest putObjectRequest) {
        // todo [matianhao] 上传进度监听
        putObjectRequest.setGeneralProgressListener(
                progressEvent ->
                        System.out.println("Transferred bytes: " + progressEvent.getBytesTransferred()));
        client.putObject(putObjectRequest);
        return putObjectRequest.getKey();
    }

    @Override
    public URL generatePresignedUrl(String objectKey) {
        return generatePresignedUrl(objectKey, null);
    }

    @Override
    public URL generatePresignedUrl(String objectKey, int duration, TimeUnit unit) {
        return generatePresignedUrl(objectKey, unit.toSeconds(duration));
    }

    @Override
    public boolean download(String objectKey, String fullFilePath) {
        File file = new File(fullFilePath);
        return download(objectKey, file);
    }

    @Override
    public boolean download(String objectKey, File file) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(ossProperties.getBucketName(), objectKey);

        // todo [matianhao] 下载进度监听
        getObjectRequest.setGeneralProgressListener(progressEvent ->
                                                            System.out.println("Download bytes: " + progressEvent.getBytesTransferred()));
        client.getObject(getObjectRequest, file);
        return file.exists();
    }

    @Override
    public byte[] download(String objectKey) throws IOException {
        S3Object object = client.getObject(ossProperties.getBucketName(), objectKey);
        S3ObjectInputStream inputStream = object.getObjectContent();

        final int available = inputStream.available();
        final byte[] result = new byte[available];

        int readLength = inputStream.read(result);
        if (readLength != available) {
            throw new IOException("File length is [" + available + "] but read [" + readLength + "]!");
        }
        return result;
    }

    @Override
    public void download(String objectKey, OutputStream outputStream) throws IOException {
        S3Object object = client.getObject(ossProperties.getBucketName(), objectKey);
        S3ObjectInputStream inputStream = object.getObjectContent();
        IOUtils.copy(inputStream, outputStream);
    }

    @Override
    public boolean objectExist(String objectKey) {
        return objectExist(ossProperties.getBucketName(), objectKey);
    }

    @Override
    public boolean objectExist(String bucketName, String objectKey) {
        return client.doesObjectExist(bucketName, objectKey);
    }

    @Override
    public S3Object getObject(String objectKey) {
        return getObject(ossProperties.getBucketName(), objectKey);
    }

    @Override
    public S3Object getObject(String bucketName, String objectKey) {
        return client.getObject(bucketName, objectKey);
    }

    @Override
    public List<S3ObjectSummary> listObjects() {
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(ossProperties.getBucketName());
        return listObjects(request);
    }

    @Override
    public List<S3ObjectSummary> listObjects(int maxKeys) {
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(ossProperties.getBucketName())
                .withMaxKeys(maxKeys);
        return listObjects(request);
    }

    @Override
    public List<S3ObjectSummary> listObjects(String prefix) {
        String trimPathPrefix = trimPathCharacter(prefix);

        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(ossProperties.getBucketName())
                .withPrefix(trimPathPrefix);
        return listObjects(request);
    }

    @Override
    public List<S3ObjectSummary> listObjects(String prefix, int maxKeys) {
        String trimPathPrefix = trimPathCharacter(prefix);
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(ossProperties.getBucketName())
                .withPrefix(trimPathPrefix)
                .withMaxKeys(maxKeys);
        return listObjects(request);
    }

    @Override
    public List<S3ObjectSummary> listObjects(ListObjectsV2Request listObjectsRequest) {
        ListObjectsV2Result objectsV2Result = client.listObjectsV2(listObjectsRequest);
        return objectsV2Result.getObjectSummaries();
    }

    @Override
    public boolean deleteObject(String objectKey) {
        client.deleteObject(ossProperties.getBucketName(), objectKey);
        return !objectExist(objectKey);
    }

    @Override
    public List<DeleteObjectsResult.DeletedObject> deleteObjects(List<String> objectKeys) {
        DeleteObjectsResult deleteResult = client.deleteObjects(
                new DeleteObjectsRequest(ossProperties.getBucketName())
                        .withQuiet(true)
                        .withKeys(objectKeys.toArray(new String[0]))
        );
        // 删除失败的文件列表
        return deleteResult.getDeletedObjects();
    }

    @Override
    public boolean copyObject(String sourceKey, String destinationKey) {
        // 创建CopyObjectRequest对象
        CopyObjectRequest request = new CopyObjectRequest(ossProperties.getBucketName(), sourceKey,
                                                          ossProperties.getBucketName(), destinationKey);
        return copyObject(request);
    }

    @Override
    public boolean copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) {
        // 创建CopyObjectRequest对象
        CopyObjectRequest request = new CopyObjectRequest(sourceBucketName, sourceKey,
                                                          destinationBucketName, destinationKey);
        return copyObject(request);
    }

    /**
     * 获取客户端对象实例
     *
     * @return 客户端对象
     */
    public AmazonS3 getClientInstance() {
        return client;
    }

    /**
     * 生成签名 URL 授权访问
     *
     * @param objectKey  Object 完整路径
     * @param expiration 签名 url 过期时长，单位秒
     * @return 授权访问 URL 对象
     */
    public URL generatePresignedUrl(String objectKey, Long expiration) {

        // 过期时间为空，则默认1小时
        long expiry = Objects.isNull(expiration) ? ossProperties.getExpiration() : expiration;
        // 转换为毫秒
        long expiryMillis = TimeUnit.SECONDS.toMillis(expiry);

        // 过期date
        Date expirationDate = new Date(System.currentTimeMillis() + expiryMillis);

        // 处理路径分隔符
        objectKey = trimPathCharacter(objectKey);

        return client.generatePresignedUrl(ossProperties.getBucketName(), objectKey, expirationDate);

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
        // 拷贝对象
        client.copyObject(request);

        // 查询拷贝后对象是否存在
        return objectExist(request.getDestinationBucketName(), request.getDestinationKey());
    }

}