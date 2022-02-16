package com.mth.oss.spring.boot.autoconfigure.service;

import com.aliyun.oss.model.*;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class OssObjectManageServiceTest {

    private static OssObjectManageService ossObjectManageService;

    private static String bucketName = "xxxxxx";

    private static String key = "20220213/test123.txt";

    @BeforeAll
    static void init() {
        OssProperties ossProperties = new OssProperties();
        ossProperties.setEndpoint("xxxxxx");
        ossProperties.setAccessKeyId("xxxxxx");
        ossProperties.setAccessKeySecret("xxxxxx");
        ossProperties.setBucketName("xxxxxx");
        ossProperties.setEnable(true);
        ossObjectManageService = new OssObjectManageService(ossProperties);
    }

    @Test
    void objectExist() {
        boolean result = ossObjectManageService.objectExist(key);
        Assertions.assertTrue(result);
    }

    @Test
    void getObjectAcl() {
        ObjectAcl objectAcl = ossObjectManageService.getObjectAcl(key);
        // AccessControlList [owner=Owner [name=1414936820682835,id=1414936820682835], permission=default]
        System.out.println(objectAcl);
    }

    @Test
    void setObjectAcl() {
        ObjectAcl objectAcl = ossObjectManageService.setObjectAcl(key, CannedAccessControlList.Default);
        System.out.println(objectAcl);
    }

    @Test
    void getObjectMetadata() {
        ObjectMetadata objectMetadata = ossObjectManageService.getObjectMetadata(key);
        System.out.println(objectMetadata.getRawMetadata());
    }

    @Test
    void listObjects() {
        ObjectListing objectListing = ossObjectManageService.listObjects();
        printObjectInfo(objectListing);
    }

    @Test
    void testListObjects() {
        ObjectListing objectListing = ossObjectManageService.listObjects(1000);
        printObjectInfo(objectListing);
    }

    @Test
    void testListObjects1() {
        ObjectListing objectListing = ossObjectManageService.listObjects("pdfDir/");
        printObjectInfo(objectListing);
    }

    @Test
    void testListObjects2() {
        ListObjectsRequest request = new ListObjectsRequest(bucketName);
        request.setPrefix("pdfDir/");
        request.setMaxKeys(500);
        ObjectListing objectListing = ossObjectManageService.listObjects(request);
        printObjectInfo(objectListing);
    }

    @Test
    void deleteObject() {
        String key = "20220124-001.txt";
        boolean result = ossObjectManageService.deleteObject(key);
        // Assertions.assertTrue(result);
    }

    @Test
    void deleteObjects() {
    }

    @Test
    void testDeleteObjects() {
        List<String> result = ossObjectManageService.deleteObjects("2022-01-24");
        System.out.println(result);
    }

    @Test
    void copyObject() {
        String destinationKey = "20220213/test1234.txt";
        CopyObjectResult copyObjectResult = ossObjectManageService.copyObject(key, destinationKey);
        System.out.println(copyObjectResult);
    }

    @Test
    void testCopyObject() {
    }

    /**
     * 打印对象信息
     */
    private void printObjectInfo(ObjectListing objectListing) {
        List<OSSObjectSummary> objectSummaries = objectListing.getObjectSummaries();
        System.out.printf("对象个数：%s个\n", objectSummaries.size());
        objectSummaries
                .stream()
                .map(OSSObjectSummary::getKey)
                .forEach(System.out::println);
    }

}