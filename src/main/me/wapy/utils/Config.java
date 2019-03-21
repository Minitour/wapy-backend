package me.wapy.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Antonio Zaitoun on 09/02/2018.
 */
public class Config {
    private JsonObject data;
    public static final Config main = new Config();

    private Config() {
        try {
            String contents = new String(Files.readAllBytes(Paths.get("config.json")));
            data =  new Gson().fromJson(contents, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonElement get(String key){
        return data.get(key);
    }
}
