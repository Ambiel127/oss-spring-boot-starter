package com.mth.oss.spring.boot.autoconfigure;

import com.mth.oss.spring.boot.autoconfigure.aliyun.AliyunStorage;
import com.mth.oss.spring.boot.autoconfigure.minio.MinioStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean 注入配置
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnWebApplication
public class OssStarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "oss.aliyun", name = "enable", havingValue = "true")
    public AliyunStorage aliyunStorage(OssProperties ossProperties) {
        return new AliyunStorage(ossProperties.getAliyun());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "oss.minio", name = "enable", havingValue = "true")
    public MinioStorage minioStorage(OssProperties ossProperties) {
        return new MinioStorage(ossProperties.getMinio());
    }

}
