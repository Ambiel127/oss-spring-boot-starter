package com.mth.oss.spring.boot.autoconfigure.minio;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import io.minio.messages.Bucket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class MinioBucketServiceTest {

    private static MinioBucketService minioBucketService;

    private static String bucketName;

    @BeforeAll
    static void init() {
        OssProperties.Minio ossProperties = new OssProperties.Minio();
        ossProperties.setEndpoint("xxxxx");
        ossProperties.setAccessKeyId("xxxxxx");
        ossProperties.setAccessKeySecret("xxxxxx");
        ossProperties.setBucketName("xxxxxx");
        ossProperties.setEnable(true);
        minioBucketService = new MinioBucketService(ossProperties);
        bucketName = ossProperties.getBucketName();
    }

    @Test
    void testListBuckets() {
        List<Bucket> buckets = minioBucketService.listBuckets();
        Assertions.assertNotNull(buckets);
        Assertions.assertNotEquals(0, buckets.size());
    }

    @Test
    void testBucketExist() {
        boolean result = minioBucketService.bucketExist();
        Assertions.assertTrue(result);
    }

    @Test
    void testBucketExist1() {
        boolean result = minioBucketService.bucketExist("do-not-exist-bucketname");
        Assertions.assertFalse(result);
    }

    @Test
    void testCreateBucket() {
        boolean result = minioBucketService.createBucket("do-not-exist-bucketname");
        Assertions.assertTrue(result);
    }

    @Test
    void testDeleteBucket() {
        boolean result = minioBucketService.deleteBucket("do-not-exist-bucketname");
        Assertions.assertTrue(result);
    }

}