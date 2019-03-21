package me.wapy.utils.swagger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created By Tony on 22/09/2018
 */

@Retention(RetentionPolicy.SOURCE)
public @interface HIResponse {
    int status();
    String message();
    HIParameter[] parameters();
}
