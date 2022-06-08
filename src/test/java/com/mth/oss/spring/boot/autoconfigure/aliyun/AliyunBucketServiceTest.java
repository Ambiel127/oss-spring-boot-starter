package com.mth.oss.spring.boot.autoconfigure.aliyun;

import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.AccessControlList;
import com.aliyun.oss.model.BucketInfo;
import com.aliyun.oss.model.CannedAccessControlList;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AliyunBucketServiceTest {

    private static AliyunBucketService aliyunBucketService;

    private static String bucketName;

    @BeforeAll
    static void init() {
        OssProperties.Aliyun ossProperties = new OssProperties.Aliyun();
        ossProperties.setEndpoint("xxxxxx");
        ossProperties.setAccessKeyId("xxxxxx");
        ossProperties.setAccessKeySecret("xxxxxx");
        ossProperties.setBucketName("xxxxxx");
        ossProperties.setEnable(true);
        aliyunBucketService = new AliyunBucketService(ossProperties);
        bucketName = ossProperties.getBucketName();
    }

    @Test
    void testListBuckets() {
        // 异常测试用例
        Assertions.assertThrows(OSSException.class, () -> aliyunBucketService.listBuckets());

        // 正常测试用例
        // List<Bucket> buckets = aliyunBucketService.listBuckets();
        // Assertions.assertNotNull(buckets);
        // Assertions.assertNotEquals(0, buckets.size());
    }

    @Test
    void testBucketExist() {
        boolean result = aliyunBucketService.bucketExist();
        Assertions.assertTrue(result);
    }

    @Test
    void testBucketExist1() {
        boolean result = aliyunBucketService.bucketExist("do-not-exist-bucketname");
        Assertions.assertFalse(result);
    }

    @Test
    void testCreateBucket() {
        // 异常测试用例
        Assertions.assertThrows(OSSException.class, () -> aliyunBucketService.createBucket("do-not-exist-bucketname"));

        // 正常测试用例
        // boolean result = aliyunBucketService.createBucket("do-not-exist-bucketname");
        // Assertions.assertTrue(result);
    }

    @Test
    void testDeleteBucket() {
        // 异常测试用例
        Assertions.assertThrows(OSSException.class, () -> aliyunBucketService.deleteBucket("do-not-exist-bucketname"));

        // 正常测试用例
        // boolean result = aliyunBucketService.deleteBucket("do-not-exist-bucketname");
        // Assertions.assertTrue(result);
    }

    @Test
    void testGetBucketLocation() {
        String bucketLocation = aliyunBucketService.getBucketLocation(bucketName);
        Assertions.assertNotNull(bucketLocation);
    }

    @Test
    void testGetBucketInfo() {
        BucketInfo bucketInfo = aliyunBucketService.getBucketInfo(bucketName);
        Assertions.assertNotNull(bucketInfo);
    }

    @Test
    void testSetBucketAcl() {
        CannedAccessControlList access = CannedAccessControlList.Private;
        AccessControlList accessControlList = aliyunBucketService.setBucketAcl(bucketName, access);

        Assertions.assertNotNull(accessControlList);
        Assertions.assertEquals(access, accessControlList.getCannedACL());
    }

    @Test
    void testGetBucketAcl() {
        AccessControlList bucketAcl = aliyunBucketService.getBucketAcl(bucketName);
        Assertions.assertNotNull(bucketAcl);
    }
}