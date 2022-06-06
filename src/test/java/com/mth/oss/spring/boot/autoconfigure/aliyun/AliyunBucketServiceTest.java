package com.mth.oss.spring.boot.autoconfigure.aliyun;

import com.aliyun.oss.model.AccessControlList;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.BucketInfo;
import com.aliyun.oss.model.CannedAccessControlList;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class AliyunBucketServiceTest {

    private static AliyunBucketService aliyunBucketService;

    private static String bucketName = "xxxxxx";

    @BeforeAll
    static void init() {
        OssProperties.Aliyun ossProperties = new OssProperties.Aliyun();
        ossProperties.setEndpoint("xxxxxx");
        ossProperties.setAccessKeyId("xxxxxx");
        ossProperties.setAccessKeySecret("xxxxxx");
        ossProperties.setBucketName("xxxxxx");
        ossProperties.setEnable(true);
        aliyunBucketService = new AliyunBucketService(ossProperties);
    }

    @Test
    void listBuckets() {
        List<Bucket> buckets = aliyunBucketService.listBuckets();
        buckets.forEach(System.out::println);
    }

    @Test
    void bucketExist() {
        boolean result = aliyunBucketService.bucketExist();
        Assertions.assertTrue(result);
    }

    @Test
    void testBucketExist() {
        boolean result = aliyunBucketService.bucketExist("0123");
        System.out.println(result);
    }

    @Test
    void createBucket() {
        boolean bucket = aliyunBucketService.createBucket("0123");
        System.out.println(bucket);
    }

    @Test
    void deleteBucket() {
    }

    @Test
    void getBucketLocation() {
        String bucketLocation = aliyunBucketService.getBucketLocation(bucketName);
        System.out.println(bucketLocation);
    }

    @Test
    void getBucketInfo() {
        BucketInfo bucketInfo = aliyunBucketService.getBucketInfo(bucketName);
        System.out.println(bucketInfo);
    }

    @Test
    void setBucketAcl() {
        AccessControlList accessControlList = aliyunBucketService.setBucketAcl(bucketName, CannedAccessControlList.Private);
        System.out.println(accessControlList);
    }

    @Test
    void getBucketAcl() {
        AccessControlList bucketAcl = aliyunBucketService.getBucketAcl(bucketName);
        System.out.println(bucketAcl);
    }
}