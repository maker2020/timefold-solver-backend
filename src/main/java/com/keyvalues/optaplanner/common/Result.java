package com.keyvalues.optaplanner.common;

import java.io.Serializable;

import com.keyvalues.optaplanner.constant.CommonConstant;

import lombok.Data;

@Data
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String msg="";

    private Integer code=-1;

    private T data;

    public Result() {
    }

    public Result(Integer code, String msg) {
        this.setMsg(msg);
        this.setCode(code);
    }

    public Result<T> success(String msg) {
        this.setMsg(msg);
        this.setCode(CommonConstant.SUCCESS);
        return this;
    }

    public static<T> Result<T> OK() {
        Result<T> r = new Result<>();
        r.setCode(CommonConstant.SUCCESS);
        return r;
    }

    public static<T> Result<T> OK(T data) {
        Result<T> r = new Result<>();
        r.setCode(CommonConstant.SUCCESS);
        r.setData(data);
        return r;
    }

    public static<T> Result<T> OK(String msg, T data) {
        Result<T> r = new Result<>();
        r.setCode(CommonConstant.SUCCESS);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    public static<T> Result<T> failed(String msg) {
        return failed(CommonConstant.FAILED, msg);
    }

    public static<T> Result<T> failed(int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

}
