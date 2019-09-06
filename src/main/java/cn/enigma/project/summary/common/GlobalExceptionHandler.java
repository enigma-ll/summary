package cn.enigma.project.summary.common;

import cn.enigma.project.summary.common.controller.response.BaseVO;
import cn.enigma.project.summary.common.error.GlobalErrorEnum;
import cn.enigma.project.summary.common.exception.DataNotFoundException;
import cn.enigma.project.summary.common.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author luzh
 * Create: 2018/11/14 5:18 PM
 * Modified By:
 * Description:
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static String getRequestInfo(HttpServletRequest request) {
        return request.getMethod() + " request: " + request.getRequestURL().toString();
    }

    /**
     * 全局通用异常处理
     *
     * @param request request
     * @param e       e
     * @return vo
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public BaseVO<?> globalHandler(HttpServletRequest request, Exception e) {
        Throwable throwable = Globals.getOriginException(e);
        BaseVO<?> result = new BaseVO<>();
        result.setCode(GlobalErrorEnum.DEFAULT_ERROR.getCode());
        result.setMsg(throwable.getMessage());
        log.error("{} error: {}.", getRequestInfo(request), result.getMsg(), e);
        return result;
    }

    /**
     * 参数@validated注解验证、框架解析失败错误处理
     *
     * @param request request
     * @param e       e
     * @return vo
     */
    @ExceptionHandler(value = {BindException.class, MethodArgumentNotValidException.class})
    @ResponseBody
    public BaseVO<?> bindExceptionHandler(HttpServletRequest request, Exception e) {
        BeanPropertyBindingResult beanPropertyBindingResult;
        if (e instanceof BindException) {
            beanPropertyBindingResult = (BeanPropertyBindingResult) ((BindException) e).getBindingResult();
        } else {
            beanPropertyBindingResult = (BeanPropertyBindingResult) ((MethodArgumentNotValidException) e).getBindingResult();
        }
        BaseVO<?> result = new BaseVO<>(GlobalErrorEnum.REQUEST_PARAM_ERROR, getErrorMessage(beanPropertyBindingResult));
        log.error("{} error: {}.", getRequestInfo(request), result.getMsg(), e);
        return result;
    }

    private String getErrorMessage(BeanPropertyBindingResult beanPropertyBindingResult) {
        List<ObjectError> errorList = beanPropertyBindingResult.getAllErrors();
        return errorList.stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(";"));
    }

    /**
     * http请求丢失必要错误处理
     *
     * @param request request
     * @param e       e
     * @return vo
     */
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    @ResponseBody
    public BaseVO<?> handleHttpMessageNotReadableException(HttpServletRequest request, HttpMessageNotReadableException e) {
        BaseVO<?> result = new BaseVO<>(GlobalErrorEnum.REQUEST_PARAM_ERROR, "参数格式错误！");
        log.error("{} error: {}.", getRequestInfo(request), result.getMsg(), e);
        return result;
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    @ResponseBody
    public BaseVO<?> handleMissingServletRequestParameterException(HttpServletRequest request, MissingServletRequestParameterException e) {
        BaseVO<?> result = new BaseVO<>(GlobalErrorEnum.REQUEST_PARAM_ERROR, "缺少必要参数!");
        log.error("{} error: {}.", getRequestInfo(request), result.getMsg(), e);
        return result;
    }

    /**
     * 全局通用异常处理
     *
     * @param request request
     * @param e       e
     * @return vo
     */
    @ExceptionHandler(value = GlobalException.class)
    @ResponseBody
    public BaseVO<?> handleGlobalException(HttpServletRequest request, GlobalException e) {
        BaseVO<?> result = new BaseVO<>(e.getCode(), e.getMessage());
        log.error("{} error: {}.", getRequestInfo(request), result.getMsg(), e);
        return result;
    }

    @ExceptionHandler(value = DataNotFoundException.class)
    @ResponseBody
    public BaseVO<?> handleDataNotFoundException(HttpServletRequest request, DataNotFoundException e) {
        BaseVO<?> result = new BaseVO<>(e.getCode(), e.getMessage());
        log.error("{} error: {}.", getRequestInfo(request), result.getMsg(), e);
        return result;
    }
}
