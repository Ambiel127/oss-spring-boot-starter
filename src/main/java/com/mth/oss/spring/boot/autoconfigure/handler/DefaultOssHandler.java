package com.mth.oss.spring.boot.autoconfigure.handler;

import com.amazonaws.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;

/**
 * 扩展点的默认实现
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.3
 */
@Slf4j
@Component
public class DefaultOssHandler implements OssHandler {

    @Override
    public void beforeUpload(PutObjectRequest request) {
        log.info("before file upload: {}", request.getKey());

        request.setGeneralProgressListener(
                progressEvent -> log.info("Upload bytes: " + progressEvent.getBytesTransferred()));
    }

    @Override
    public void afterUpload(PutObjectRequest request, PutObjectResult result) {
        log.info("after file upload: {}", request.getKey());
    }

    @Override
    public void beforeDownload(GetObjectRequest request) {
        log.info("before file download: {}", request.getKey());

        request.setGeneralProgressListener(
                progressEvent -> log.info("Download bytes: " + progressEvent.getBytesTransferred()));
    }

    @Override
    public void afterDownload(GetObjectRequest request, ObjectMetadata metadata) {
        log.info("after file download: {}", request.getKey());
    }

    @Override
    public void beforeBucketDelete(String bucketName) {
        log.info("before bucket delete: {}", bucketName);
    }

    @Override
    public void afterBucketDelete(String bucketName) {
        log.info("after bucket delete: {}", bucketName);
    }

    @Override
    public void beforeObjectDelete(List<String> objects) {
        log.info("before file delete: {}", objects);
    }

    @Override
    public void afterObjectDelete(List<String> objects) {
        log.info("after file delete: {}", objects);
    }

    @Override
    public void beforeGeneratePresignedUrl(GeneratePresignedUrlRequest request) {
        log.info("before generatePresignedUrl: {}", request.getKey());
    }

    @Override
    public void afterGeneratePresignedUrl(GeneratePresignedUrlRequest request, URL url) {
        log.info("after generatePresignedUrl: {}", request.getKey());
    }
}
