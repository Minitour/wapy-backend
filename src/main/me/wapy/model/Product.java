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

    @AutoLink
    Long value;

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

    public Long getValue() {return value;}

    public void setObject_id(String object_id) {
        this.object_id = object_id;
    }

    public void setStore_id(String store_id) {
        this.store_id = store_id;
    }

    public void setCamera_id(String camera_id) {
        this.camera_id = camera_id;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    @Override
    public String toString(){
        return "object_id: "+ object_id + "\n" +
                "store_id: " + store_id + "\n" +
                "camera_id: " + camera_id + "\n";
    }
}
