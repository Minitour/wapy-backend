package me.wapy.utils.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created By Tony on 22/09/2018
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface ModelField {
    String name() default "*"; // auto extract
    String type() default "*";
    String container() default "null";
    String description() default "An field.";
}
