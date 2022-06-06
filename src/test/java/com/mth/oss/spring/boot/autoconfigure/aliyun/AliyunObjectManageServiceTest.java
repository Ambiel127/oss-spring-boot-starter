package com.mth.oss.spring.boot.autoconfigure.aliyun;

import com.aliyun.oss.model.*;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class AliyunObjectManageServiceTest {

    private static AliyunObjectManageService aliyunObjectManageService;

    private static String bucketName = "xxxxxx";

    private static String key = "20220213/test123.txt";

    @BeforeAll
    static void init() {
        OssProperties.Aliyun ossProperties = new OssProperties.Aliyun();
        ossProperties.setEndpoint("xxxxxx");
        ossProperties.setAccessKeyId("xxxxxx");
        ossProperties.setAccessKeySecret("xxxxxx");
        ossProperties.setBucketName("xxxxxx");
        ossProperties.setEnable(true);
        aliyunObjectManageService = new AliyunObjectManageService(ossProperties);
    }

    @Test
    void objectExist() {
        boolean result = aliyunObjectManageService.objectExist(key);
        Assertions.assertTrue(result);
    }

    @Test
    void getObjectMetadata() {
        ObjectMetadata objectMetadata = aliyunObjectManageService.getObjectMetadata(key);
        System.out.println(objectMetadata.getRawMetadata());
    }

    @Test
    void listObjects() {
        Iterable<OSSObjectSummary> objectListing = aliyunObjectManageService.listObjects();
        printObjectInfo(objectListing);
    }

    @Test
    void testListObjects() {
        Iterable<OSSObjectSummary> objectListing = aliyunObjectManageService.listObjects(1000);
        printObjectInfo(objectListing);
    }

    @Test
    void testListObjects1() {
        Iterable<OSSObjectSummary> objectListing = aliyunObjectManageService.listObjects("pdfDir/");
        printObjectInfo(objectListing);
    }

    @Test
    void testListObjects2() {
        ListObjectsRequest request = new ListObjectsRequest(bucketName);
        request.setPrefix("pdfDir/");
        request.setMaxKeys(500);
        Iterable<OSSObjectSummary> objectListing = aliyunObjectManageService.listObjects(request);
        printObjectInfo(objectListing);
    }

    @Test
    void deleteObject() {
        String key = "20220124-001.txt";
        boolean result = aliyunObjectManageService.deleteObject(key);
        // Assertions.assertTrue(result);
    }

    @Test
    void deleteObjects() {
    }

    @Test
    void testDeleteObjects() {
        List<String> result = aliyunObjectManageService.deleteObjects("2022-01-24");
        System.out.println(result);
    }

    @Test
    void copyObject() {
        String destinationKey = "20220213/test1234.txt";
        boolean result = aliyunObjectManageService.copyObject(key, destinationKey);
        Assertions.assertTrue(result);
    }

    @Test
    void testCopyObject() {
    }

    /**
     * 打印对象信息
     */
    private void printObjectInfo(Iterable<OSSObjectSummary> objectListing) {
        int size = 0;
        for (OSSObjectSummary item : objectListing) {
            System.out.println(item.getKey());
            size++;
        }
        System.out.printf("对象个数：%s个\n", size);
    }

}