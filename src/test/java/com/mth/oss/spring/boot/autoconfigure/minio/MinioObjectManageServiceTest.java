package com.mth.oss.spring.boot.autoconfigure.minio;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.messages.DeleteError;
import io.minio.messages.Item;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MinioObjectManageServiceTest {

    private static MinioObjectManageService minioObjectManageService;

    private static MinioUploadService minioUploadService;

    private static String bucketName;

    private static File testFile = new File("C:\\Users\\ambie\\Desktop\\test.txt");

    private static String testPath = "ossSpringBootStarterTestDir/";

    private static String testObjectKey;

    @BeforeAll
    static void init() {
        OssProperties.Minio ossProperties = new OssProperties.Minio();
        ossProperties.setEndpoint("xxxxxx");
        ossProperties.setAccessKeyId("xxxxxx");
        ossProperties.setAccessKeySecret("xxxxxx");
        ossProperties.setBucketName("xxxxxx");
        ossProperties.setEnable(true);
        minioObjectManageService = new MinioObjectManageService(ossProperties);
        MinioBucketService bucketService = new MinioBucketService(ossProperties);
        minioUploadService = new MinioUploadService(ossProperties, bucketService);
        bucketName = ossProperties.getBucketName();


        // 上传测试文件
        String objectKey = minioUploadService.upload(testFile, testPath);

        // 验证上传是否成功
        assertNotNull(objectKey);
        assertTrue(minioObjectManageService.objectExist(objectKey));

        testObjectKey = objectKey;
    }

    @AfterAll
    static void cleanTestFile() {
        // 删除文件
        assertTrue(minioObjectManageService.deleteObject(testObjectKey));

        // 校验测试目录是否清理干净
        Iterable<Result<Item>> objectListing = minioObjectManageService.listObjects(testPath);
        assertFalse(objectListing.iterator().hasNext());
    }

    /**
     * 打印对象信息
     */
    private void printObjectInfo(Iterable<Result<Item>> objectListing) {
        int size = 0;
        for (Result<Item> item : objectListing) {
            try {
                System.out.println(item.get().objectName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            size++;
        }
        System.out.printf("对象个数：%s个\n", size);
    }

    @Test
    void testObjectExist() {
        boolean result = minioObjectManageService.objectExist(testObjectKey);
        Assertions.assertTrue(result);
    }

    @Test
    void testGetObjectMetadata() {
        StatObjectResponse objectMetadata = (StatObjectResponse) minioObjectManageService.getObjectMetadata(testObjectKey);
        assertEquals(testObjectKey, objectMetadata.object());
    }

    @Test
    void testListObjects() {
        Iterable<Result<Item>> objectListing = minioObjectManageService.listObjects();
        printObjectInfo(objectListing);
        assertNotNull(objectListing);
    }

    @Test
    void testListObjects1() {
        Iterable<Result<Item>> objectListing = minioObjectManageService.listObjects(1);
        printObjectInfo(objectListing);
        assertNotNull(objectListing);
    }

    @Test
    void testListObjects2() {
        Iterable<Result<Item>> objectListing = minioObjectManageService.listObjects(testPath);
        printObjectInfo(objectListing);
        assertNotNull(objectListing);
    }

    @Test
    void testListObjects3() {
        Iterable<Result<Item>> objectListing = minioObjectManageService.listObjects(testPath, 10);
        printObjectInfo(objectListing);
        assertNotNull(objectListing);
    }

    @Test
    void testDeleteObject() {
    }

    @Test
    void testDeleteObjects() {
        // 上传
        String objectKey1 = minioUploadService.upload(testFile, testPath);
        String objectKey2 = minioUploadService.upload(testFile, testPath);

        // 删除，返回删除失败的文件列表
        ArrayList<String> objectKeys = new ArrayList<>();
        objectKeys.add(objectKey1);
        objectKeys.add(objectKey2);
        Iterable<Result<DeleteError>> result = minioObjectManageService.deleteObjects(objectKeys);

        // 验证
        assertFalse(result.iterator().hasNext());
        assertFalse(minioObjectManageService.objectExist(objectKey1));
        assertFalse(minioObjectManageService.objectExist(objectKey2));
    }

    @Test
    void testCopyObject() {
        String destinationKey = "testDestinationKey.txt";
        boolean result = minioObjectManageService.copyObject(testObjectKey, destinationKey);

        // 验证
        Assertions.assertTrue(result);

        // 删除
        assertTrue(minioObjectManageService.deleteObject(destinationKey));
    }

    @Test
    void testCopyObject1() {
        String destinationKey = "testDestinationKey.txt";
        boolean result = minioObjectManageService.copyObject(bucketName, testObjectKey, bucketName, destinationKey);

        // 验证
        Assertions.assertTrue(result);

        // 删除
        assertTrue(minioObjectManageService.deleteObject(destinationKey));
    }

}