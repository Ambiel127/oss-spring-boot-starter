package com.mth.oss.spring.boot.autoconfigure.handler;

import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

/**
 * 提供操作的扩展点
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.3
 */
public interface OssHandler {

    /**
     * 上传之前
     *
     * @param request 上传请求对象
     */
    void beforeUpload(PutObjectRequest request);

    /**
     * 上传之后
     *
     * @param request 上传请求对象
     * @param result 上传响应对象
     */
    void afterUpload(PutObjectRequest request, PutObjectResult result);

}
