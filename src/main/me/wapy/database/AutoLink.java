package me.wapy.database;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Antonio Zaitoun on 26/02/2018.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface AutoLink {

    /**
     * @return the name of the field in the database.
     */
    String NAME() default "AUTO";

    /**
     * @return true if this field is considered part of the primary key.
     */
    boolean PK() default false;

    /**
     * @return true if this value is auto assigned by the database upon insertion.
     */
    boolean AUTO() default false;

    /**
     * @return true if this field is a foreign key.
     */
    boolean FK() default false;

    /**
     * A way to map DB Object's primary keys into the table's foreign keys.
     *
     * @return
     */
    String[] MAP() default "";


}
