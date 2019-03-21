package me.wapy.utils.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created By Tony on 22/09/2018
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface HIRoute {
    String name();
    String[] tags() default {};
    String[] method();
    String summary() default "";
    String description() default "";
    HIParameter[] parameters() default {};
    HIResponse[] responses() ;
}
