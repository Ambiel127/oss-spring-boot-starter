package com.mth.oss.spring.boot.autoconfigure.core.local;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private static final String testPathName = "localOssSpringBootStarterTestDir/test.txt";


    @Autowired(required = false)
    private LocalOssTemplate localOssTemplate;

    @Test
    void testLocalOssTemplate() {
        System.out.println("localOssTemplate: " + localOssTemplate.toString());
    }

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
        String key = localOssTemplate.upload(testFile, testPathName);
        // 验证
        assertFileAndClean(key);
    }

    @Test
    void testUpload2() throws FileNotFoundException {
        // 上传
        String key = localOssTemplate.upload(new FileInputStream(testFile), testPathName);
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
        String key = localOssTemplate.replaceUpload(testFile, testPathName);
        // 验证
        assertFileAndClean(key);
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
        // assertTrue(localOssTemplate.deleteObject(key));
        assertTrue(localOssTemplate.getObject(key).delete());
    }

}
