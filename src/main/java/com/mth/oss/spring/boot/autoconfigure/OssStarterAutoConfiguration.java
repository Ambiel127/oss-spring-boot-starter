package com.mth.oss.spring.boot.autoconfigure;

/**
 * @author MaTianHao
 * @date 2022/1/8
 */

import com.mth.oss.spring.boot.autoconfigure.aliyun.AliyunBucketService;
import com.mth.oss.spring.boot.autoconfigure.aliyun.AliyunDownloadService;
import com.mth.oss.spring.boot.autoconfigure.aliyun.AliyunObjectManageService;
import com.mth.oss.spring.boot.autoconfigure.aliyun.AliyunUploadService;
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
    public AliyunBucketService ossBucketService(OssProperties ossProperties) {
        return new AliyunBucketService(ossProperties.getAliyun());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "oss.aliyun", name = "enable", havingValue = "true")
    public AliyunUploadService ossUploadService(OssProperties ossProperties, AliyunBucketService aliyunBucketService) {
        return new AliyunUploadService(ossProperties.getAliyun(), aliyunBucketService);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "oss.aliyun", name = "enable", havingValue = "true")
    public AliyunDownloadService ossDownloadService(OssProperties ossProperties) {
        return new AliyunDownloadService(ossProperties.getAliyun());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "oss.aliyun", name = "enable", havingValue = "true")
    public AliyunObjectManageService ossObjectManageService(OssProperties ossProperties) {
        return new AliyunObjectManageService(ossProperties.getAliyun());
    }

}
