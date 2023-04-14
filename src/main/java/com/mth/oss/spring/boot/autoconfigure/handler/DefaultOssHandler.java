package com.mth.oss.spring.boot.autoconfigure.handler;

import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
}