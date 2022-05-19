
package pers.zcc.vertxprc.vo;

import java.io.Serializable;

/**
 * 接口响应类，不要调用set方法 成功返回 000 success，失败code取值 001-999
 * 
 * @author zhangchangchun
 * @since 2021年4月1日
 * @param <T> 返回数据类型
 */
public class Response<T> implements Serializable {

    private static final long serialVersionUID = 7744379893031140883L;

    private static final String SUCCESS_CODE = "000";

    private static final String SUCCESS_MSG = "success";

    /**
     * 状态码
     */
    private String code;

    /**
     * 状态说明
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 调用成功无数据
     * 
     * @return
     */
    public Response<T> success() {
        this.code = SUCCESS_CODE;
        this.message = SUCCESS_MSG;
        return this;
    }

    /**
     * 调用成功有数据
     * 
     * @param data 返回的数据
     * @return
     */
    public Response<T> success(T data) {
        this.code = SUCCESS_CODE;
        this.message = SUCCESS_MSG;
        this.data = data;
        return this;
    }

    /**
     * 调用失败无数据返回
     * 
     * @param code 状态码
     * @param message 状态说明
     * @return
     */
    public Response<T> fail(String code, String message) {
        this.code = code;
        this.message = message;
        this.data = null;
        return this;
    }

    /**
     * 判断接口调用是否成功
     *
     * @return true, if is success
     */
    public boolean exeSuccess() {
        return SUCCESS_CODE.equals(code);
    }

    /**
     * 调用失败有数据返回
     * 
     * @param code  状态码
     * @param message 状态说明
     * @param data 返回数据
     * @return
     */
    public Response<T> fail(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        return this;
    }

    public Response() {
        this.code = SUCCESS_CODE;
        this.message = SUCCESS_MSG;
    }

    public Response(String code, String message, T data) {
        super();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
