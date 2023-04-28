package com.mth.oss.spring.boot.autoconfigure.exception;

/**
 * IO 运行时异常，用于对 {@link java.io.IOException} 的封装
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.3
 */
public class IORuntimeException extends RuntimeException{

    private static final long serialVersionUID = 8247610319171014183L;

    public IORuntimeException() {
        super();
    }

    public IORuntimeException(Throwable e) {
        super(e);
    }

    public IORuntimeException(String message) {
        super(message);
    }

    public IORuntimeException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
