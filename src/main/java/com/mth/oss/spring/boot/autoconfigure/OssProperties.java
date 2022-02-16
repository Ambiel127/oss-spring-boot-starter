package com.mth.oss.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.StringJoiner;

/**
 * oss客户端连接配置
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.0
 */
@ConfigurationProperties(prefix = "oss.config")
public class OssProperties {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private Boolean enable;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OssProperties.class.getSimpleName() + "[", "]")
                .add("endpoint='" + endpoint + "'")
                .add("accessKeyId='" + accessKeyId + "'")
                .add("accessKeySecret='" + accessKeySecret + "'")
                .add("bucketName='" + bucketName + "'")
                .add("enable=" + enable)
                .toString();
    }
}
