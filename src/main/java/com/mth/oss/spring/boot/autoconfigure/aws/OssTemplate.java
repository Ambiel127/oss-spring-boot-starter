package com.mth.oss.spring.boot.autoconfigure.aws;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import com.mth.oss.spring.boot.autoconfigure.handler.DefaultOssHandler;
import com.mth.oss.spring.boot.autoconfigure.handler.OssHandler;
import com.mth.oss.spring.boot.autoconfigure.service.OssOperations;
import org.springframework.http.MediaType;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 对象存储服务的 Amazon S3 实现
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.3
 */
public class OssTemplate implements OssOperations {

    @Resource
    private AmazonS3 client;

    @Resource
    private OssProperties ossProperties;

    private OssHandler ossHandler;


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
        ossHandler.beforeBucketDelete(bucketName);

        client.deleteBucket(bucketName);

        ossHandler.afterBucketDelete(bucketName);
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
        ossHandler.beforeUpload(putObjectRequest);

        // 上传
        PutObjectResult putObjectResult = client.putObject(putObjectRequest);

        ossHandler.afterUpload(putObjectRequest, putObjectResult);
        return putObjectRequest.getKey();
    }

    @Override
    public URL presignedUrlForUpload(String objectKey) {
        return generatePresignedUrl(objectKey, null, HttpMethod.PUT, null);
    }

    @Override
    public URL presignedUrlForUpload(String objectKey, int duration, TimeUnit unit) {
        return generatePresignedUrl(objectKey, unit.toSeconds(duration), HttpMethod.PUT, null);
    }

    @Override
    public URL generatePresignedUrl(GeneratePresignedUrlRequest request) {
        ossHandler.beforeGeneratePresignedUrl(request);

        URL url = client.generatePresignedUrl(request);

        ossHandler.afterGeneratePresignedUrl(request, url);
        return url;
    }

    @Override
    public InitiateMultipartUploadResult initMultipartUpload(String objectKey) {
        return initMultipartUpload(objectKey, MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    @Override
    public InitiateMultipartUploadResult initMultipartUpload(String objectKey, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);

        InitiateMultipartUploadRequest req = new InitiateMultipartUploadRequest(
                ossProperties.getBucketName(), objectKey, metadata);
        return initMultipartUpload(req);
    }

    @Override
    public InitiateMultipartUploadResult initMultipartUpload(InitiateMultipartUploadRequest request) {
        return client.initiateMultipartUpload(request);
    }

    @Override
    public URL presignedUrlForMultipartUpload(String uploadId, int partNumber, String objectKey) {
        Map<String, String> params = new HashMap<>();
        params.put("uploadId", uploadId);
        params.put("partNumber", String.valueOf(partNumber));

        return generatePresignedUrl(objectKey, null, HttpMethod.PUT, params);
    }

    @Override
    public URL presignedUrlForMultipartUpload(String uploadId, int partNumber, String objectKey, int duration, TimeUnit unit) {
        Map<String, String> params = new HashMap<>();
        params.put("uploadId", uploadId);
        params.put("partNumber", String.valueOf(partNumber));

        return generatePresignedUrl(objectKey, unit.toSeconds(duration), HttpMethod.PUT, params);
    }

    @Override
    public UploadPartResult uploadPart(UploadPartRequest request) {
        return client.uploadPart(request);
    }

    @Override
    public PartListing listParts(String uploadId, String objectKey) {
        ListPartsRequest request = new ListPartsRequest(ossProperties.getBucketName(), objectKey, uploadId);
        return listParts(request);
    }

    @Override
    public PartListing listParts(ListPartsRequest request) {
        return client.listParts(request);
    }

    @Override
    public MultipartUploadListing listMultipartUploads() {
        ListMultipartUploadsRequest request = new ListMultipartUploadsRequest(ossProperties.getBucketName());
        return listMultipartUploads(request);
    }

    @Override
    public MultipartUploadListing listMultipartUploads(ListMultipartUploadsRequest request) {
        return client.listMultipartUploads(request);
    }

    @Override
    public CompleteMultipartUploadResult completeMultipartUpload(String uploadId, String objectKey, List<PartETag> partETags) {
        CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(
                ossProperties.getBucketName(), objectKey, uploadId, partETags);
        return completeMultipartUpload(compRequest);
    }

    @Override
    public CompleteMultipartUploadResult completeMultipartUpload(CompleteMultipartUploadRequest request) {
        return client.completeMultipartUpload(request);
    }

    @Override
    public void abortMultipartUpload(String uploadId, String objectKey) {
        AbortMultipartUploadRequest request = new AbortMultipartUploadRequest(
                ossProperties.getBucketName(), objectKey, uploadId);
        abortMultipartUpload(request);
    }

    @Override
    public void abortMultipartUpload(AbortMultipartUploadRequest request) {
        client.abortMultipartUpload(request);
    }

    @Override
    public URL presignedUrlForAccess(String objectKey) {
        return generatePresignedUrl(objectKey, null, HttpMethod.GET, null);
    }

    @Override
    public URL presignedUrlForAccess(String objectKey, int duration, TimeUnit unit) {
        return generatePresignedUrl(objectKey, unit.toSeconds(duration), HttpMethod.GET, null);
    }

    @Override
    public boolean download(String objectKey, String fullFilePath) {
        File file = new File(fullFilePath);
        return download(objectKey, file);
    }

    @Override
    public boolean download(String objectKey, File file) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(ossProperties.getBucketName(), objectKey);

        ossHandler.beforeDownload(getObjectRequest);

        ObjectMetadata metadata = client.getObject(getObjectRequest, file);

        ossHandler.afterDownload(getObjectRequest, metadata);
        return file.exists();
    }

    @Override
    public byte[] download(String objectKey) throws IOException {
        S3ObjectInputStream inputStream = getObjectInputStream(objectKey);

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
        S3ObjectInputStream inputStream = getObjectInputStream(objectKey);
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
        ossHandler.beforeObjectDelete(Collections.singletonList(objectKey));

        client.deleteObject(ossProperties.getBucketName(), objectKey);

        ossHandler.afterObjectDelete(Collections.singletonList(objectKey));
        return !objectExist(objectKey);
    }

    @Override
    public List<DeleteObjectsResult.DeletedObject> deleteObjects(List<String> objectKeys) {
        ossHandler.beforeObjectDelete(objectKeys);

        DeleteObjectsResult deleteResult = client.deleteObjects(
                new DeleteObjectsRequest(ossProperties.getBucketName())
                        .withQuiet(true)
                        .withKeys(objectKeys.toArray(new String[0]))
        );

        ossHandler.afterObjectDelete(objectKeys);
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
     * 获取默认 object key 完整路径
     * <p>
     * 默认前缀为 yyyyMMdd 日期文件夹；
     * 如 file 文件名为 temp.txt，则默认 Object 完整路径为 "yyyyMMdd/temp_yyyyMMddHHmmssSSS.txt"
     *
     * @param file 文件
     * @return object 完整路径
     */
    private String getDefaultObjectKey(File file) {
        // 获取文件名称和扩展名
        String fileName = file.getName();
        String extra = fileName.substring(fileName.lastIndexOf("."));
        String name = fileName.substring(0, fileName.lastIndexOf("."));

        // 路径前缀
        String pathPrefix = LocalDate.now().format(DATE_FORMATTER);

        // 时间戳字符串
        String dateTimeStr = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        // 重新命名后的 Object 完整路径
        return pathPrefix + "/" + name + "_" + dateTimeStr + extra;
    }

    /**
     * 生成签名 URL 授权访问
     *
     * @param objectKey  Object 完整路径
     * @param expiration 签名 url 过期时长，单位秒
     * @param method     签名 url 请求方法。PUT用来上传对象；GET用来访问对象
     * @param params     额外请求参数
     * @return 授权访问 URL 对象
     */
    private URL generatePresignedUrl(String objectKey, Long expiration, HttpMethod method, Map<String, String> params) {
        // 过期时间为空，则默认1小时
        long expiry = Objects.isNull(expiration) ? ossProperties.getExpiration() : expiration;
        // 转换为毫秒
        long expiryMillis = TimeUnit.SECONDS.toMillis(expiry);

        // 过期date
        Date expirationDate = new Date(System.currentTimeMillis() + expiryMillis);

        // 处理路径分隔符
        objectKey = trimPathCharacter(objectKey);

        // 组装请求对象
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(ossProperties.getBucketName(), objectKey)
                .withMethod(method)
                .withExpiration(expirationDate);

        if (Objects.nonNull(params)) {
            params.forEach(request::addRequestParameter);
        }

        return generatePresignedUrl(request);

    }

    /**
     * 获取文件输入流
     *
     * @param objectKey Object 完整路径
     * @return 文件输入流
     */
    private S3ObjectInputStream getObjectInputStream(String objectKey) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(ossProperties.getBucketName(), objectKey);

        ossHandler.beforeDownload(getObjectRequest);

        S3Object object = client.getObject(getObjectRequest);

        ossHandler.afterDownload(getObjectRequest, object.getObjectMetadata());

        return object.getObjectContent();
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

    /**
     * 获取客户端对象实例
     *
     * @return 客户端对象
     */
    public AmazonS3 getClientInstance() {
        return client;
    }

    /**
     * 设置自定义扩展点实现
     *
     * @param ossHandler 扩展点实现类
     */
    public void setOssHandler(OssHandler ossHandler) {
        this.ossHandler = Objects.nonNull(ossHandler) ? ossHandler : new DefaultOssHandler();
    }

}
