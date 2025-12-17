package com.ping.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


//@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultT<T> {
    private String code;
    private String msg;
    private T data;

    public static <T> ResultT<T> success(String msg) {
        return new ResultT<T>("200", msg, null);
    }
    public static <T> ResultT<T> success(T data) {
        return new ResultT<T>("200", "成功", data);
    }

    public static <T> ResultT<T> success(String msg, T data) {
        return new ResultT<T>("200", msg, data);
    }

    public static <T> ResultT<T> error(String msg) {
        return new ResultT<T>("400", msg, null);
    }
}
