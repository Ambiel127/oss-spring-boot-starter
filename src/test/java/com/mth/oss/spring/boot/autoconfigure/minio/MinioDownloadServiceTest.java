package com.mth.oss.spring.boot.autoconfigure.minio;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class MinioDownloadServiceTest {

    private static MinioDownloadService minioDownloadService;

    private static MinioUploadService minioUploadService;

    private static MinioObjectManageService minioObjectManageService;

    private static File testFile = new File("C:\\Users\\ambie\\Desktop\\test.txt");


    @BeforeAll
    static void init() {
        OssProperties.Minio ossProperties = new OssProperties.Minio();
        ossProperties.setEndpoint("xxxxxx");
        ossProperties.setAccessKeyId("xxxxxx");
        ossProperties.setAccessKeySecret("xxxxxx");
        ossProperties.setBucketName("xxxxxx");
        ossProperties.setEnable(true);
        minioDownloadService = new MinioDownloadService(ossProperties);
        MinioBucketService bucketService = new MinioBucketService(ossProperties);
        minioUploadService = new MinioUploadService(ossProperties, bucketService);
        minioObjectManageService = new MinioObjectManageService(ossProperties);
    }

    /**
     * 验证文件并清理文件
     *
     * @param objectKey object 完整路径
     */
    private void assertFileAndClean(String objectKey) {
        System.out.println(objectKey);

        // 验证上传是否成功
        assertNotNull(objectKey);
        assertTrue(minioObjectManageService.objectExist(objectKey));

        // 删除文件
        assertTrue(minioObjectManageService.deleteObject(objectKey));
    }


    @Test
    void testDownload() {
        // 上传
        String objectKey = minioUploadService.upload(testFile);

        File destFile = new File("C:\\Users\\ambie\\Desktop\\test-download.txt");
        assertFalse(destFile.exists());

        // 下载
        minioDownloadService.download(objectKey, destFile);

        // 验证本地文件
        assertTrue(destFile.exists());

        // 验证oss文件并清理
        assertFileAndClean(objectKey);

        // 清理本地文件
        assertTrue(destFile.delete());
    }
}