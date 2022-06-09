package com.mth.oss.spring.boot.autoconfigure.minio;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import io.minio.PutObjectArgs;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MinioUploadServiceTest {

    private static MinioUploadService minioUploadService;

    private static MinioObjectManageService minioObjectManageService;

    private static String bucketName;

    private static File testFile = new File("C:\\Users\\ambie\\Desktop\\test.txt");

    private static String testPath = "ossSpringBootStarterTestDir/";

    private static HttpClient httpClient;

    @BeforeAll
    static void init() {
        OssProperties.Minio ossProperties = new OssProperties.Minio();
        ossProperties.setEndpoint("xxxxxx");
        ossProperties.setAccessKeyId("xxxxxx");
        ossProperties.setAccessKeySecret("xxxxxx");
        ossProperties.setBucketName("xxxxxx");
        ossProperties.setEnable(true);
        MinioBucketService bucketService = new MinioBucketService(ossProperties);
        minioUploadService = new MinioUploadService(ossProperties, bucketService);
        minioObjectManageService = new MinioObjectManageService(ossProperties);
        bucketName = ossProperties.getBucketName();
        httpClient = HttpClients.createDefault();
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
    void testUpload() {
        // 上传
        String objectKey = minioUploadService.upload(testFile);
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testUpload1() {
        // 上传
        String objectKey = minioUploadService.upload(testFile, testPath);
        // 验证
        assertFileAndClean(objectKey);
    }


    @Test
    void testUpload2() throws FileNotFoundException {
        // 上传
        String objectKey = minioUploadService.upload(new FileInputStream(testFile), testPath + "test123.txt");
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testUpload3() throws IOException {
        // 上传
        FileInputStream fileInputStream = new FileInputStream(testFile);
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(testPath + "test123.txt")
                .stream(fileInputStream, fileInputStream.available(), -1)
                .build();
        String objectKey = minioUploadService.upload(args);
        fileInputStream.close();
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testUploadAndOverwrite() {
        // 上传
        String objectKey = minioUploadService.uploadAndOverwrite(testFile);
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testUploadAndOverwrite1() {
        // 上传
        String objectKey = minioUploadService.uploadAndOverwrite(testFile, testPath);
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testGeneratePresignedUrl() throws IOException {
        // 上传
        String objectKey = minioUploadService.uploadAndOverwrite(testFile);

        // 生成签名URL
        String url = minioUploadService.generatePresignedUrl(objectKey);

        // 验证url生效
        HttpResponse urlResult = httpClient.execute(new HttpGet(url));
        assertEquals(200, urlResult.getStatusLine().getStatusCode());

        // 验证文件并清理
        assertFileAndClean(objectKey);

        // 验证url无效，文件已清理 NoSuchKey
        HttpResponse invalidUrlResult = httpClient.execute(new HttpGet(url));
        assertEquals(404, invalidUrlResult.getStatusLine().getStatusCode());
    }

    @Test
    @Disabled
    void testGeneratePresignedUrl1() throws IOException, InterruptedException {
        // 持续时间 5
        int duration = 5;
        // 单位秒
        TimeUnit unit = TimeUnit.SECONDS;

        // 上传
        String objectKey = minioUploadService.uploadAndOverwrite(testFile);

        // 生成签名url
        String url = minioUploadService.generatePresignedUrl(objectKey, duration, unit);

        // 验证url生效
        HttpResponse urlResult = httpClient.execute(new HttpGet(url));
        assertEquals(200, urlResult.getStatusLine().getStatusCode());

        // 验证url无效，签名过期 AccessDenied
        unit.sleep(duration);
        HttpResponse urlExpireResult = httpClient.execute(new HttpGet(url));
        assertEquals(403, urlExpireResult.getStatusLine().getStatusCode());

        // 验证文件并清理
        assertFileAndClean(objectKey);
    }

    @Test
    @Disabled
    void testGeneratePresignedUrl2() throws IOException, InterruptedException {
        // 持续时间 5s
        long duration = 5L;

        // 上传
        String objectKey = minioUploadService.uploadAndOverwrite(testFile);

        // 生成签名url
        String url = minioUploadService.generatePresignedUrl(objectKey, duration);

        // 验证url生效
        HttpResponse urlResult = httpClient.execute(new HttpGet(url));
        assertEquals(200, urlResult.getStatusLine().getStatusCode());

        // 验证url无效，签名过期 AccessDenied
        TimeUnit.SECONDS.sleep(duration);
        HttpResponse urlExpireResult = httpClient.execute(new HttpGet(url));
        assertEquals(403, urlExpireResult.getStatusLine().getStatusCode());

        // 验证文件并清理
        assertFileAndClean(objectKey);
    }

}