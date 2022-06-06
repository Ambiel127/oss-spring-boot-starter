package com.mth.oss.spring.boot.autoconfigure.aliyun;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

class AliyunDownloadServiceTest {

    private static AliyunDownloadService aliyunDownloadService;

    private static String bucketName = "xxxxxx";

    @BeforeAll
    static void init() {
        OssProperties.Aliyun ossProperties = new OssProperties.Aliyun();
        ossProperties.setEndpoint("xxxxxx");
        ossProperties.setAccessKeyId("xxxxxx");
        ossProperties.setAccessKeySecret("xxxxxx");
        ossProperties.setBucketName("xxxxxx");
        ossProperties.setEnable(true);
        aliyunDownloadService = new AliyunDownloadService(ossProperties);
    }


    @Test
    void testDownload() {
        String key = "20220213/test123.txt";
        aliyunDownloadService.download(key, new File("C:\\Users\\ambie\\Desktop\\20220216-001.txt"));
    }
}