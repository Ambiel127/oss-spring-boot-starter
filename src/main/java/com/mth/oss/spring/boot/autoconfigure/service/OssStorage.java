package com.mth.oss.spring.boot.autoconfigure.service;

import org.springframework.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 对象存储服务接口
 * <p>
 * 包含 bucket管理、存储对象管理、上传、下载、URL 授权访问等常用操作
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.2
 */
public interface OssStorage {

    /**
     * 时间戳字符串格式化 yyyyMMddHHmmssSSS
     */
    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * 日期字符串格式化 yyyyMMdd
     */
    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");



    // ------------------------------------------------------------
    // ----------------------- bucket 管理 ------------------------
    // ------------------------------------------------------------

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



    // ------------------------------------------------------------
    // ----------------------- upload 上传 ------------------------
    // ------------------------------------------------------------

    /**
     * 通用上传文件
     * <p>
     * 如 file 文件名为 temp.txt，则默认 Object 完整路径为 "temp_yyyyMMddHHmmssSSS.txt"
     * 场景：通用业务上传，Object 路径默认添加时间戳，避免重名导致文件覆盖
     *
     * @param file 文件
     * @return 存储对象名称，含相对路径
     */
    String upload(File file);

    /**
     * 通用上传文件
     * <p>
     * - 如 file 文件名为 temp.txt，则默认 Object 完整路径为 "temp_yyyyMMddHHmmssSSS.txt"
     * - path 路径为 null 时，前缀默认为 yyyyMMdd 日期文件夹
     * 场景：通用业务上传，Object 路径默认添加时间戳，避免重名导致文件覆盖
     *
     * @param file 文件
     * @param path 存储路径前缀
     * @return 存储对象名称，含相对路径
     */
    String upload(File file, String path);

    // todo [matianhao] 完整路径 objectKey 交由用户自定义

    /**
     * 流式上传
     *
     * @param inputStream 流对象
     *                    文件流：new FileInputStream("D:\\localpath\\examplefile.txt");
     *                    网络流：new URL("https://www.aliyun.com/").openStream();
     *                    字节数组：new ByteArrayInputStream("Hello OSS".getBytes())
     * @param objectKey   Object 完整路径
     * @return 存储对象名称，含相对路径
     */
    String upload(InputStream inputStream, String objectKey);

    /**
     * 文件上传并覆盖同名文件
     * <p>
     * 默认 Object 完整路径为 file 文件名
     * 场景：使用手册、说明文件等，需要替换文件但不修改 Object 路径的场景（路径不变、授权访问的 URL 也就不变）
     *
     * @param file 文件
     * @return 存储对象名称，含相对路径
     */
    String uploadAndOverwrite(File file);

    /**
     * 文件上传并覆盖同名文件，指定路径
     * <p>
     * 如 path 为 dir，file 文件名为 temp.txt，则默认 Object 完整路径为 dir/temp.txt
     * 场景：使用手册、说明文件等，需要替换文件但不修改 Object 路径的场景（路径不变、授权访问的 URL 也就不变）
     *
     * @param file 文件
     * @param path 存储路径前缀
     * @return 存储对象名称，含相对路径
     */
    String uploadAndOverwrite(File file, String path);

    // todo [matianhao] 进度条，前端需要何种方式  {@link https://help.aliyun.com/document_detail/84796.html}

    /**
     * 生成签名 URL 授权访问
     * <p>
     * 默认过期时间1小时
     *
     * @param objectKey Object 完整路径
     * @return 授权访问 URL 对象
     */
    String generatePresignedUrl(String objectKey);

    /**
     * 生成签名 URL 授权访问
     *
     * @param objectKey Object 完整路径
     * @param duration  链接有效时长
     * @param unit      时间单位
     * @return 授权访问 URL 对象
     */
    String generatePresignedUrl(String objectKey, int duration, TimeUnit unit);



    // ------------------------------------------------------------
    // ---------------------- download 下载 -----------------------
    // ------------------------------------------------------------

    /**
     * 下载到指定 File 中
     *
     * @param objectKey Object 完整路径
     * @param file      指定的本地路径，如果指定的本地文件存在会覆盖，不存在则新建。
     */
    void download(String objectKey, File file);



    // ------------------------------------------------------------
    // ------------------ object manage 文件管理 -------------------
    // ------------------------------------------------------------

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
     * 将源Bucket中的文件（Object）复制到同一地域下同一目标Bucket中
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



    // ------------------------------------------------------------
    // ----------------------- 默认方法实现 ------------------------
    // ------------------------------------------------------------

    /**
     * 获取时间戳字符串 yyyyMMddHHmmssSSS
     *
     * @return 时间戳字符串
     */
    default String getDateTimeStr() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    /**
     * 获取默认路径前缀 yyyyMMdd
     *
     * @return 日期字符串
     */
    default String getDefaultPathPrefix() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * 获取默认 objectkey 完整路径
     * <p>
     * - 如 file 文件名为 temp.txt，则默认 Object 完整路径为 "temp_yyyyMMddHHmmssSSS.txt"
     * - path 路径为 null 时，前缀默认为 yyyyMMdd 日期文件夹
     *
     * @param file 文件
     * @param path 存储路径前缀
     * @return object 完整路径
     */
    default String getDefaultObjectKey(File file, String path) {

        // 获取文件名称和扩展名
        String fileName = file.getName();
        String extra = fileName.substring(fileName.lastIndexOf("."));
        String name = fileName.substring(0, fileName.lastIndexOf("."));

        // 路径前缀
        String trimPathPrefix = getDefaultPathPrefix();
        if (Objects.nonNull(path)) {
            // 去除路径前后 / 字符
            trimPathPrefix = StringUtils.trimTrailingCharacter(
                    StringUtils.trimLeadingCharacter(path, '/'), '/');
        }

        // 时间戳字符串
        String dateTimeStr = getDateTimeStr();

        // 重新命名后的 Object 完整路径
        return trimPathPrefix + "/" + name + "_" + dateTimeStr + extra;
    }

}
