package com.mth.oss.spring.boot.autoconfigure.core.local;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import com.mth.oss.spring.boot.autoconfigure.exception.IORuntimeException;
import com.mth.oss.spring.boot.autoconfigure.handler.DefaultObjectKeyHandler;
import com.mth.oss.spring.boot.autoconfigure.handler.DefaultOssHandler;
import com.mth.oss.spring.boot.autoconfigure.handler.OssHandler;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * 对象存储服务的 Local 本地实现
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.3
 */
@RequiredArgsConstructor
public class LocalOssTemplate implements LocalOssOperations, DefaultObjectKeyHandler {

    private final OssProperties ossProperties;

    private OssHandler ossHandler;


    @Override
    public String upload(File file) {
        String pathName = getDefaultObjectKey(file);
        return upload(file, pathName);
    }

    @Override
    public String upload(File file, String objectKey) {
        try (InputStream source = new FileInputStream(file)) {
            return upload(source, objectKey);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    @Override
    public String upload(InputStream inputStream, String objectKey) {
        String completeObjectKey = getCompleteObjectKey(objectKey);

        try {
            FileUtils.copyToFile(inputStream, new File(completeObjectKey));
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }

        return objectKey;
    }

    @Override
    public String replaceUpload(File file) {
        String pathName = file.getName();
        return replaceUpload(file, pathName);
    }

    @Override
    public String replaceUpload(File file, String objectKey) {
        String completeObjectKey = getCompleteObjectKey(objectKey);

        try {
            Files.deleteIfExists(Paths.get(completeObjectKey));
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }

        return upload(file, objectKey);
    }

    @Override
    public boolean download(String objectKey, String fullFilePath) {
        File file = new File(fullFilePath);
        return download(objectKey, file);
    }

    @Override
    public boolean download(String objectKey, File file) {
        File source = getObjectFile(objectKey);
        try {
            FileUtils.copyFile(source, file);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        return file.exists();
    }


    /**
     * 获取完整路径
     *
     * @param objectKey 路径名，包含文件名
     * @return 存储对象完整路径
     */
    public String getCompleteObjectKey(String objectKey) {
        String key = ossProperties.getLocalBasePath() + File.separatorChar + objectKey;
        key = key.replaceAll("\\\\", "/").replaceAll("//", "/");
        return key;
    }

    /**
     * 设置自定义扩展点实现
     *
     * @param ossHandler 扩展点实现类
     */
    public void setOssHandler(OssHandler ossHandler) {
        this.ossHandler = Objects.nonNull(ossHandler) ? ossHandler : new DefaultOssHandler();
    }

    /**
     * 获取 object key 对应文件
     *
     * @param objectKey 路径名，包含文件名
     * @return 文件对象
     */
    private File getObjectFile(String objectKey) {
        String completeObjectKey = getCompleteObjectKey(objectKey);
        return new File(completeObjectKey);
    }

}
