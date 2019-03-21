package me.wapy.database.exception;

/**
 * Created by Antonio Zaitoun on 23/02/2018.
 */
public abstract class DataAccessException extends RuntimeException {
    DataAccessException(){}
    DataAccessException(String message){
        super(message);
    }
}
