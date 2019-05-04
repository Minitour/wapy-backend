package me.wapy.model;

import me.wapy.database.AutoLink;
import me.wapy.database.DBObject;

import java.sql.Timestamp;
import java.util.Map;

public class Product extends DBObject{

    @AutoLink
    String object_id;

    @AutoLink
    String store_id;

    @AutoLink
    String camera_id;

    @AutoLink
    Timestamp timestamp;

    public Product() {}

    public Product(Map<String, Object> map) {
        super(map);
    }

    public String getObject_id() {
        return object_id;
    }

    public String getStore_id() {
        return store_id;
    }

    public String getCamera_id() {
        return camera_id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
