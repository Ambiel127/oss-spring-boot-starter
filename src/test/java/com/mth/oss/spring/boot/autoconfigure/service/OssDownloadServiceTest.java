package com.mth.oss.spring.boot.autoconfigure.service;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

class OssDownloadServiceTest {

    private static OssDownloadService ossDownloadService;

    private static String bucketName = "xxxxxx";

    @BeforeAll
    static void init() {
        OssProperties ossProperties = new OssProperties();
        ossProperties.setEndpoint("xxxxxx");
        ossProperties.setAccessKeyId("xxxxxx");
        ossProperties.setAccessKeySecret("xxxxxx");
        ossProperties.setBucketName("xxxxxx");
        ossProperties.setEnable(true);
        ossDownloadService = new OssDownloadService(ossProperties);
    }


    @Test
    void testDownload() {
        String key = "20220213/test123.txt";
        ossDownloadService.download(key, new File("C:\\Users\\ambie\\Desktop\\20220216-001.txt"));
    }
}