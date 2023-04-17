package com.mth.oss.spring.boot.autoconfigure.health;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import com.mth.oss.spring.boot.autoconfigure.aws.OssTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * 健康状态检查
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.3
 */
@Component
@RequiredArgsConstructor
public class OssHealthIndicator implements HealthIndicator {

    private final OssTemplate ossTemplate;

    private final OssProperties ossProperties;

    @Override
    public Health health() {
        if (ossTemplate == null) {
            return Health.down().build();
        }

        String bucketName = ossProperties.getBucketName();

        boolean bucketExist;
        try {
            bucketExist = ossTemplate.bucketExist(bucketName);
        } catch (Exception e) {
            return Health.down(e).withDetail("bucketName", bucketName).build();
        }

        if (bucketExist) {
            return Health.up().withDetail("bucketName", bucketName).build();
        } else {
            return Health.down().withDetail("bucketName", bucketName).build();
        }

    }

}
