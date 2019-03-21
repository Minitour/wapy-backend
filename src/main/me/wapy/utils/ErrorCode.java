package me.wapy.utils;

/**
 * Created by Antonio Zaitoun on 09/10/2018.
 */
public enum ErrorCode {

    OK(200,"Success"),
    UNKNOWN_ERROR(400,"Unknown Error"),
    NO_PERMISSION(401,"No Permission"),
    INVALID_CONTEXT(402,"Invalid Context"),
    DATA_SOURCE_ERROR(403,"Data Source Error"),
    RESOURCE_NOT_AVAILABLE(404,"Resource Not Available"),
    MISSING_PARAMETERS(405,"Missing Parameters");


    int code;
    String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public JSONResponse toJsonResponse(){
        return new JSONResponse(code,message);
    }
}
