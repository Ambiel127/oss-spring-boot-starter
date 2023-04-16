package com.mth.oss.spring.boot.autoconfigure.handler;

import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import java.net.URL;
import java.util.List;

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
     * @param result  上传响应对象
     */
    void afterUpload(PutObjectRequest request, PutObjectResult result);

    /**
     * 存储空间删除之前
     *
     * @param bucketName 桶名称
     */
    void beforeBucketDelete(String bucketName);

    /**
     * 存储空间删除之后
     *
     * @param bucketName 桶名称
     */
    void afterBucketDelete(String bucketName);

    /**
     * 文件删除之前
     *
     * @param objects object路径集合
     */
    void beforeObjectDelete(List<String> objects);

    /**
     * 文件删除之后
     *
     * @param objects object路径集合
     */
    void afterObjectDelete(List<String> objects);

    /**
     * 生成预签名 URL 之前
     *
     * @param request 请求对象
     */
    void beforeGeneratePresignedUrl(GeneratePresignedUrlRequest request);

    /**
     * 生成预签名 URL 之后
     *
     * @param request 请求对象
     * @param url 授权 URL 对象
     */
    void afterGeneratePresignedUrl(GeneratePresignedUrlRequest request, URL url);

}
