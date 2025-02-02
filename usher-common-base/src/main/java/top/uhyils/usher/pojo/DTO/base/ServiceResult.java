package top.uhyils.usher.pojo.DTO.base;

import java.io.Serializable;
import top.uhyils.usher.enums.ServiceCode;
import top.uhyils.usher.exception.ServiceResultException;
import top.uhyils.usher.pojo.DTO.HotSpotDTO;

/**
 * 微服务返回类
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2020年04月23日 14时17分
 */
public class ServiceResult<T> implements Serializable {


    private static final long serialVersionUID = -4154566267377974875L;

    /**
     * 返回的类型
     */
    private T data;

    /**
     * 服务状态码.
     */
    private Integer serviceCode;

    /**
     * 服务提示给用户的信息.
     */
    private String serviceMessage;


    public ServiceResult(T data, Integer serviceCode, String serviceMessage) {
        this.data = data;
        this.serviceCode = serviceCode;
        this.serviceMessage = serviceMessage;
    }


    public ServiceResult() {
    }


    /**
     * 构建一个逻辑成功的返回
     *
     * @param t   请求返回值
     * @param <T> 请求返回值
     *
     * @return 一个code是200 代表成功的返回
     */
    public static <T> ServiceResult<T> buildSuccessResult(T t) {
        return new ServiceResult<>(t, ServiceCode.SUCCESS.getText(), null);
    }

    /**
     * 构建一个逻辑失败的返回
     *
     * @param businessMessage 给前台的返回
     * @param t               失败的返回体,通常是null
     * @param <T>             失败的返回体,通常是null
     *
     * @return 一个code是400的 代表逻辑错误的返回(程序并没有错)
     */
    public static <T> ServiceResult<T> buildFailedResult(String businessMessage, T t) {
        return new ServiceResult<>(t, ServiceCode.REQUEST_PARAM_ERROR.getText(), businessMessage);
    }

    /**
     * 创建一个断言异常的返回
     *
     * @param msg
     *
     * @return
     */
    public static ServiceResult<?> buildAssertFailedResult(String msg) {
        return new ServiceResult<>(null, ServiceCode.ASSERT_EXCEPTION.getText(), msg);

    }

    /**
     * 构建一个逻辑失败的返回
     *
     * @param businessMessage 给前台的返回
     *
     * @return 一个code是400的 代表逻辑错误的返回(程序并没有错)
     */
    public static <T> ServiceResult<T> buildFailedResult(String businessMessage) {
        return buildFailedResult(businessMessage, null);
    }

    /**
     * 构建一个程序失败的返回
     *
     * @param businessMessage 给前台的返回
     * @param <T>             null
     *
     * @return 一个code是500 代表逻辑错误的返回
     */
    public static <T> ServiceResult<T> buildErrorResult(String businessMessage) {

        return new ServiceResult<>(null, ServiceCode.ERROR.getText(), businessMessage);
    }

    /**
     * 构建一个权限未通过的返回
     *
     * @param <T> null
     *
     * @return 一个code是401 代表权限未通过的返回
     */
    public static <T> ServiceResult<T> buildNoAuthResult() {
        return buildResultByServiceCode(ServiceCode.NONE_AUTH_ERROR);
    }

    /**
     * 构建一个登录过期的返回
     *
     * @param <T> null
     *
     * @return 一个code是402 代表登录问题的返回
     */
    public static <T> ServiceResult<T> buildLoginOutResult() {
        return buildResultByServiceCode(ServiceCode.LOGIN_TIME_OUT_ERROR);
    }

    /**
     * 构建一个未登录的返回
     *
     * @param <T> null
     *
     * @return 一个code是403 代表登录问题的返回
     */
    public static <T> ServiceResult<T> buildNoLoginResult() {
        return buildResultByServiceCode(ServiceCode.NO_LOGIN__ERROR);
    }

    /**
     * 根据ServiceCode构建一个返回
     *
     * @param <T> null
     *
     * @return 一个返回
     */
    public static <T> ServiceResult<T> buildResultByServiceCode(ServiceCode code) {
        return new ServiceResult<>(null, code.getText(), code.getMsg());
    }

    /**
     * 创建一个结果在redis中的返回
     *
     * @param key  redis中的key
     * @param hkey redis中hash内的key
     *
     * @return
     */
    public static ServiceResult<HotSpotDTO> buildHotSpotHaveResult(String key, String hkey) {
        HotSpotDTO build = HotSpotDTO.build(key, hkey);
        return new ServiceResult<>(build, ServiceCode.SUCCESS_REDIS.getText(), null);
    }

    /**
     * 通用构建
     *
     * @param data 返回的数据
     * @param sc   返回的类型
     * @param <T>
     *
     * @return
     */
    public static <T> ServiceResult<T> build(T data, ServiceCode sc) {
        return new ServiceResult<>(data, sc.getText(), sc.getMsg());
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Integer getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(Integer serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getServiceMessage() {
        return serviceMessage;
    }

    public void setServiceMessage(String serviceMessage) {
        this.serviceMessage = serviceMessage;
    }

    /**
     * 验证并且返回
     *
     * @return
     */
    public T validationAndGet() {
        if (ServiceCode.SUCCESS.getText().equals(this.getServiceCode())) {
            return getData();
        } else if (ServiceCode.SUCCESS_REDIS.getText().equals(this.getServiceCode())) {
            throw new ServiceResultException(this.getServiceCode(), "rpc结果通过redis存储.请自行处理或者引入: usher-common-hot-spot");
        } else {
            throw new ServiceResultException(this.getServiceCode(), this.getServiceMessage());
        }
    }
}
