package com.mth.oss.spring.boot.autoconfigure.handler;

import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
                progressEvent -> log.info("Transferred bytes: " + progressEvent.getBytesTransferred()));
    }

    @Override
    public void afterUpload(PutObjectRequest request, PutObjectResult result) {
        log.info("after file upload: {}", request.getKey());
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

}
