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


    /**
     * 下载到指定 File 中
     *
     * @param objectKey  Object 完整路径
     * @param file 指定的本地路径，如果指定的本地文件存在会覆盖，不存在则新建。
     *             如果未指定本地路径，则下载后的文件默认保存到程序所属项目对应本地路径中
     */
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
