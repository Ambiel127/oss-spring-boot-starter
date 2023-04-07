package com.mth.oss.spring.boot.autoconfigure;

import com.mth.oss.spring.boot.autoconfigure.aws.OssTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
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
@ConditionalOnWebApplication
@ComponentScan(basePackages = "com.mth.oss.spring.boot.autoconfigure")
public class OssStarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "oss" , name = "enable" , havingValue = "true")
    public OssTemplate awsStorage(OssProperties ossProperties) {
        return new OssTemplate(ossProperties);
    }

}
