package com.mth.oss.spring.boot.autoconfigure.core.local;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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

    @Autowired(required = false)
    private LocalOssTemplate localOssTemplate;

    @Test
    void testLocalOssTemplate() {
        System.out.println("localOssTemplate: " + localOssTemplate.toString());
    }

}
