package com.mth.oss.spring.boot.autoconfigure;

/**
 * @author MaTianHao
 * @date 2022/1/8
 */

import com.mth.oss.spring.boot.autoconfigure.service.OssBucketService;
import com.mth.oss.spring.boot.autoconfigure.service.OssDownloadService;
import com.mth.oss.spring.boot.autoconfigure.service.OssObjectManageService;
import com.mth.oss.spring.boot.autoconfigure.service.OssUploadService;
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
    @ConditionalOnProperty(prefix = "oss.config", name = "enable", havingValue = "true")
    public OssBucketService ossBucketService(OssProperties ossProperties) {
        return new OssBucketService(ossProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "oss.config", name = "enable", havingValue = "true")
    public OssUploadService ossUploadService(OssProperties ossProperties, OssBucketService ossBucketService) {
        return new OssUploadService(ossProperties, ossBucketService);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "oss.config", name = "enable", havingValue = "true")
    public OssDownloadService ossDownloadService(OssProperties ossProperties) {
        return new OssDownloadService(ossProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "oss.config", name = "enable", havingValue = "true")
    public OssObjectManageService ossObjectManageService(OssProperties ossProperties) {
        return new OssObjectManageService(ossProperties);
    }

}
