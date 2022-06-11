package com.mth.oss.spring.boot.autoconfigure.minio;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.Item;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * minio oss 测试类
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.2
 */
public class MinioStorageTest {

    private static MinioStorage minioStorage;

    private static String bucketName;

    private static final File testFile = new File("C:\\Users\\ambie\\Desktop\\test.txt");

    private static final File testDownLoadFile = new File("C:\\Users\\ambie\\Desktop\\test-download.txt");

    private static final  String testPath = "ossSpringBootStarterTestDir/";

    private static HttpClient httpClient;

    @BeforeAll
    static void init() {
        OssProperties.Minio ossProperties = new OssProperties.Minio();
        ossProperties.setEndpoint("xxxxx");
        ossProperties.setAccessKeyId("xxxxxx");
        ossProperties.setAccessKeySecret("xxxxxx");
        ossProperties.setBucketName("xxxxxx");
        ossProperties.setEnable(true);
        minioStorage = new MinioStorage(ossProperties);
        bucketName = ossProperties.getBucketName();
        httpClient = HttpClients.createDefault();
    }
    
    // ------------------------------------------------------------
    // ----------------------- bucket 管理 ------------------------
    // ------------------------------------------------------------

    @Test
    void testListBuckets() {
        List<Bucket> buckets = minioStorage.listBuckets();
        Assertions.assertNotNull(buckets);
        Assertions.assertNotEquals(0, buckets.size());
    }

    @Test
    void testBucketExist() {
        boolean result = minioStorage.bucketExist();
        Assertions.assertTrue(result);
    }

    @Test
    void testBucketExist1() {
        boolean result = minioStorage.bucketExist("do-not-exist-bucketname");
        Assertions.assertFalse(result);
    }

    @Test
    void testCreateBucket() {
        assertTrue(minioStorage.createBucket("do-not-exist-bucketname"));

        assertTrue(minioStorage.deleteBucket("do-not-exist-bucketname"));
    }

    @Test
    void testDeleteBucket() {

    }
    
    

    // ------------------------------------------------------------
    // ----------------------- upload 上传 ------------------------
    // ------------------------------------------------------------

