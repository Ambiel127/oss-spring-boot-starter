package com.mth.oss.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.StringJoiner;

/**
 * oss 配置
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.0
 */
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    private Aliyun aliyun;

    private Minio minio;

    public Aliyun getAliyun() {
        return aliyun;
    }

    public void setAliyun(Aliyun aliyun) {
        this.aliyun = aliyun;
    }

    public Minio getMinio() {
        return minio;
    }

    public void setMinio(Minio minio) {
        this.minio = minio;
    }

    /**
     * Aliyun oss 配置
     */
    public static class Aliyun {
        /**
         * endpoint
         */
        private String endpoint;
        /**
         * accessKeyId
         */
        private String accessKeyId;
        /**
         * accessKeySecret
         */
        private String accessKeySecret;
        /**
         * 存储空间名
         */
        private String bucketName;
        /**
         * 签名URL授权访问有效时间（默认1小时 单位分钟）
         *
         * @since 1.1
         */
        private Integer expiration = 60;
        /**
         * 组件是否开启注入，true时开启
         */
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

        public Integer getExpiration() {
            return expiration;
        }

        public void setExpiration(Integer expiration) {
            this.expiration = expiration;
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
                    .add("expiration='" + expiration + "'")
                    .add("enable=" + enable)
                    .toString();
        }
    }

    /**
     * Minio oss 配置
     *
     * @since 1.1
     */
    public static class Minio {
        /**
         * endpoint
         */
        private String endpoint;
        /**
         * accessKeyId
         */
        private String accessKeyId;
        /**
         * accessKeySecret
         */
        private String accessKeySecret;
        /**
         * 存储空间名
         */
        private String bucketName;
        /**
         * 签名URL授权访问有效时间（默认1小时 单位分钟）
         *
         * @since 1.1
         */
        private Integer expiration = 60;
        /**
         * 组件是否开启注入，true时开启
         */
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

        public Integer getExpiration() {
            return expiration;
        }

        public void setExpiration(Integer expiration) {
            this.expiration = expiration;
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
                    .add("expiration='" + expiration + "'")
                    .add("enable=" + enable)
                    .toString();
        }
    }

}
