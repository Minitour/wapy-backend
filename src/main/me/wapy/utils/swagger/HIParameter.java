package me.wapy.utils.swagger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created By Tony on 22/09/2018
 */
@Retention(RetentionPolicy.SOURCE)
public @interface HIParameter {
    /**
     * @return The name of the parameter.
     */
    String name();

    /**
     * @return The type object the parameter.
     */
    String type();

    /**
     * @return a container object: aka List, Map, Set.
     */
    String container() default "null";

    /**
     * @return description of this parameter.
     */
    String description() default "A parameter.";
    /**
     * @return query,header,path,formData,body
     */
    String in() default "body";

    /**
     * @return String used to override the value in case a complex parameter is needed. Accepts JSON.
     */
    String literalOverride() default "";
}
