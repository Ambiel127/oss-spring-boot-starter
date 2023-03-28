package com.mth.oss.spring.boot.autoconfigure.aws;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * aws s3 测试类
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.3
 */
@SpringBootApplication(scanBasePackages = "com.mth.oss.spring.boot.autoconfigure")
@SpringBootTest
@ActiveProfiles("aws")
public class OssTemplateTest {

    private static final File testFile = new File("C:\\Users\\watone\\Desktop\\test.txt");
    private static final File testDownLoadFile = new File("C:\\Users\\watone\\Desktop\\test-download.txt");
    private static final String testObjectKey = "ossSpringBootStarterTestDir/test.pdf";
    private static final HttpClient httpClient = HttpClients.createDefault();

    @Value("${oss.bucket-name}")
    private String bucketName;

    @Resource
    private OssTemplate ossTemplate;


    // ------------------------------------------------------------
    // ----------------------- bucket 管理 ------------------------
    // ------------------------------------------------------------

    @Test
    void testListBuckets() {
        // 异常测试用例
        // Assertions.assertThrows(OSSException.class, () -> awsStorage.listBuckets());

        // 正常测试用例
        List<Bucket> buckets = ossTemplate.listBuckets();
        Assertions.assertNotNull(buckets);
        Assertions.assertNotEquals(0, buckets.size());
    }

    @Test
    void testBucketExist() {
        boolean result = ossTemplate.bucketExist();
        Assertions.assertTrue(result);
    }

    @Test
    void testBucketExist1() {
        boolean result = ossTemplate.bucketExist("do-not-exist-bucketname");
        Assertions.assertFalse(result);
    }

    @Test
    void testCreateBucket() {
        // 异常测试用例
        // Assertions.assertThrows(OSSException.class, () -> awsStorage.createBucket("do-not-exist-bucketname"));

        // 正常测试用例
        Assertions.assertTrue(ossTemplate.createBucket("do-not-exist-bucketname"));

        Assertions.assertTrue(ossTemplate.deleteBucket("do-not-exist-bucketname"));
    }


    // ------------------------------------------------------------
    // ----------------------- upload 上传 ------------------------
    // ------------------------------------------------------------

