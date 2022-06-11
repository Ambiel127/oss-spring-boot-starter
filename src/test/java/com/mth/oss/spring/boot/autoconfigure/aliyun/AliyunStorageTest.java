package com.mth.oss.spring.boot.autoconfigure.aliyun;

import com.aliyun.oss.model.*;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
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
 * aliyun oss 测试类
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.2
 */
public class AliyunStorageTest {

    private static AliyunStorage aliyunStorage;

    private static String bucketName;

    private static final File testFile = new File("C:\\Users\\ambie\\Desktop\\test.txt");

    private static final File testDownLoadFile = new File("C:\\Users\\ambie\\Desktop\\test-download.txt");

    private static final String testPath = "ossSpringBootStarterTestDir/";

    private static HttpClient httpClient;

    @BeforeAll
    static void init() {
        OssProperties.Aliyun ossProperties = new OssProperties.Aliyun();
        ossProperties.setEndpoint("xxxxxx");
        ossProperties.setAccessKeyId("xxxxxx");
        ossProperties.setAccessKeySecret("xxxxxx");
        ossProperties.setBucketName("xxxxxx");
        ossProperties.setEnable(true);
        aliyunStorage = new AliyunStorage(ossProperties);
        bucketName = ossProperties.getBucketName();
        httpClient = HttpClients.createDefault();
    }

    // ------------------------------------------------------------
    // ----------------------- bucket 管理 ------------------------
    // ------------------------------------------------------------

    @Test
    void testListBuckets() {
        // 异常测试用例
        // Assertions.assertThrows(OSSException.class, () -> aliyunStorage.listBuckets());

        // 正常测试用例
        List<Bucket> buckets = aliyunStorage.listBuckets();
        Assertions.assertNotNull(buckets);
        Assertions.assertNotEquals(0, buckets.size());
    }

    @Test
    void testBucketExist() {
        boolean result = aliyunStorage.bucketExist();
        Assertions.assertTrue(result);
    }

    @Test
    void testBucketExist1() {
        boolean result = aliyunStorage.bucketExist("do-not-exist-bucketname");
        Assertions.assertFalse(result);
    }

    @Test
    void testCreateBucket() {
        // 异常测试用例
        // Assertions.assertThrows(OSSException.class, () -> aliyunStorage.createBucket("do-not-exist-bucketname"));

        // 正常测试用例
        Assertions.assertTrue(aliyunStorage.createBucket("do-not-exist-bucketname"));

        Assertions.assertTrue(aliyunStorage.deleteBucket("do-not-exist-bucketname"));
    }

    @Test
    void testDeleteBucket() {
        // 异常测试用例
        // Assertions.assertThrows(OSSException.class, () -> aliyunStorage.deleteBucket("do-not-exist-bucketname"));

        // 正常测试用例
        // boolean result = aliyunStorage.deleteBucket("do-not-exist-bucketname");
        // Assertions.assertTrue(result);
    }

    @Test
    void testGetBucketLocation() {
        String bucketLocation = aliyunStorage.getBucketLocation(bucketName);
        Assertions.assertNotNull(bucketLocation);
    }

    @Test
    void testGetBucketInfo() {
        BucketInfo bucketInfo = aliyunStorage.getBucketInfo(bucketName);
        Assertions.assertNotNull(bucketInfo);
    }

    @Test
    void testSetBucketAcl() {
        CannedAccessControlList access = CannedAccessControlList.Private;
        AccessControlList accessControlList = aliyunStorage.setBucketAcl(bucketName, access);

        Assertions.assertNotNull(accessControlList);
        Assertions.assertEquals(access, accessControlList.getCannedACL());
    }

    @Test
    void testGetBucketAcl() {
        AccessControlList bucketAcl = aliyunStorage.getBucketAcl(bucketName);
        Assertions.assertNotNull(bucketAcl);
    }
    


    // ------------------------------------------------------------
    // ----------------------- upload 上传 ------------------------
    // ------------------------------------------------------------

