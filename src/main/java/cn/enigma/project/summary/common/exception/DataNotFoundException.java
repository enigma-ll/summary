package cn.enigma.project.summary.common.exception;

import cn.enigma.project.summary.common.error.GlobalErrorEnum;

/**
 * @author luzh
 * Create: 2019-06-17 12:03
 * Modified By:
 * Description:
 */
public class DataNotFoundException extends Exception {
    private static final long serialVersionUID = -2719568469419774112L;

    private String code = GlobalErrorEnum.DATA_BOT_FOUNT_ERROR.getCode();

    public DataNotFoundException(String message) {
        super(message);
    }

    public DataNotFoundException() {
        super(GlobalErrorEnum.DATA_BOT_FOUNT_ERROR.getMessage());
    }

    public String getCode() {
        return code;
    }
}
