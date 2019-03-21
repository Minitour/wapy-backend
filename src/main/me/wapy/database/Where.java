package me.wapy.database;

/**
 * Created by Antonio Zaitoun on 23/02/2018.
 */
public class Where {
    public final String syntax;
    public final Object[] values;

    public Where(String syntax, Object...values) {
        this.syntax = syntax;
        this.values = values;
    }
}
