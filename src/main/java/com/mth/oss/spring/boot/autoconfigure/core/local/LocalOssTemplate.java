package com.mth.oss.spring.boot.autoconfigure.core.local;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import com.mth.oss.spring.boot.autoconfigure.handler.OssHandler;
import lombok.RequiredArgsConstructor;

/**
 * 对象存储服务的 Local 本地实现
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.3
 */
@RequiredArgsConstructor
public class LocalOssTemplate implements LocalOssOperations {

    private final OssProperties ossProperties;

    private OssHandler ossHandler;




}
