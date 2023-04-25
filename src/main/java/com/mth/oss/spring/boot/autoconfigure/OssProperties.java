package com.mth.oss.spring.boot.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * oss 配置
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    /**
     * 组件是否开启注入，true时开启
     */
    private Boolean enable;
    /**
     * region
     */
    private String region;
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
     * 签名URL授权访问有效时间（默认1小时 单位秒）
     *
     * @since 1.1
     */
    private Integer expiration = 3600;
    /**
     * S3支持路径（Path）请求风格和虚拟托管（Virtual Hosted）请求风格。
     * Aliyun OSS 基于安全考虑，仅支持虚拟托管访问方式，应设置为false；
     * 其余支持S3协议的客户端可以根据情况设置为true。
     * 只是url的显示不一样。
     *
     * @since 1.3
     */
    private Boolean pathStyleAccess = false;

    /**
     * 本地数据存储路径：使用 Local 本地对象存储服务必填
     *
     * @since 1.3
     */
    private String localBasePath;

}
