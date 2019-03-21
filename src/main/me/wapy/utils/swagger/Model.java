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
public @interface Model {
    /**
     * @return The name of the model. By default will use the class name.
     */
    String name() default "*"; // auto extract

    /**
     * @return List of models to inherit from. If your mode (class) extends an existing model then you must use this field.
     */
    String[] includes() default {};

    /**
     * @return A description of the model.
     */
    String description() default "An object";
}
