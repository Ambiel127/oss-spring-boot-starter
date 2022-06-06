package com.mth.oss.spring.boot.autoconfigure.aliyun;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import com.mth.oss.spring.boot.autoconfigure.service.BucketService;

import java.util.ArrayList;
import java.util.List;

/**
 * Aliyun 存储空间 bucket 操作
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.0
 */
public class AliyunBucketService implements BucketService {

    private final OssProperties.Aliyun ossProperties;

    public AliyunBucketService(OssProperties.Aliyun ossProperties) {
        this.ossProperties = ossProperties;
    }


    /**
     * 列举存储空间
     *
     * @return 桶列表
     */
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

    /**
     * 判断存储空间是否存在
     *
     * @return 存在true；不存在false
     */
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

    /**
     * 判断存储空间是否存在
     *
     * @param bucketName 桶名称
     * @return 存在true；不存在false
     */
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

    /**
     * 创建存储空间
     *
     * @param bucketName 桶名称
     * @return 是否创建成功，创建成功true；创建失败false
     */
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
     * 删除存储空间
     *
     * @param bucketName 桶名称
     * @return 是否删除成功，删除成功true；删除失败false
     */
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