    @Test
    void testUpload() {
        // 上传
        String objectKey = minioStorage.upload(testFile);
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testUpload1() {
        // 上传
        String objectKey = minioStorage.upload(testFile, testPath);
        // 验证
        assertFileAndClean(objectKey);
    }


    @Test
    void testUpload2() throws FileNotFoundException {
        // 上传
        String objectKey = minioStorage.upload(new FileInputStream(testFile), testPath + "test123.txt");
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
        String objectKey = minioStorage.upload(args);
        fileInputStream.close();
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testUploadAndOverwrite() {
        // 上传
        String objectKey = minioStorage.uploadAndOverwrite(testFile);
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testUploadAndOverwrite1() {
        // 上传
        String objectKey = minioStorage.uploadAndOverwrite(testFile, testPath);
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testGeneratePresignedUrl() throws IOException {
        // 上传
        String objectKey = minioStorage.uploadAndOverwrite(testFile);

        // 生成签名URL
        String url = minioStorage.generatePresignedUrl(objectKey);

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
        String objectKey = minioStorage.uploadAndOverwrite(testFile);

        // 生成签名url
        String url = minioStorage.generatePresignedUrl(objectKey, duration, unit);

        // 验证url生效
        HttpResponse urlResult = httpClient.execute(new HttpGet(url));
        assertEquals(200, urlResult.getStatusLine().getStatusCode());

        // 验证url无效，签名过期 AccessDenied
        unit.sleep(duration + 1);
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
        String objectKey = minioStorage.uploadAndOverwrite(testFile);

        // 生成签名url
        String url = minioStorage.generatePresignedUrl(objectKey, duration);

        // 验证url生效
        HttpResponse urlResult = httpClient.execute(new HttpGet(url));
        assertEquals(200, urlResult.getStatusLine().getStatusCode());

        // 验证url无效，签名过期 AccessDenied
        TimeUnit.SECONDS.sleep(duration + 1);
        HttpResponse urlExpireResult = httpClient.execute(new HttpGet(url));
        assertEquals(403, urlExpireResult.getStatusLine().getStatusCode());

        // 验证文件并清理
        assertFileAndClean(objectKey);
    }
    
    

    // ------------------------------------------------------------
    // ---------------------- download 下载 -----------------------
    // ------------------------------------------------------------

    @Test
    void testDownload() {
        // 上传
        String objectKey = minioStorage.upload(testFile);

        assertFalse(testDownLoadFile.exists());

        // 下载
        minioStorage.download(objectKey, testDownLoadFile);

        // 验证本地文件
        assertTrue(testDownLoadFile.exists());

        // 验证oss文件并清理
        assertFileAndClean(objectKey);

        // 清理本地文件
        assertTrue(testDownLoadFile.delete());
    }



    // ------------------------------------------------------------
    // ------------------ object manage 文件管理 -------------------
    // ------------------------------------------------------------

    @Test
    void testObjectExist() {
        // 上传
        String objectKey = minioStorage.upload(testFile, testPath);

        boolean result = minioStorage.objectExist(objectKey);
        Assertions.assertTrue(result);

        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testGetObjectMetadata() {
        // 上传
        String objectKey = minioStorage.upload(testFile, testPath);

        StatObjectResponse objectMetadata = (StatObjectResponse) minioStorage.getObjectMetadata(objectKey);
        assertEquals(objectKey, objectMetadata.object());

        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testListObjects() {
        // 上传
        String objectKey = minioStorage.upload(testFile, testPath);

        Iterable<Result<Item>> objectListing = minioStorage.listObjects();
        printObjectInfo(objectListing);
        assertNotNull(objectListing);

        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testListObjects1() {
        // 上传
        String objectKey = minioStorage.upload(testFile, testPath);

        Iterable<Result<Item>> objectListing = minioStorage.listObjects(1);
        printObjectInfo(objectListing);
        assertNotNull(objectListing);

        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testListObjects2() {
        // 上传
        String objectKey = minioStorage.upload(testFile, testPath);
        
        Iterable<Result<Item>> objectListing = minioStorage.listObjects(testPath);
        printObjectInfo(objectListing);
        assertNotNull(objectListing);

        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testListObjects3() {
        // 上传
        String objectKey = minioStorage.upload(testFile, testPath);
        
        Iterable<Result<Item>> objectListing = minioStorage.listObjects(testPath, 10);
        printObjectInfo(objectListing);
        assertNotNull(objectListing);

        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testDeleteObject() {
    }

    @Test
    void testDeleteObjects() {
        // 上传
        String objectKey1 = minioStorage.upload(testFile, testPath);
        String objectKey2 = minioStorage.upload(testFile, testPath);

        // 删除，返回删除失败的文件列表
        ArrayList<String> objectKeys = new ArrayList<>();
        objectKeys.add(objectKey1);
        objectKeys.add(objectKey2);
        Iterable<Result<DeleteError>> result = minioStorage.deleteObjects(objectKeys);

        // 验证
        assertFalse(result.iterator().hasNext());
        assertFalse(minioStorage.objectExist(objectKey1));
        assertFalse(minioStorage.objectExist(objectKey2));
    }

    @Test
    void testCopyObject() {
        // 上传
        String objectKey = minioStorage.upload(testFile, testPath);

        String destinationKey = "testDestinationKey.txt";
        boolean result = minioStorage.copyObject(objectKey, destinationKey);

        // 验证
        Assertions.assertTrue(result);

        // 删除
        assertTrue(minioStorage.deleteObject(destinationKey));
        assertFileAndClean(objectKey);
    }

    @Test
    void testCopyObject1() {
        // 上传
        String objectKey = minioStorage.upload(testFile, testPath);

        String destinationKey = "testDestinationKey.txt";
        boolean result = minioStorage.copyObject(bucketName, objectKey, bucketName, destinationKey);

        // 验证
        Assertions.assertTrue(result);

        // 删除
        assertTrue(minioStorage.deleteObject(destinationKey));
        assertFileAndClean(objectKey);
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
        assertTrue(minioStorage.objectExist(objectKey));

        // 删除文件
        assertTrue(minioStorage.deleteObject(objectKey));
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

}
