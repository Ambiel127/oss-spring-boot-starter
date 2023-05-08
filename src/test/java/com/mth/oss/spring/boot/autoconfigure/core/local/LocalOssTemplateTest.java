package com.mth.oss.spring.boot.autoconfigure.core.local;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Local 本地对象存储服务测试类
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.3
 */
@SpringBootApplication(scanBasePackages = "com.mth.oss.spring.boot.autoconfigure")
@SpringBootTest
@ActiveProfiles("aws")
public class LocalOssTemplateTest {

    private static final File testFile = new File("C:\\Users\\watone\\Desktop\\test.txt");

    private static final File testDownLoadFile = new File("C:\\Users\\watone\\Desktop\\test-download.txt");

    private static final String testObjectKey = "localOssSpringBootStarterTestDir/test.txt";


    @Autowired(required = false)
    private LocalOssTemplate localOssTemplate;

    @Test
    void testLocalOssTemplate() {
        System.out.println("localOssTemplate: " + localOssTemplate.toString());
    }


    // ------------------------------------------------------------
    // ----------------------- upload 上传 ------------------------
    // ------------------------------------------------------------

    @Test
    void testUpload() {
        // 上传
        String key = localOssTemplate.upload(testFile);
        // 验证
        assertFileAndClean(key);
    }

    @Test
    void testUpload1() {
        // 上传
        String key = localOssTemplate.upload(testFile, testObjectKey);
        // 验证
        assertFileAndClean(key);
    }

    @Test
    void testUpload2() throws FileNotFoundException {
        // 上传
        String key = localOssTemplate.upload(new FileInputStream(testFile), testObjectKey);
        // 验证
        assertFileAndClean(key);
    }

    @Test
    void testReplaceUpload() {
        // 上传
        String key = localOssTemplate.replaceUpload(testFile);
        // 验证
        assertFileAndClean(key);
    }

    @Test
    void testReplaceUpload1() {
        // 上传
        String key = localOssTemplate.replaceUpload(testFile, testObjectKey);
        // 验证
        assertFileAndClean(key);
    }


    // ------------------------------------------------------------
    // ---------------------- download 下载 -----------------------
    // ------------------------------------------------------------

    @Test
    void testDownload() {
        // 上传
        String key = localOssTemplate.upload(testFile);

        // 下载
        boolean download = localOssTemplate.download(key, testDownLoadFile.getAbsolutePath());
        assertTrue(download);

        // 验证 + 清理
        assertFileAndClean(key);
        assertTrue(testDownLoadFile.delete());
    }

    @Test
    void testDownload1() {
        // 上传
        String key = localOssTemplate.upload(testFile);

        // 下载
        boolean download = localOssTemplate.download(key, testDownLoadFile);
        assertTrue(download);

        // 验证 + 清理
        assertFileAndClean(key);
        assertTrue(testDownLoadFile.delete());
    }

    @Test
    void testDownload2() {
        // 上传
        String key = localOssTemplate.upload(testFile);

        // 下载
        byte[] download = localOssTemplate.download(key);
        System.out.println(new String(download));

        // 验证 + 清理
        assertTrue(download.length > 0);
        assertFileAndClean(key);
    }

    @Test
    void testDownload3() throws IOException {
        // 上传
        String key = localOssTemplate.upload(testFile);

        // 下载
        FileOutputStream outputStream = new FileOutputStream(testDownLoadFile);
        localOssTemplate.download(key, outputStream);
        outputStream.close();

        // 验证 + 清理
        assertFileAndClean(key);
        assertTrue(testDownLoadFile.delete());
    }

    @Test
    void testDeleteObject() {
        // 上传
        String key = localOssTemplate.upload(testFile);
        // 删除
        assertTrue(localOssTemplate.deleteObject(key));
        // 验证
        assertFalse(localOssTemplate.objectExist(key));
    }

    /**
     * 验证文件并清理文件
     *
     * @param key 存储对象完整路径
     */
    @SneakyThrows
    void assertFileAndClean(String key) {
        System.out.println("开始清理文件：" + key);

        // 验证上传是否成功
        assertNotNull(key);
        assertTrue(localOssTemplate.objectExist(key));

        // 删除文件
        assertTrue(localOssTemplate.deleteObject(key));
    }

}
