package me.wapy.database.exception;

/**
 * Created by Antonio Zaitoun on 23/02/2018.
 */
public class UnimplementedException extends DataAccessException {
    public UnimplementedException() {
        super("Unimplemented");
    }

    UnimplementedException(String message) {
        super(message);
    }
}
