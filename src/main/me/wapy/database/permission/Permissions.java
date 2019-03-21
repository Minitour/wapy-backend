package me.wapy.database.permission;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import me.wapy.database.AuthContext;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created By Tony on 27/02/2018
 */
public class Permissions {
    private static Map<Integer,PermissionGroup> permissionGroups;
    private static Map<Integer,Rank> permissionRanks;

    public static Map<Integer, PermissionGroup> getPermissionGroups() {
        return Collections.unmodifiableMap(permissionGroups);
    }

    public static Map<Integer, Rank> getPermissionRanks() {
        return Collections.unmodifiableMap(permissionRanks);
    }

    /**
     * Call this method only once to init the permissions.
     * @throws IOException an exception may be thrown if the file isn't found.
     */
    public static void init() throws IOException {
        if(permissionGroups != null && permissionRanks != null)
            return;

        permissionGroups = new HashMap<>();
        permissionRanks = new HashMap<>();

        Gson gson = new Gson();
        String contents = new String(Files.readAllBytes(Paths.get("roles.json")));
        JsonObject roles =  gson.fromJson(contents, JsonObject.class);

        //init groups
        JsonArray groups = roles.get("groups").getAsJsonArray();
        for (JsonElement group : groups) {
            JsonObject object = group.getAsJsonObject();
            PermissionGroup g = gson.fromJson(object,PermissionGroup.class);

            if (object.has("extends")){
                for(JsonElement e : object.get("extends").getAsJsonArray()){
                    int ext = e.getAsInt();
                    if(permissionGroups.containsKey(ext)){
                        g.permissions.addAll(permissionGroups.get(ext).permissions);
                    }
                }
            }
            permissionGroups.put(g.groupId,g);
        }

        //init groups
        JsonArray ranks = roles.get("ranks").getAsJsonArray();
        for (JsonElement rank : ranks) {
            Rank r = gson.fromJson(rank,Rank.class);

            JsonObject object = rank.getAsJsonObject();

            if (object.has("extends")){
                for(JsonElement e : object.get("extends").getAsJsonArray()){
                    int ext = e.getAsInt();
                    if(permissionRanks.containsKey(ext)){
                        r.permissions.addAll(permissionRanks.get(ext).permissions);
                    }
                }
            }

            permissionRanks.put(r.rankId,r);
        }

        contents = new String(Files.readAllBytes(Paths.get("permissions.json")));
        JsonObject pers =  gson.fromJson(contents, JsonObject.class);
        JsonObject permissions = pers.get("permissions").getAsJsonObject();
        buildPermissions(permissions);
//        System.out.println("Permissions Tree Built");
    }

    /**
     * Checks if a validated context has access to a certain permission.
     *
     * @param permission The permission to check.
     * @param context The validated context.
     * @return true if the context has permission.
     */
    public static boolean hasPermissionFor(String permission, AuthContext context){
        if(!context.isValid())
            return false;

        return permissionGroups.get(context.getUserType()).permissions.contains(permission) ||
                permissionRanks.get(context.getRank()).permissions.contains(permission);
    }

    /**
     * Returns a set of permissions from a given format.
     *
     * for example @code{"io.nbrs.posts.*"} would return a set of permissions that is nested inside posts.
     *
     * @param path The permission code.
     * @param permissions The json object that contains the permission tree.
     * @return a set of permissions extracted from the path.
     */
    private static Set<String> permissionFromPath(String path,JsonObject permissions){
        Set<String> pl = new HashSet<>();
        String[] splitted = path.split("\\.");

        JsonObject currentObject = permissions;
        StringBuilder rebuilder = new StringBuilder();
        for (int i = 0; i < splitted.length - 1; i++) {

            rebuilder.append(splitted[i]).append(".");
            JsonElement e = currentObject.get(splitted[i]);

            if(splitted[i + 1].equals("*")) {

                if (e.isJsonObject()) {
                    JsonObject object = e.getAsJsonObject();
                    for(String key : object.keySet()){
                        pl.addAll(permissionFromPath(rebuilder.toString() + key + ".*",permissions));
                    }
                }

                if (e.isJsonArray()) {
                    JsonArray array = e.getAsJsonArray();
                    for (JsonElement permission : array) {
                        String str = permission.getAsString();
                        pl.add(rebuilder + str);
                    }
                }
            }else{
                if(e.isJsonObject()){
                    currentObject = e.getAsJsonObject();
                }else{
                    String permission = rebuilder.toString() + splitted[i+1];

                    Type listType = new TypeToken<Set<String>>() {}.getType();
                    Set<String> pers = new Gson().fromJson(e,listType);
                    if(pers.contains(splitted[i+1]))
                        pl.add(permission);
                }
            }

        }

        return pl;
    }

    /**
     * This method builds the permission tree and parses it.
     *
     * @param permissions the object containing the permissions tree.
     */
    private static void buildPermissions(JsonObject permissions){
        for (PermissionGroup group : permissionGroups.values()) {
            Set<String> pers = new HashSet<>(group.permissions);
            group.permissions.clear();
            for (String per : pers) {
                group.permissions.addAll(permissionFromPath(per,permissions));
            }
        }

        for (Rank rank: permissionRanks.values()) {
            Set<String> pers = new HashSet<>(rank.permissions);
            rank.permissions.clear();
            for (String per : pers) {
                rank.permissions.addAll(permissionFromPath(per,permissions));
            }
        }
    }

    public static class PermissionGroup{

        @Expose
        int groupId;

        @Expose
        String groupName;

        @Expose
        Set<String> permissions;

        public Set<String> getPermissions() {
            return Collections.unmodifiableSet(permissions);
        }

    }

    public static class Rank{

        @Expose
        int rankId;

        @Expose
        String rankName;

        @Expose
        Set<String> permissions;

        public Set<String> getPermissions() {
            return Collections.unmodifiableSet(permissions);
        }
    }
}
