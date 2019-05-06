package me.wapy.model;

import me.wapy.database.AutoLink;
import me.wapy.database.DBObject;

import java.util.Map;

public class Reaction extends DBObject {

    @AutoLink
    private String reaction;

    @AutoLink
    private Long value;


    public Reaction(String reaction, Long value) {
        this.reaction = reaction;
        this.value = value;
    }

    public Reaction(Map<String, Object> map) {
        super(map);
    }

    public String getReaction() {
        return reaction;
    }

    public Long getValue() {
        return value;
    }
}
