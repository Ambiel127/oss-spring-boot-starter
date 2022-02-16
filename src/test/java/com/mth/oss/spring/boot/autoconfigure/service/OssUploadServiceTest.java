package com.mth.oss.spring.boot.autoconfigure.service;

import com.aliyun.oss.model.PutObjectRequest;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Date;

class OssUploadServiceTest {

    private static OssUploadService ossUploadService;

    private static OssBucketService ossBucketService;

    private static String bucketName = "xxxxxx";

    @BeforeAll
    static void init() {
        OssProperties ossProperties = new OssProperties();
        ossProperties.setEndpoint("xxxxxx");
        ossProperties.setAccessKeyId("xxxxxx");
        ossProperties.setAccessKeySecret("xxxxxx");
        ossProperties.setBucketName("xxxxxx");
        ossProperties.setEnable(true);
        ossBucketService = new OssBucketService(ossProperties);
        ossUploadService = new OssUploadService(ossProperties, ossBucketService);
    }

    @Test
    void upload() {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
                                                                 "2022-01-24/20220124-001.txt",
                                                                 new File("C:\\Users\\ambie\\Desktop\\20220124-001.txt"));
        URL upload = ossUploadService.upload(putObjectRequest, null);
        System.out.println(upload);
    }

    @Test
    void uploadAndOverwrite() {
        URL url = ossUploadService.uploadAndOverwrite(
                new File("C:\\Users\\ambie\\Desktop\\20220124-001.txt"),
                null);
        System.out.println(url);
    }

    @Test
    void testUploadAndOverwrite() {
        URL url = ossUploadService.uploadAndOverwrite(
                new File("C:\\Users\\ambie\\Desktop\\20220124-001.txt"),
                "/2022-01-24//",
                null);
        System.out.println(url);
    }

    @Test
    void testUpload() {
        URL upload = ossUploadService.upload(
                new File("C:\\Users\\ambie\\Desktop\\20220124-001.txt"),
                null);
        System.out.println(upload);
    }

    @Test
    void testUpload1() {
        URL upload = ossUploadService.upload(
                new File("C:\\Users\\ambie\\Desktop\\20220124-001.txt"),
                "20220213",
                null);
        System.out.println(upload);
    }


    @Test
    void testUpload2() throws FileNotFoundException {
        URL upload = ossUploadService.upload(
                new FileInputStream("C:\\Users\\ambie\\Desktop\\20220124-001.txt"),
                "20220213/test123.txt",
                null);
        System.out.println(upload);
    }

    @Test
    void generatePresignedUrl() {
        URL url = ossUploadService.generatePresignedUrl("20220213/test123.txt");
        System.out.println(url);
    }

    @Test
    void testGeneratePresignedUrl() {
        URL url = ossUploadService.generatePresignedUrl(
                "20220213/test123.txt",
                new Date(System.currentTimeMillis() + 30 * 1000));
        System.out.println(url);
    }

    @Test
    void testGeneratePresignedUrl1() {
        URL url = ossUploadService.generatePresignedUrl(
                "20220213/test123.txt",
                System.currentTimeMillis() + 30 * 1000);
        System.out.println(url);
    }
}