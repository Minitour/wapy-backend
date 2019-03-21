package me.wapy.utils;

import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.Map;

public class JSONResponse<T> {

    /**
     * Default Success Object
     * @param <T>
     * @return
     */
    public static <T> JSONResponse<T> SUCCESS() { return new JSONResponse<>(200, "Success"); }

    /**
     * Default Failure Object
     * @param <T>
     * @return
     */
    public static <T> JSONResponse<T> FAILURE() { return new JSONResponse<>(400, "Error"); }

    public static <T> JSONResponse<T> FAILURE(String msg) { return new JSONResponse<>(400, msg); }

    @Expose
    private int code;

    @Expose
    private String message;

    @Expose
    private T data;

    transient Map<String,Object> injected = null;

    public void inject(String key,Object value){

        if (injected == null)
            injected = new HashMap<>();

        injected.put(key,value);
    }

    public boolean isInjected(){
        return injected != null;
    }

    public JSONResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public JSONResponse data(T data) {
        this.data = data;
        return this;
    }

    public JSONResponse message(String message) {
        this.message = message;
        return this;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}