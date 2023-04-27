package com.mth.oss.spring.boot.autoconfigure.health;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import com.mth.oss.spring.boot.autoconfigure.core.local.LocalOssTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 健康状态检查
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.3
 */
@Component
@RequiredArgsConstructor
public class LocalOssHealthIndicator implements HealthIndicator {

    private final ObjectProvider<LocalOssTemplate> localOssTemplateProvider;

    private final OssProperties ossProperties;


    @Override
    public Health health() {
        LocalOssTemplate localOssTemplate = localOssTemplateProvider.getIfAvailable();

        if (localOssTemplate == null) {
            return Health.down().build();
        }

        String basePath = ossProperties.getLocalBasePath();

        if (StringUtils.hasText(basePath)) {
            return Health.up().withDetail("basePath", basePath).build();
        } else {
            return Health.down().withDetail("basePath", basePath).build();
        }

    }

}
