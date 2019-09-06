package cn.enigma.project.summary.common.controller.response;

import cn.enigma.project.summary.common.error.ErrorFunction;
import cn.enigma.project.summary.common.error.GlobalErrorEnum;
import lombok.Data;

/**
 * @author luzh
 * Create: 2019-05-28 14:38
 * Modified By:
 * Description:
 */
@Data
public class BaseVO<T> {
    public static final String SUCCESS_CODE = "0";
    public static final String SUCCESS_MSG = "success";
    private String code = SUCCESS_CODE;
    private String msg = SUCCESS_MSG;
    private T data;

    public BaseVO() {
    }

    public BaseVO(GlobalErrorEnum errorCodeEnum) {
        this.code = errorCodeEnum.getCode();
        this.msg = errorCodeEnum.getMessage();
    }

    public BaseVO(GlobalErrorEnum errorCodeEnum, String message, ErrorFunction<String, GlobalErrorEnum, String> function) {
        this.code = errorCodeEnum.getCode();
        this.msg = function.apply(message, errorCodeEnum);
    }

    public BaseVO(GlobalErrorEnum errorCodeEnum, String message) {
        this.code = errorCodeEnum.getCode();
        this.msg = message;
    }

    public BaseVO(T data) {
        super();
        this.data = data;
    }
    public BaseVO(String code, String msg) {
        super();
        this.code = code;
        this.msg = msg;
    }
    public BaseVO(String code, String msg, T data) {
        super();
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}