    @Test
    void testUpload() {
        // 上传
        String objectKey = ossTemplate.upload(testFile);
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testUpload1() {
        // 上传
        String objectKey = ossTemplate.upload(testFile, testObjectKey);
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testUpload2() throws FileNotFoundException {
        // 上传
        String objectKey = ossTemplate.upload(new FileInputStream(testFile), testObjectKey + "test123.txt");
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testUpload3() {
        // 上传
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
                                                                 testObjectKey + "test123.txt" ,
                                                                 testFile);
        String objectKey = ossTemplate.upload(putObjectRequest);
        // 验证
        // assertFileAndClean(objectKey);
    }

    @Test
    void testReplaceUpload() {
        // 上传
        String objectKey = ossTemplate.replaceUpload(testFile);
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testReplaceUpload1() {
        // 上传
        String objectKey = ossTemplate.replaceUpload(testFile, testObjectKey);
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testPresignedUrlForUpload() {
        URL url = ossTemplate.presignedUrlForUpload(testObjectKey);
        System.out.println(url);
        assertNotNull(url);
    }

    @Test
    void testPresignedUrlForUpload2() {
        URL url = ossTemplate.presignedUrlForUpload(testObjectKey, 5, TimeUnit.SECONDS);
        System.out.println(url);
        assertNotNull(url);
    }

    @Test
    void testGeneratePresignedUrl() throws IOException {
        // 上传
        String objectKey = ossTemplate.replaceUpload(testFile);

        // 生成签名URL
        URL url = ossTemplate.presignedUrlForAccess(objectKey);
        System.out.println(url);

        // 验证url生效
        HttpResponse urlResult = httpClient.execute(new HttpGet(url.toString()));
        assertEquals(200, urlResult.getStatusLine().getStatusCode());

        // 验证文件并清理
        assertFileAndClean(objectKey);

        // 验证url无效，文件已清理 NoSuchKey
        HttpResponse invalidUrlResult = httpClient.execute(new HttpGet(url.toString()));
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
        String objectKey = ossTemplate.replaceUpload(testFile);

        // 生成签名url
        URL url = ossTemplate.presignedUrlForAccess(objectKey, duration, unit);
        System.out.println(url);

        // 验证url生效
        HttpResponse urlResult = httpClient.execute(new HttpGet(url.toString()));
        assertEquals(200, urlResult.getStatusLine().getStatusCode());

        // 验证url无效，签名过期 AccessDenied
        unit.sleep(duration + 1);
        HttpResponse urlExpireResult = httpClient.execute(new HttpGet(url.toString()));
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
        String objectKey = ossTemplate.replaceUpload(testFile);

        // 生成签名url
        URL url = ossTemplate.generatePresignedUrl(objectKey, duration, HttpMethod.GET);

        // 验证url生效
        HttpResponse urlResult = httpClient.execute(new HttpGet(url.toString()));
        assertEquals(200, urlResult.getStatusLine().getStatusCode());

        // 验证url无效，签名过期 AccessDenied
        TimeUnit.SECONDS.sleep(duration + 1);
        HttpResponse urlExpireResult = httpClient.execute(new HttpGet(url.toString()));
        assertEquals(403, urlExpireResult.getStatusLine().getStatusCode());

        // 验证文件并清理
        assertFileAndClean(objectKey);
    }


    // ------------------------------------------------------------
    // ---------------------- download 下载 -----------------------
    // ------------------------------------------------------------

    @Test
    void testDownloadToFile() {
        // 上传
        String objectKey = ossTemplate.upload(testFile);

        assertFalse(testDownLoadFile.exists());

        // 下载
        boolean downloadResult = ossTemplate.download(objectKey, testDownLoadFile);

        // 验证本地文件
        assertTrue(downloadResult);

        // 验证oss文件并清理
        assertFileAndClean(objectKey);

        // 清理本地文件
        assertTrue(testDownLoadFile.delete());
    }

    @Test
    void testDownloadToFile2() {
        // 上传
        String objectKey = ossTemplate.upload(testFile);

        assertFalse(testDownLoadFile.exists());

        // 下载
        boolean downloadResult = ossTemplate.download(objectKey, testDownLoadFile);

        // 验证本地文件
        assertTrue(downloadResult);

        // 验证oss文件并清理
        assertFileAndClean(objectKey);

        // 清理本地文件
        assertTrue(testDownLoadFile.delete());
    }

    @Test
    void testDownloadToByteArray() throws IOException {
        // 上传
        String objectKey = ossTemplate.upload(testFile);

        assertFalse(testDownLoadFile.exists());

        // 下载
        byte[] download = ossTemplate.download(objectKey);

        FileInputStream inputStream = new FileInputStream(testFile);
        byte[] origin = new byte[inputStream.available()];
        inputStream.read(origin);

        assertTrue(Arrays.equals(origin, download));
        System.out.println(new String(download));

        // 验证oss文件并清理
        assertFileAndClean(objectKey);
    }

    @Test
    void testDownloadToOutputStream() throws IOException {
        // 上传
        String objectKey = ossTemplate.upload(testFile);

        assertFalse(testDownLoadFile.exists());

        // 下载
        FileOutputStream outputStream = new FileOutputStream(testDownLoadFile);
        ossTemplate.download(objectKey, outputStream);
        outputStream.close();

        // 验证oss文件并清理
        assertFileAndClean(objectKey);

        // 清理本地文件
        assertTrue(testDownLoadFile.exists());
        assertTrue(testDownLoadFile.delete());
    }


    // ------------------------------------------------------------
    // ------------------ object manage 文件管理 -------------------
    // ------------------------------------------------------------

    @Test
    void testObjectExist() {
        // 上传
        String objectKey = ossTemplate.upload(testFile, testObjectKey);

        // 判断文件是否存在
        boolean result = ossTemplate.objectExist(objectKey);
        Assertions.assertTrue(result);

        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testGetObject() {
        // 上传
        String objectKey = ossTemplate.upload(testFile, testObjectKey);

        assertDoesNotThrow(() -> ossTemplate.getObject(objectKey));

        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testListObjects() {
        // 上传
        String objectKey = ossTemplate.upload(testFile, testObjectKey);

        List<S3ObjectSummary> objectListing = ossTemplate.listObjects();
        printObjectInfo(objectListing);
        assertNotNull(objectListing);

        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testListObjects1() {
        // 上传
        String objectKey = ossTemplate.upload(testFile, testObjectKey);

        List<S3ObjectSummary> objectListing = ossTemplate.listObjects(1000);
        printObjectInfo(objectListing);
        assertNotNull(objectListing);

        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testListObjects2() {
        // 上传
        String objectKey = ossTemplate.upload(testFile, testObjectKey);

        List<S3ObjectSummary> objectListing = ossTemplate.listObjects(testObjectKey);
        printObjectInfo(objectListing);
        assertNotNull(objectListing);

        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testListObjects3() {
        // 上传
        String objectKey = ossTemplate.upload(testFile, testObjectKey);

        List<S3ObjectSummary> objectListing = ossTemplate.listObjects(testObjectKey, 10);
        printObjectInfo(objectListing);
        assertNotNull(objectListing);

        // 验证
        assertFileAndClean(objectKey);
    }

    // @Test
    // void testDeleteObjects1() {
    //     // 删除指定目录
    //     List<String> result = awsStorage.deleteObjects(testPath);
    //     assertTrue(result.isEmpty());
    //
    //     // 校验测试目录是否清理干净
    //     Iterable<S3ObjectSummary> objectListing = awsStorage.listObjects(testPath);
    //     assertFalse(objectListing.iterator().hasNext());
    // }

    @Test
    void testDeleteObjects() {
        // 上传
        String objectKey1 = ossTemplate.upload(testFile, testObjectKey);
        String objectKey2 = ossTemplate.upload(testFile, testObjectKey);

        // 删除，返回删除失败的文件列表
        ArrayList<String> objectKeys = new ArrayList<>();
        objectKeys.add(objectKey1);
        objectKeys.add(objectKey2);
        List<DeleteObjectsResult.DeletedObject> result = ossTemplate.deleteObjects(objectKeys);

        // 验证
        assertFalse(result.iterator().hasNext());
        assertFalse(ossTemplate.objectExist(objectKey1));
        assertFalse(ossTemplate.objectExist(objectKey2));
    }

    @Test
    void testCopyObject() {
        // 上传
        String objectKey = ossTemplate.upload(testFile, testObjectKey);

        // 拷贝
        String destinationKey = "testDestinationKey.txt";
        boolean result = ossTemplate.copyObject(objectKey, destinationKey);

        // 验证
        Assertions.assertTrue(result);

        // 删除
        assertTrue(ossTemplate.deleteObject(destinationKey));
        assertFileAndClean(objectKey);
    }

    @Test
    void testCopyObject1() {
        // 上传
        String objectKey = ossTemplate.upload(testFile, testObjectKey);

        // 拷贝
        String destinationKey = "testDestinationKey.txt";
        boolean result = ossTemplate.copyObject(bucketName, objectKey, bucketName, destinationKey);

        // 验证
        Assertions.assertTrue(result);

        // 删除
        assertTrue(ossTemplate.deleteObject(destinationKey));
        assertFileAndClean(objectKey);
    }

    /**
     * 验证文件并清理文件
     *
     * @param objectKey object 完整路径
     */
    void assertFileAndClean(String objectKey) {
        System.out.println("开始清理文件：" + objectKey);

        // 验证上传是否成功
        assertNotNull(objectKey);
        assertTrue(ossTemplate.objectExist(objectKey));

        // 删除文件
        assertTrue(ossTemplate.deleteObject(objectKey));
    }

    /**
     * 打印对象信息
     */
    void printObjectInfo(Iterable<S3ObjectSummary> objectListing) {
        int size = 0;
        for (S3ObjectSummary item : objectListing) {
            System.out.println(item.getKey());
            size++;
        }
        System.out.printf("对象个数：%s个\n" , size);
    }


}
