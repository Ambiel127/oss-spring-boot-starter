package com.mth.oss.spring.boot.autoconfigure.core.local;

import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import com.mth.oss.spring.boot.autoconfigure.exception.IORuntimeException;
import com.mth.oss.spring.boot.autoconfigure.handler.DefaultObjectKeyHandler;
import com.mth.oss.spring.boot.autoconfigure.handler.DefaultOssHandler;
import com.mth.oss.spring.boot.autoconfigure.handler.OssHandler;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        try {
            FileUtils.copyToFile(inputStream, getObject(objectKey));
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
        return upload(file, objectKey);
    }

    @Override
    public boolean download(String objectKey, String fullFilePath) {
        File file = new File(fullFilePath);
        return download(objectKey, file);
    }

    @Override
    public boolean download(String objectKey, File file) {
        File source = getObject(objectKey);
        try {
            FileUtils.copyFile(source, file);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        return file.exists();
    }

    @Override
    public byte[] download(String objectKey) {
        File source = getObject(objectKey);

        try {
            return FileUtils.readFileToByteArray(source);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    @Override
    public void download(String objectKey, OutputStream outputStream) {
        File source = getObject(objectKey);
        try {
            FileUtils.copyFile(source, outputStream);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    @Override
    public boolean objectExist(String objectKey) {
        File file = getObject(objectKey);
        return file.exists();
    }

    @Override
    public File getObject(String objectKey) {
        String completeObjectKey = getCompleteObjectKey(objectKey);
        return new File(completeObjectKey);
    }

    @Override
    public boolean deleteObject(String objectKey) {
        File file = getObject(objectKey);
        return file.delete();
    }

    @Override
    public List<String> deleteObjects(List<String> objectKeys) {
        return objectKeys.stream()
                .filter(objectKey -> !deleteObject(objectKey))
                .collect(Collectors.toList());
    }

    @Override
    public boolean copyObject(String sourceKey, String destinationKey) {
        try {
            FileUtils.copyFile(getObject(sourceKey), getObject(destinationKey));
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        return objectExist(destinationKey);
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

}
