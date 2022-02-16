package com.mth.oss.spring.boot.autoconfigure.service;

import com.aliyun.oss.model.AccessControlList;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.BucketInfo;
import com.aliyun.oss.model.CannedAccessControlList;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class OssBucketServiceTest {

    private static OssBucketService ossBucketService;

    private static String bucketName = "xxxxxx";

    @BeforeAll
    static void init() {
        OssProperties ossProperties = new OssProperties();
        ossProperties.setEndpoint("xxxxxx");
        ossProperties.setAccessKeyId("xxxxxx");
        ossProperties.setAccessKeySecret("xxxxxx");
        ossProperties.setBucketName("xxxxxx");
        ossProperties.setEnable(true);
        ossBucketService = new OssBucketService(ossProperties);
    }

    @Test
    void listBuckets() {
        List<Bucket> buckets = ossBucketService.listBuckets();
        buckets.forEach(System.out::println);
    }

    @Test
    void bucketExist() {
        boolean result = ossBucketService.bucketExist();
        Assertions.assertTrue(result);
    }

    @Test
    void testBucketExist() {
        boolean result = ossBucketService.bucketExist("0123");
        System.out.println(result);
    }

    @Test
    void createBucket() {
        boolean bucket = ossBucketService.createBucket("0123");
        System.out.println(bucket);
    }

    @Test
    void deleteBucket() {
    }

    @Test
    void getBucketLocation() {
        String bucketLocation = ossBucketService.getBucketLocation(bucketName);
        System.out.println(bucketLocation);
    }

    @Test
    void getBucketInfo() {
        BucketInfo bucketInfo = ossBucketService.getBucketInfo(bucketName);
        System.out.println(bucketInfo);
    }

    @Test
    void setBucketAcl() {
        AccessControlList accessControlList = ossBucketService.setBucketAcl(bucketName, CannedAccessControlList.Private);
        System.out.println(accessControlList);
    }

    @Test
    void getBucketAcl() {
        AccessControlList bucketAcl = ossBucketService.getBucketAcl(bucketName);
        System.out.println(bucketAcl);
    }
}