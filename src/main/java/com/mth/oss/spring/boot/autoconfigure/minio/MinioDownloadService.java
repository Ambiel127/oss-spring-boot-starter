package com.mth.oss.spring.boot.autoconfigure.minio;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import com.mth.oss.spring.boot.autoconfigure.service.DownloadService;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Minio oss 下载文件
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.1
 */
public class MinioDownloadService implements DownloadService {

    private final OssProperties.Minio ossProperties;

    public MinioDownloadService(OssProperties.Minio ossProperties) {
        this.ossProperties = ossProperties;
    }

    @Override
    public void download(String objectKey, File file) {
        if (!ossProperties.getEnable()) {
            return;
        }

        MinioClient minioClient = getClient();

        GetObjectArgs args = GetObjectArgs.builder()
                .bucket(ossProperties.getBucketName())
                .object(objectKey)
                .build();

        // 以流的形式下载，再写入到文件
        try (GetObjectResponse response = minioClient.getObject(args);
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {

            IOUtils.copy(response, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建 oss 客户端
     */
    private MinioClient getClient() {
        return MinioClient.builder()
                .credentials(ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret())
                .endpoint(ossProperties.getEndpoint())
                .build();
    }

}
