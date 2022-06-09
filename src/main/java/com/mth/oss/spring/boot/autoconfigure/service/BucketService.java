package com.mth.oss.spring.boot.autoconfigure.service;

import java.util.List;

/**
 * 存储空间 bucket 操作
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.1
 */
public interface BucketService {


    /**
     * 列举存储空间
     *
     * @return 桶列表
     */
    List<?> listBuckets();

    /**
     * 判断存储空间是否存在
     *
     * @return 存在true；不存在false
     */
    boolean bucketExist();

    /**
     * 判断存储空间是否存在
     *
     * @param bucketName 桶名称
     * @return 存在true；不存在false
     */
    boolean bucketExist(String bucketName);

    /**
     * 创建存储空间
     *
     * @param bucketName 桶名称
     * @return 是否创建成功，创建成功true；创建失败false
     */
    boolean createBucket(String bucketName);


    /**
     * 删除存储空间
     *
     * @param bucketName 桶名称
     * @return 是否删除成功，删除成功true；删除失败false
     */
    boolean deleteBucket(String bucketName);


}
