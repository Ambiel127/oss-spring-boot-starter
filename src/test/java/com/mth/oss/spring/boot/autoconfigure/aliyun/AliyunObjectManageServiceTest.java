package com.mth.oss.spring.boot.autoconfigure.aliyun;

import com.aliyun.oss.model.OSSObjectSummary;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AliyunObjectManageServiceTest {

    private static AliyunObjectManageService aliyunObjectManageService;

    private static AliyunUploadService aliyunUploadService;

    private static String bucketName;

    private static File testFile = new File("C:\\Users\\ambie\\Desktop\\test.txt");

    private static String testPath = "ossSpringBootStarterTestDir/";

    private static String testObjectKey;

    @BeforeAll
    static void init() {
        OssProperties.Aliyun ossProperties = new OssProperties.Aliyun();
        ossProperties.setEndpoint("xxxxxx");
        ossProperties.setAccessKeyId("xxxxxx");
        ossProperties.setAccessKeySecret("xxxxxx");
        ossProperties.setBucketName("xxxxxx");
        ossProperties.setEnable(true);
        aliyunObjectManageService = new AliyunObjectManageService(ossProperties);
        AliyunBucketService aliyunBucketService = new AliyunBucketService(ossProperties);
        aliyunUploadService = new AliyunUploadService(ossProperties, aliyunBucketService);
        bucketName = ossProperties.getBucketName();


        // 上传测试文件
        String objectKey = aliyunUploadService.upload(testFile, testPath);

        // 验证上传是否成功
        assertNotNull(objectKey);
        assertTrue(aliyunObjectManageService.objectExist(objectKey));

        testObjectKey = objectKey;
    }

    @AfterAll
    static void cleanTestFile() {
        // 删除文件
        assertTrue(aliyunObjectManageService.deleteObject(testObjectKey));

        // 校验测试目录是否清理干净
        Iterable<OSSObjectSummary> objectListing = aliyunObjectManageService.listObjects(testPath);
        assertFalse(objectListing.iterator().hasNext());
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

    @Test
    void testObjectExist() {
        boolean result = aliyunObjectManageService.objectExist(testObjectKey);
        Assertions.assertTrue(result);
    }

    @Test
    void testGetObjectMetadata() {
        assertDoesNotThrow(() -> aliyunObjectManageService.getObjectMetadata(testObjectKey));
    }

    @Test
    void testListObjects() {
        Iterable<OSSObjectSummary> objectListing = aliyunObjectManageService.listObjects();
        printObjectInfo(objectListing);
        assertNotNull(objectListing);
    }

    @Test
    void testListObjects1() {
        Iterable<OSSObjectSummary> objectListing = aliyunObjectManageService.listObjects(1000);
        printObjectInfo(objectListing);
        assertNotNull(objectListing);
    }

    @Test
    void testListObjects2() {
        Iterable<OSSObjectSummary> objectListing = aliyunObjectManageService.listObjects(testPath);
        printObjectInfo(objectListing);
        assertNotNull(objectListing);
    }

    @Test
    void testListObjects3() {
        Iterable<OSSObjectSummary> objectListing = aliyunObjectManageService.listObjects(testPath, 10);
        printObjectInfo(objectListing);
        assertNotNull(objectListing);
    }

    @Test
    void testDeleteObject() {
    }

    @Test
    void testDeleteObjects() {
        // 上传
        String objectKey1 = aliyunUploadService.upload(testFile, testPath);
        String objectKey2 = aliyunUploadService.upload(testFile, testPath);

        // 删除，返回删除失败的文件列表
        ArrayList<String> objectKeys = new ArrayList<>();
        objectKeys.add(objectKey1);
        objectKeys.add(objectKey2);
        Iterable<String> result = aliyunObjectManageService.deleteObjects(objectKeys);

        // 验证
        assertFalse(result.iterator().hasNext());
        assertFalse(aliyunObjectManageService.objectExist(objectKey1));
        assertFalse(aliyunObjectManageService.objectExist(objectKey2));
    }

    @Test
    void testDeleteObjects1() {
        List<String> result = aliyunObjectManageService.deleteObjects(testPath);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCopyObject() {
        String destinationKey = "testDestinationKey.txt";
        boolean result = aliyunObjectManageService.copyObject(testObjectKey, destinationKey);

        // 验证
        Assertions.assertTrue(result);

        // 删除
        assertTrue(aliyunObjectManageService.deleteObject(destinationKey));
    }

    @Test
    void testCopyObject1() {
        String destinationKey = "testDestinationKey.txt";
        boolean result = aliyunObjectManageService.copyObject(bucketName, testObjectKey, bucketName, destinationKey);

        // 验证
        Assertions.assertTrue(result);

        // 删除
        assertTrue(aliyunObjectManageService.deleteObject(destinationKey));
    }

}