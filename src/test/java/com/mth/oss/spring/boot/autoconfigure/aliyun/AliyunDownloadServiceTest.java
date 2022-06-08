package com.mth.oss.spring.boot.autoconfigure.aliyun;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class AliyunDownloadServiceTest {

    private static AliyunDownloadService aliyunDownloadService;

    private static AliyunUploadService aliyunUploadService;

    private static AliyunObjectManageService aliyunObjectManageService;

    private static File testFile = new File("C:\\Users\\ambie\\Desktop\\test.txt");


    @BeforeAll
    static void init() {
        OssProperties.Aliyun ossProperties = new OssProperties.Aliyun();
        ossProperties.setEndpoint("xxxxxx");
        ossProperties.setAccessKeyId("xxxxxx");
        ossProperties.setAccessKeySecret("xxxxxx");
        ossProperties.setBucketName("xxxxxx");
        ossProperties.setEnable(true);
        aliyunDownloadService = new AliyunDownloadService(ossProperties);
        AliyunBucketService aliyunBucketService = new AliyunBucketService(ossProperties);
        aliyunUploadService = new AliyunUploadService(ossProperties, aliyunBucketService);
        aliyunObjectManageService = new AliyunObjectManageService(ossProperties);
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
        assertTrue(aliyunObjectManageService.objectExist(objectKey));

        // 删除文件
        assertTrue(aliyunObjectManageService.deleteObject(objectKey));
    }


    @Test
    void testDownload() {
        // 上传
        String objectKey = aliyunUploadService.upload(testFile);

        File destFile = new File("C:\\Users\\ambie\\Desktop\\test-download.txt");
        assertFalse(destFile.exists());

        // 下载
        aliyunDownloadService.download(objectKey, destFile);

        // 验证本地文件
        assertTrue(destFile.exists());

        // 验证oss文件并清理
        assertFileAndClean(objectKey);

        // 清理本地文件
        assertTrue(destFile.delete());
    }
}