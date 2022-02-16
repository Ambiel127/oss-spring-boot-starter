package com.mth.oss.spring.boot.autoconfigure.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.LogUtils;
import com.aliyun.oss.model.*;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import org.apache.commons.logging.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import static com.aliyun.oss.internal.OSSConstants.URL_ENCODING;

/**
 * oss 管理文件
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.0
 */
public class OssObjectManageService {

    private final OssProperties ossProperties;

    public OssObjectManageService(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }


    /**
     * 判断文件是否存在
     *
     * @param key Object完整路径，不能包含Bucket名称
     * @return 存在true；不存在false
     */
    public boolean objectExist(String key) {
        if (!ossProperties.getEnable()) {
            return false;
        }

        OSS ossClient = getClient();
        try {
            return ossClient.doesObjectExist(ossProperties.getBucketName(), key);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 获取文件访问权限
     *
     * @param key Object完整路径，不能包含Bucket名称
     * @return 文件访问权限对象
     */
    public ObjectAcl getObjectAcl(String key) {
        if (!ossProperties.getEnable()) {
            return new ObjectAcl();
        }

        OSS ossClient = getClient();
        try {
            return ossClient.getObjectAcl(ossProperties.getBucketName(), key);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 设置文件访问权限
     * <p>
     * 文件的访问权限优先级高于存储空间的访问权限，如果某个文件没有设置过访问权限，则遵循存储空间的访问权限。
     *
     * @param key    Object完整路径，不能包含Bucket名称
     * @param access 访问控制权限
     * @return 当前访问控制权限信息
     */
    public ObjectAcl setObjectAcl(String key, CannedAccessControlList access) {
        if (!ossProperties.getEnable()) {
            return new ObjectAcl();
        }

        OSS ossClient = getClient();
        try {
            ossClient.setObjectAcl(ossProperties.getBucketName(), key, access);
            return ossClient.getObjectAcl(ossProperties.getBucketName(), key);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 获取文件元信息
     *
     * @param key Object完整路径，不能包含Bucket名称
     * @return 文件元信息对象
     */
    public ObjectMetadata getObjectMetadata(String key) {
        if (!ossProperties.getEnable()) {
            return new ObjectMetadata();
        }

        OSS ossClient = getClient();
        try {
            return ossClient.getObjectMetadata(ossProperties.getBucketName(), key);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 列举文件
     *
     * @return 集合文件对象，默认100个
     */
    public ObjectListing listObjects() {
        return listObjects(new ListObjectsRequest(ossProperties.getBucketName(), null, null, null, null));
    }

    /**
     * 列举文件
     *
     * @param maxKeys 最大个数
     * @return 集合文件对象
     */
    public ObjectListing listObjects(int maxKeys) {
        return listObjects(new ListObjectsRequest(ossProperties.getBucketName(), null, null, null, maxKeys));
    }

    /**
     * 列举文件
     *
     * @param prefix 指定前缀
     * @return 集合文件对象
     */
    public ObjectListing listObjects(String prefix) {
        return listObjects(new ListObjectsRequest(ossProperties.getBucketName(), prefix, null, null, null));
    }

    /**
     * 列举文件
     *
     * @param listObjectsRequest 请求对象
     * @return 集合文件对象
     */
    public ObjectListing listObjects(ListObjectsRequest listObjectsRequest) {
        if (!ossProperties.getEnable()) {
            return new ObjectListing();
        }

        OSS ossClient = getClient();
        try {
            return ossClient.listObjects(listObjectsRequest);
        } finally {
            ossClient.shutdown();
        }
    }

    // todo [matianhao] 列举全部文件需要封装吗？

    /**
     * 删除单个文件
     * <p>
     * 如果要删除目录，目录必须为空
     *
     * @param key Object完整路径，不能包含Bucket名称
     * @return 是否删除成功，删除成功true；删除失败false
     */
    public boolean deleteObject(String key) {
        if (!ossProperties.getEnable()) {
            return false;
        }

        OSS ossClient = getClient();
        try {
            ossClient.deleteObject(ossProperties.getBucketName(), key);

            return !ossClient.doesObjectExist(ossProperties.getBucketName(), key);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 删除指定的多个文件
     *
     * @param keys Object完整路径集合
     * @return 删除结果对象
     */
    public DeleteObjectsResult deleteObjects(List<String> keys) {
        if (!ossProperties.getEnable()) {
            return new DeleteObjectsResult();
        }

        OSS ossClient = getClient();
        try {
            return ossClient.deleteObjects(
                    new DeleteObjectsRequest(ossProperties.getBucketName())
                            .withKeys(keys)
                            .withEncodingType(URL_ENCODING));
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
     * @param sourceKey      源Object完整路径
     * @param destinationKey 目标Object完整路径
     * @return 拷贝结果对象
     */
    public CopyObjectResult copyObject(String sourceKey, String destinationKey) {
        // 创建CopyObjectRequest对象。
        CopyObjectRequest request = new CopyObjectRequest(ossProperties.getBucketName(), sourceKey,
                                                          ossProperties.getBucketName(), destinationKey);
        return copyObject(request);
    }

    /**
     * 拷贝文件
     * <p>
     * 将源Bucket中的文件（Object）复制到同一地域下相同或不同目标Bucket中
     *
     * @param request 请求对象
     * @return 拷贝结果对象
     */
    public CopyObjectResult copyObject(CopyObjectRequest request) {
        if (!ossProperties.getEnable()) {
            return new CopyObjectResult();
        }

        OSS ossClient = getClient();
        try {
            return ossClient.copyObject(request);
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
