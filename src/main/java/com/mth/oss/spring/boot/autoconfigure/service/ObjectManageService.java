package com.mth.oss.spring.boot.autoconfigure.service;

import java.util.List;

/**
 * oss 管理文件
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.1
 */
public interface ObjectManageService {

    /**
     * 判断文件是否存在
     *
     * @param objectKey Object完整路径，不能包含Bucket名称
     * @return 存在true；不存在false
     */
    boolean objectExist(String objectKey);

    /**
     * 判断文件是否存在
     *
     * @param bucketName 存储空间名称
     * @param objectKey  Object完整路径，不能包含Bucket名称
     * @return 存在true；不存在false
     */
    boolean objectExist(String bucketName, String objectKey);

    /**
     * 获取文件元信息
     *
     * @param objectKey Object完整路径，不能包含Bucket名称
     * @return 文件元信息对象
     */
    Object getObjectMetadata(String objectKey);

    /**
     * 列举文件
     *
     * @return 集合文件对象，默认100个
     */
    Iterable<?> listObjects();

    /**
     * 列举文件
     *
     * @param maxKeys 最大个数
     * @return 集合文件对象
     */
    Iterable<?> listObjects(int maxKeys);

    /**
     * 列举文件
     *
     * @param prefix 指定前缀
     * @return 集合文件对象
     */
    Iterable<?> listObjects(String prefix);

    /**
     * 列举文件
     *
     * @param prefix  指定前缀
     * @param maxKeys 最大个数
     * @return 集合文件对象
     */
    Iterable<?> listObjects(String prefix, int maxKeys);

    /**
     * 删除单个文件
     * <p>
     * 如果要删除目录，目录必须为空
     *
     * @param objectKey Object完整路径，不能包含Bucket名称
     * @return 是否删除成功，删除成功true；删除失败false
     */
    boolean deleteObject(String objectKey);

    /**
     * 删除指定的多个文件
     *
     * @param objectKeys Object完整路径集合，不能包含Bucket名称
     * @return 删除失败的文件列表
     */
    Iterable<?> deleteObjects(List<String> objectKeys);

    /**
     * 拷贝文件
     * <p>
     * 将源Bucket中的文件（Object）复制到同一地域下相同或不同目标Bucket中
     *
     * @param sourceKey      源Object完整路径
     * @param destinationKey 目标Object完整路径
     * @return 是否拷贝成功，拷贝成功true；拷贝失败false
     */
    boolean copyObject(String sourceKey, String destinationKey);

    /**
     * 拷贝文件
     * <p>
     * 将源Bucket中的文件（Object）复制到同一地域下相同或不同目标Bucket中
     *
     * @param sourceBucketName      源存储空间名称
     * @param sourceKey             源Object完整路径
     * @param destinationBucketName 目标存储空间名称
     * @param destinationKey        目标Object完整路径
     * @return 是否拷贝成功，拷贝成功true；拷贝失败false
     */
    boolean copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey);

}