    @Test
    void testUpload() {
        // 上传
        String objectKey = aliyunStorage.upload(testFile);
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testUpload1() {
        // 上传
        String objectKey = aliyunStorage.upload(testFile, testPath);
        // 验证
        assertFileAndClean(objectKey);
    }


    @Test
    void testUpload2() throws FileNotFoundException {
        // 上传
        String objectKey = aliyunStorage.upload(new FileInputStream(testFile), testPath + "test123.txt");
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testUpload3() {
        // 上传
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
                                                                 testPath + "test123.txt",
                                                                 testFile);
        String objectKey = aliyunStorage.upload(putObjectRequest);
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testUploadAndOverwrite() {
        // 上传
        String objectKey = aliyunStorage.uploadAndOverwrite(testFile);
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testUploadAndOverwrite1() {
        // 上传
        String objectKey = aliyunStorage.uploadAndOverwrite(testFile, testPath);
        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testGeneratePresignedUrl() throws IOException {
        // 上传
        String objectKey = aliyunStorage.uploadAndOverwrite(testFile);

        // 生成签名URL
        String url = aliyunStorage.generatePresignedUrl(objectKey);

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
        String objectKey = aliyunStorage.uploadAndOverwrite(testFile);

        // 生成签名url
        String url = aliyunStorage.generatePresignedUrl(objectKey, duration, unit);

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
        String objectKey = aliyunStorage.uploadAndOverwrite(testFile);

        // 生成签名url
        String url = aliyunStorage.generatePresignedUrl(objectKey, duration);

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
        String objectKey = aliyunStorage.upload(testFile);

        assertFalse(testDownLoadFile.exists());

        // 下载
        aliyunStorage.download(objectKey, testDownLoadFile);

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
        String objectKey = aliyunStorage.upload(testFile, testPath);

        // 判断文件是否存在
        boolean result = aliyunStorage.objectExist(objectKey);
        Assertions.assertTrue(result);

        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testGetObjectMetadata() {
        // 上传
        String objectKey = aliyunStorage.upload(testFile, testPath);

        assertDoesNotThrow(() -> aliyunStorage.getObjectMetadata(objectKey));

        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testListObjects() {
        // 上传
        String objectKey = aliyunStorage.upload(testFile, testPath);

        Iterable<OSSObjectSummary> objectListing = aliyunStorage.listObjects();
        printObjectInfo(objectListing);
        assertNotNull(objectListing);

        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testListObjects1() {
        // 上传
        String objectKey = aliyunStorage.upload(testFile, testPath);

        Iterable<OSSObjectSummary> objectListing = aliyunStorage.listObjects(1000);
        printObjectInfo(objectListing);
        assertNotNull(objectListing);

        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testListObjects2() {
        // 上传
        String objectKey = aliyunStorage.upload(testFile, testPath);

        Iterable<OSSObjectSummary> objectListing = aliyunStorage.listObjects(testPath);
        printObjectInfo(objectListing);
        assertNotNull(objectListing);

        // 验证
        assertFileAndClean(objectKey);
    }

    @Test
    void testListObjects3() {
        // 上传
        String objectKey = aliyunStorage.upload(testFile, testPath);

        Iterable<OSSObjectSummary> objectListing = aliyunStorage.listObjects(testPath, 10);
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
        String objectKey1 = aliyunStorage.upload(testFile, testPath);
        String objectKey2 = aliyunStorage.upload(testFile, testPath);

        // 删除，返回删除失败的文件列表
        ArrayList<String> objectKeys = new ArrayList<>();
        objectKeys.add(objectKey1);
        objectKeys.add(objectKey2);
        Iterable<String> result = aliyunStorage.deleteObjects(objectKeys);

        // 验证
        assertFalse(result.iterator().hasNext());
        assertFalse(aliyunStorage.objectExist(objectKey1));
        assertFalse(aliyunStorage.objectExist(objectKey2));
    }

    @Test
    void testDeleteObjects1() {
        // 删除指定目录
        List<String> result = aliyunStorage.deleteObjects(testPath);
        assertTrue(result.isEmpty());

        // 校验测试目录是否清理干净
        Iterable<OSSObjectSummary> objectListing = aliyunStorage.listObjects(testPath);
        assertFalse(objectListing.iterator().hasNext());
    }

    @Test
    void testCopyObject() {
        // 上传
        String objectKey = aliyunStorage.upload(testFile, testPath);

        // 拷贝
        String destinationKey = "testDestinationKey.txt";
        boolean result = aliyunStorage.copyObject(objectKey, destinationKey);

        // 验证
        Assertions.assertTrue(result);

        // 删除
        assertTrue(aliyunStorage.deleteObject(destinationKey));
        assertFileAndClean(objectKey);
    }

    @Test
    void testCopyObject1() {
        // 上传
        String objectKey = aliyunStorage.upload(testFile, testPath);

        // 拷贝
        String destinationKey = "testDestinationKey.txt";
        boolean result = aliyunStorage.copyObject(bucketName, objectKey, bucketName, destinationKey);

        // 验证
        Assertions.assertTrue(result);

        // 删除
        assertTrue(aliyunStorage.deleteObject(destinationKey));
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
        assertTrue(aliyunStorage.objectExist(objectKey));

        // 删除文件
        assertTrue(aliyunStorage.deleteObject(objectKey));
    }

    /**
     * 打印对象信息
     */
    private void printObjectInfo(Iterable<OSSObjectSummary> objectListing) {
        int size = 0;
        for (OSSObjectSummary item : objectListing) {
            System.out.println(item.getKey());
            size++;
        }
        System.out.printf("对象个数：%s个\n", size);
    }
    
    
}
