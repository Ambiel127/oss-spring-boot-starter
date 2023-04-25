package com.mth.oss.spring.boot.autoconfigure;

import com.amazonaws.services.s3.AmazonS3;
import com.mth.oss.spring.boot.autoconfigure.core.aws.OssTemplate;
import com.mth.oss.spring.boot.autoconfigure.core.local.LocalOssTemplate;
import com.mth.oss.spring.boot.autoconfigure.handler.DefaultOssHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Bean 注入配置
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties(OssProperties.class)
@ComponentScan(basePackages = "com.mth.oss.spring.boot.autoconfigure")
public class OssStarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "oss", name = "enable", havingValue = "true")
    public OssTemplate ossTemplate(AmazonS3 client, OssProperties ossProperties) {
        OssTemplate ossTemplate = new OssTemplate(client, ossProperties);
        ossTemplate.setOssHandler(new DefaultOssHandler());
        return ossTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "oss", name = "enable", havingValue = "true")
    @ConditionalOnExpression(
            "#{ environment['oss.localBasePath'] != null && !''.equals(environment['oss.localBasePath']) }")
    public LocalOssTemplate localOssTemplate(OssProperties ossProperties) {
        LocalOssTemplate localOssTemplate = new LocalOssTemplate(ossProperties);
        localOssTemplate.setOssHandler(new DefaultOssHandler());
        return localOssTemplate;
    }

}
