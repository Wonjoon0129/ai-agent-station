package top.kimwonjoon.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> implements Serializable {

    private static final long serialVersionUID = 7000723935764546321L;

    private String code;
    private String info;
    private T data;

    public static <T> Response<T> ok(T data) {
        Response<T> resp = new Response<T>();
        resp.setData(data);
        resp.setCode("200");
        return resp;
    }

    public static <T> Response<T> ok(T data, String msg) {
        Response<T> resp = new Response<T>();
        resp.setData(data);
        resp.setCode("200");
        resp.setInfo(msg);
        return resp;
    }

    public static <T> Response<T> ok(String code, T data, String msg) {
        Response<T> resp = new Response<T>();
        resp.setData(data);
        resp.setCode(code);
        resp.setInfo(msg);
        return resp;
    }

    public static <T> Response<T> ok() {
        return ok((T)null);
    }

    public static <T> Response<T> fail(String code, String errorMsg) {
        Response<T> resp = new Response<T>();
        resp.setCode(code);
        resp.setInfo(errorMsg);
        return resp;
    }

}
