package cn.enigma.project.summary.common.exception;


import cn.enigma.project.summary.common.error.ErrorFunction;
import cn.enigma.project.summary.common.error.GlobalErrorEnum;
import cn.enigma.project.summary.common.error.SystemError;

import java.util.function.Function;

/**
 * @author luzh
 * Create: 2019-05-30 12:08
 * Modified By:
 * Description: 全局通用的异常类，包含code和message用来生成BaseVO
 */
public class GlobalException extends Exception {
    private static final long serialVersionUID = 1490039336296631839L;

    private String code;

    /**
     * message可以自定义，code为默认的错误枚举对象的code
     *
     * @param message 异常信息
     */
    public GlobalException(String message) {
        super(message);
        this.code = GlobalErrorEnum.DEFAULT_ERROR.getCode();
    }

    /**
     * 封装异常（异常message打印出来是com.xx.xx.xxException: xxx原因导致的错误），不是很友好
     *
     * @param cause 要封装的异常
     */
    @Deprecated
    public GlobalException(Throwable cause) {
        super(cause);
        this.code = GlobalErrorEnum.DEFAULT_ERROR.getCode();
    }

    /**
     * 封装异常，可以使用function自定义错误message
     *
     * @param cause         要分装的异常
     * @param messageMapper 异常信息function
     */
    public GlobalException(Throwable cause, Function<Throwable, String> messageMapper) {
        super(messageMapper.apply(cause), cause);
        this.code = GlobalErrorEnum.DEFAULT_ERROR.getCode();
    }

    /**
     * 封装异常，并定义异常信息
     *
     * @param message 异常信息
     * @param cause   要封装的异常
     */
    public GlobalException(String message, Throwable cause) {
        super(message, cause);
        this.code = GlobalErrorEnum.DEFAULT_ERROR.getCode();
    }

    /**
     * 封装异常，并定义异常信息，使用function自定义错误message
     *
     * @param message       自定义的异常信息
     * @param cause         要封装的异常
     * @param messageMapper 异常信息function
     */
    public GlobalException(String message, Throwable cause, ErrorFunction<Throwable, String, String> messageMapper) {
        super(messageMapper.apply(cause, message), cause);
        this.code = GlobalErrorEnum.DEFAULT_ERROR.getCode();
    }

    /**
     * 简单构造函数
     *
     * @param message 错误消息
     * @param code    错误code
     */
    public GlobalException(String message, String code) {
        super(message);
        this.code = code;
    }

    /**
     * 在简单构造方法的基础上封装异常信息
     *
     * @param message 自定义异常信息
     * @param code    code
     * @param cause   封装的异常
     */
    public GlobalException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 封装异常，提供code和message，并可以使用function自定义异常
     *
     * @param message       message
     * @param code          code
     * @param cause         封装的异常
     * @param messageMapper 异常消息function
     */
    public GlobalException(String message, String code, Throwable cause, ErrorFunction<Throwable, String, String> messageMapper) {
        super(messageMapper.apply(cause, message), cause);
        this.code = code;
    }

    /**
     * 使用错误枚举构造异常，异常信息取枚举message，code使用枚举code
     *
     * @param systemError 错误枚举
     */
    public GlobalException(SystemError systemError) {
        super(systemError.errorMessage());
        this.code = systemError.errorCode();
    }

    /**
     * 使用错误枚举构造异常，code使用枚举的code，并且可以传入message后使用function自定义异常信息
     *
     * @param systemError   错误信息
     * @param message       自定义错误
     * @param messageMapper 异常信息function
     */
    public GlobalException(SystemError systemError, String message, ErrorFunction<SystemError, String, String> messageMapper) {
        super(messageMapper.apply(systemError, message));
        this.code = systemError.errorCode();
    }

    /**
     * 使用错误枚举构造异常，并封装异常信息，code和message均由SystemError获得
     *
     * @param systemError 错误信息
     * @param cause       封装的异常
     */
    public GlobalException(SystemError systemError, Throwable cause) {
        super(systemError.errorMessage(), cause);
        this.code = systemError.errorCode();
    }

    /**
     * 使用枚举构造异常，封装异常信息，支持使用function通过错误信息和异常自定义错误message
     *
     * @param errorCodeEnum 错误枚举值
     * @param cause         封装的异常
     * @param messageMapper 异常信息function表达式
     */
    public GlobalException(SystemError errorCodeEnum, Throwable cause, ErrorFunction<SystemError, Throwable, String> messageMapper) {
        super(messageMapper.apply(errorCodeEnum, cause), cause);
        this.code = errorCodeEnum.errorCode();
    }

    public String getCode() {
        return code;
    }
}
