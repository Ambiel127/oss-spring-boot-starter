package com.mth.oss.spring.boot.autoconfigure;

/**
 * @author MaTianHao
 * @date 2022/1/8
 */

import com.mth.oss.spring.boot.autoconfigure.aliyun.AliyunStorage;
import com.mth.oss.spring.boot.autoconfigure.minio.MinioStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
