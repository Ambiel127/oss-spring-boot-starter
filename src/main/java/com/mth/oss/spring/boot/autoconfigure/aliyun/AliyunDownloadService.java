package com.mth.oss.spring.boot.autoconfigure.aliyun;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GetObjectRequest;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import com.mth.oss.spring.boot.autoconfigure.service.DownloadService;

import java.io.File;

/**
 * Aliyun oss 下载文件
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.0
 */
public class AliyunDownloadService implements DownloadService {

    private final OssProperties.Aliyun ossProperties;

    public AliyunDownloadService(OssProperties.Aliyun ossProperties) {
        this.ossProperties = ossProperties;
    }


    @Override
    public void download(String objectKey, File file) {
        if (!ossProperties.getEnable()) {
            return;
        }

        OSS ossClient = getClient();
        try {
            ossClient.getObject(new GetObjectRequest(ossProperties.getBucketName(), objectKey), file);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 构建 oss 客户端
     */
    private OSS getClient() {
        return new OSSClientBuilder()
                .build(ossProperties.getEndpoint(),
                       ossProperties.getAccessKeyId(),
                       ossProperties.getAccessKeySecret());
    }

}
