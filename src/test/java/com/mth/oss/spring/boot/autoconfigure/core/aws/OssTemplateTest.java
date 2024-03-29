package com.mth.oss.spring.boot.autoconfigure.core.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.SneakyThrows;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * aws s3 对象存储服务测试类
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
    private static final String testObjectKey = "ossSpringBootStarterTestDir/test.txt";
    private static final HttpClient httpClient = HttpClients.createDefault();

    @Value("${oss.bucket-name}")
    private String bucketName;

    @Autowired(required = false)
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
        assertFileAndClean(objectKey);
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
    void testGeneratePresignedUrl2() throws IOException {
        // 上传
        String objectKey = ossTemplate.replaceUpload(testFile);

        // 生成签名url
        URL url = ossTemplate.presignedUrlForAccess(objectKey);

        // 验证url生效
        HttpResponse urlResult = httpClient.execute(new HttpGet(url.toString()));
        assertEquals(200, urlResult.getStatusLine().getStatusCode());

        // 验证文件并清理
        assertFileAndClean(objectKey);
    }


    // ------------------------------------------------------------
    // ---------------- multipart upload 分片上传 ------------------
    // ------------------------------------------------------------

    @Test
    void testInitAndAbortMultipartUpload() {
        // initial multipart upload
        InitiateMultipartUploadResult initResponse = ossTemplate.initMultipartUpload(testObjectKey);
        assertNotNull(initResponse);
        assertNotNull(initResponse.getUploadId());
        System.out.println("uploadId: " + initResponse.getUploadId());

        URL url = ossTemplate.presignedUrlForMultipartUpload(
                initResponse.getUploadId(), 1, testObjectKey, 5, TimeUnit.MINUTES);
        System.out.println("upload url: " + url);

        // abort multipart upload
        ossTemplate.abortMultipartUpload(initResponse.getUploadId(), testObjectKey);
        System.out.println("abort multipart upload");

        // list multipart upload
        MultipartUploadListing uploadListing = ossTemplate.listMultipartUploads();
        System.out.println(uploadListing.getMultipartUploads().size());

        for (MultipartUpload item : uploadListing.getMultipartUploads()) {
            System.out.println("uploadId: " + item.getUploadId());
            System.out.println("key: " + item.getKey());
            System.out.println("init date: " + item.getInitiated());
        }
    }

    @Test
    void testMultipartUpload() {
        long contentLength = testFile.length();
        // 5MB
        long partSize = 5 * 1024 * 1024;

        List<PartETag> partETags = new ArrayList<>();

        // initial multipart upload
        InitiateMultipartUploadResult initResponse = ossTemplate.initMultipartUpload(testObjectKey, "text/plain");
        assertNotNull(initResponse);
        assertNotNull(initResponse.getUploadId());
        System.out.println("uploadId: " + initResponse.getUploadId());

        long filePosition = 0;

        // upload file parts
        for (int i = 1; filePosition < contentLength; i++) {
            partSize = Math.min(partSize, (contentLength - filePosition));

            UploadPartRequest request = new UploadPartRequest()
                    .withBucketName(bucketName)
                    .withKey(testObjectKey)
                    .withUploadId(initResponse.getUploadId())
                    .withPartNumber(i)
                    .withFileOffset(filePosition)
                    .withFile(testFile)
                    .withPartSize(partSize);
            UploadPartResult uploadResponse = ossTemplate.uploadPart(request);
            partETags.add(uploadResponse.getPartETag());

            filePosition += partSize;
            System.out.println("分片" + i + "上传完毕");
        }

        // list part
        PartListing partListing = ossTemplate.listParts(initResponse.getUploadId(), testObjectKey);
        if (partListing.getParts().size() == partETags.size()) {
            System.out.println("分片上传完毕，开始合并");
        }

        // complete multipart upload
        CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest()
                .withBucketName(bucketName)
                .withKey(testObjectKey)
                .withUploadId(initResponse.getUploadId())
                .withPartETags(partETags);
        CompleteMultipartUploadResult completeResult = ossTemplate.completeMultipartUpload(request);
        System.out.println("complete result: " + completeResult);

        // 清理文件
        assertFileAndClean(testObjectKey);
    }

    @SneakyThrows
    @Test
    void testMultipartUpload2() {
        FileInputStream fis = new FileInputStream(testFile);
        long contentLength = testFile.length();
        long partSize = 5 * 1024 * 1024;
        byte[] bytes = new byte[(int) partSize];

        int partNums = 0;

        // initial multipart upload
        InitiateMultipartUploadResult initResponse = ossTemplate.initMultipartUpload(testObjectKey);
        assertNotNull(initResponse);
        assertNotNull(initResponse.getUploadId());
        System.out.println("uploadId: " + initResponse.getUploadId());

        long filePosition = 0;

        // upload file parts
        for (int i = 1; filePosition < contentLength; i++) {
            partSize = Math.min(partSize, (contentLength - filePosition));

            URL url = ossTemplate.presignedUrlForMultipartUpload(initResponse.getUploadId(), i, testObjectKey);
            System.out.println(url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setRequestMethod("PUT");
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            int read = fis.read(bytes, 0, (int) partSize);
            out.write(bytes, 0, read);
            out.close();

            connection.getResponseCode();
            System.out.println("HTTP response code is " + connection.getResponseCode());

            filePosition += partSize;
            partNums += 1;
            System.out.println("分片" + i + "上传完毕");
        }

        // list part
        PartListing partListing = ossTemplate.listParts(initResponse.getUploadId(), testObjectKey);
        if (partListing.getParts().size() == partNums) {
            System.out.println("分片上传完毕，开始合并");
        }

        // complete multipart upload
        List<PartETag> partETags = partListing.getParts().stream()
                .map(item -> new PartETag(item.getPartNumber(), item.getETag()))
                .collect(Collectors.toList());
        CompleteMultipartUploadResult completeResult = ossTemplate.completeMultipartUpload(
                initResponse.getUploadId(), testObjectKey, partETags);
        System.out.println("complete result: " + completeResult);

        // 清理文件
        assertFileAndClean(testObjectKey);
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
        boolean downloadResult = ossTemplate.download(objectKey, testDownLoadFile.getAbsolutePath());

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
        System.out.println(new String(download));

        // 验证oss文件并清理
        assertTrue(download.length > 0);
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

    @Test
    void testDeleteObject() {
        // 上传
        String objectKey = ossTemplate.upload(testFile, testObjectKey);
        // 删除
        ossTemplate.deleteObject(bucketName, objectKey);

        // 验证
        assertFalse(ossTemplate.objectExist(objectKey));
    }

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

    @Test
    void testMoveObject() {
        // 上传
        String objectKey = ossTemplate.upload(testFile, testObjectKey);

        // 移动
        String destinationKey = "testDestinationKey.txt";
        boolean result = ossTemplate.moveObject(objectKey, destinationKey);

        // 验证
        Assertions.assertTrue(result);
        assertFileAndClean(destinationKey);
    }

    @Test
    void testMoveObject1() {
        // 上传
        String objectKey = ossTemplate.upload(testFile, testObjectKey);

        // 移动
        String destinationKey = "testDestinationKey.txt";
        boolean result = ossTemplate.moveObject(bucketName, objectKey, bucketName, destinationKey);

        // 验证
        Assertions.assertTrue(result);
        assertFileAndClean(destinationKey);
    }

    @Test
    void testClientInstance() {
        // 直接使用客户端 api
        AmazonS3 client = ossTemplate.getClientInstance();
        client.putObject(bucketName, testObjectKey, testFile);

        // 验证并清理
        assertFileAndClean(testObjectKey);
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
