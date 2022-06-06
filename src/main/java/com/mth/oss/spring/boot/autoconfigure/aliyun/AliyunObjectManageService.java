package com.mth.oss.spring.boot.autoconfigure.aliyun;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.LogUtils;
import com.aliyun.oss.model.*;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import com.mth.oss.spring.boot.autoconfigure.service.ObjectManageService;
import org.apache.commons.logging.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import static com.aliyun.oss.internal.OSSConstants.URL_ENCODING;

/**
 *  Aliyun oss 管理文件
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.0
 */
public class AliyunObjectManageService implements ObjectManageService {

    private final OssProperties.Aliyun ossProperties;

    public AliyunObjectManageService(OssProperties.Aliyun ossProperties) {
        this.ossProperties = ossProperties;
    }


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
        return listObjects(new ListObjectsRequest(ossProperties.getBucketName(), prefix, null, null, null));
    }

    @Override
    public Iterable<OSSObjectSummary> listObjects(String prefix, int maxKeys) {
        return listObjects(new ListObjectsRequest(ossProperties.getBucketName(), prefix, null, null, maxKeys));
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
     *
     * @param prefix 指定前缀
     * @return 被删除的文件 key 集合
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
        try {
            do {
                // 查询指定目录下的文件
                ListObjectsRequest listObjectsRequest = new ListObjectsRequest(ossProperties.getBucketName())
                        .withPrefix(prefix)
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
                            .withKeys(keys)
                            .withEncodingType(URL_ENCODING);
                    DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(deleteObjectsRequest);

                    List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();
                    try {
                        for (String obj : deletedObjects) {
                            String deleteObj = URLDecoder.decode(obj, "UTF-8");
                            log.info("delete object - key name: " + deleteObj);
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
