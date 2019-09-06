package cn.enigma.project.summary.common.exception;

import cn.enigma.project.summary.common.Globals;
import cn.enigma.project.summary.common.error.GlobalErrorEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author luzh
 * Create: 2019-05-30 14:39
 * Modified By:
 * Description:
 */
public class RequestParamException extends GlobalException {
    private static final long serialVersionUID = 2691947415013365682L;

    private List<Throwable> causes = new ArrayList<>();

    public RequestParamException(List<? extends Throwable> causes) {
        super(!causes.isEmpty()
                        ? causes.stream().map(Globals::getOriginException).map(Throwable::getMessage)
                        .collect(Collectors.joining("；"))
                        : GlobalErrorEnum.REQUEST_PARAM_ERROR.getMessage(),
                GlobalErrorEnum.REQUEST_PARAM_ERROR.getCode()
        );
        this.causes.addAll(causes);
    }

    public RequestParamException(String message) {
        super(message, GlobalErrorEnum.REQUEST_PARAM_ERROR.getCode());
    }

    public List<Throwable> getCauses() {
        return causes;
    }
}
