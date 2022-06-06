package com.mth.oss.spring.boot.autoconfigure.aliyun;

import com.aliyun.oss.model.PutObjectRequest;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

class AliyunUploadServiceTest {

    private static AliyunUploadService aliyunUploadService;

    private static AliyunBucketService aliyunBucketService;

    private static String bucketName = "xxxxxx";

    @BeforeAll
    static void init() {
        OssProperties.Aliyun ossProperties = new OssProperties.Aliyun();
        ossProperties.setEndpoint("xxxxxx");
        ossProperties.setAccessKeyId("xxxxxx");
        ossProperties.setAccessKeySecret("xxxxxx");
        ossProperties.setBucketName("xxxxxx");
        ossProperties.setEnable(true);
        aliyunBucketService = new AliyunBucketService(ossProperties);
        aliyunUploadService = new AliyunUploadService(ossProperties, aliyunBucketService);
    }

    @Test
    void upload() {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
                                                                 "2022-01-24/20220124-001.txt",
                                                                 new File("C:\\Users\\ambie\\Desktop\\20220124-001.txt"));
        String upload = aliyunUploadService.upload(putObjectRequest);
        System.out.println(upload);
    }

    @Test
    void uploadAndOverwrite() {
        String url = aliyunUploadService.uploadAndOverwrite(
                new File("C:\\Users\\ambie\\Desktop\\20220124-001.txt"));
        System.out.println(url);
    }

    @Test
    void testUploadAndOverwrite() {
        String url = aliyunUploadService.uploadAndOverwrite(
                new File("C:\\Users\\ambie\\Desktop\\20220124-001.txt"),
                "/2022-01-24//");
        System.out.println(url);
    }

    @Test
    void testUpload() {
        String upload = aliyunUploadService.upload(
                new File("C:\\Users\\ambie\\Desktop\\20220124-001.txt"));
        System.out.println(upload);
    }

    @Test
    void testUpload1() {
        String upload = aliyunUploadService.upload(
                new File("C:\\Users\\ambie\\Desktop\\20220124-001.txt"),
                "20220213");
        System.out.println(upload);
    }


    @Test
    void testUpload2() throws FileNotFoundException {
        String upload = aliyunUploadService.upload(
                new FileInputStream("C:\\Users\\ambie\\Desktop\\20220124-001.txt"),
                "20220213/test123.txt");
        System.out.println(upload);
    }

    @Test
    void generatePresignedUrl() {
        String url = aliyunUploadService.generatePresignedUrl("20220213/test123.txt");
        System.out.println(url);
    }

    @Test
    void testGeneratePresignedUrl() {
        String url = aliyunUploadService.generatePresignedUrl(
                "20220213/test123.txt",
                30,
                TimeUnit.SECONDS);
        System.out.println(url);
    }

    @Test
    void testGeneratePresignedUrl1() {
        String url = aliyunUploadService.generatePresignedUrl(
                "20220213/test123.txt",
                1L);
        System.out.println(url);
    }
}