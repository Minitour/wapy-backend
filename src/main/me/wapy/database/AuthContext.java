package me.wapy.database;

import com.google.gson.annotations.Expose;
import me.wapy.database.permission.Permissions;
import me.wapy.utils.swagger.Model;
import me.wapy.utils.swagger.ModelField;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Antonio Zaitoun on 09/02/2018.
 */
@Model
public class AuthContext {

    @ModelField (description = "the user id")
    @Expose
    public final long user_id;

    @ModelField (description = "the session token")
    @Expose
    public final String session_token;

    @Expose
    private int user_id_rank = -1;

    @Expose
    private int user_type = -1;

    @ModelField(container = "List",type = "String")
    @Expose
    private Set<String> permissions;

    private String user_firebase_token;

    public AuthContext(long user_id, String session_token) {
        this.user_id = user_id;
        this.session_token = session_token;
    }

    public void setRank(int rank) {
        this.user_id_rank = rank;
    }

    public int getRank() { return user_id_rank; }

    public int getUserType(){
        return user_type;
    }

    public void setType_user(int user_type) {
        this.user_type = user_type;
    }

    public String getFirebaseToken() {
        return user_firebase_token;
    }

    void validate(int rank, int type_user, String user_firebase_token){
        this.user_id_rank = rank;
        this.user_type = type_user;
        this.user_firebase_token = user_firebase_token;
        initPermissions();
    }

    public void initPermissions(){
        if(this.permissions != null)
            return;

        Permissions.PermissionGroup g = Permissions.getPermissionGroups().get(user_type);
        Permissions.Rank r = Permissions.getPermissionRanks().get(user_id_rank);
        if(g != null || r != null){
            this.permissions = new HashSet<>();
        }

        if (g != null){
            this.permissions.addAll(g.getPermissions());
        }

        if (r != null) {
            this.permissions.addAll(r.getPermissions());
        }
    }

    public boolean isValid(){
        return user_id_rank > -1 && user_type > -1;
    }


    @Override
    public String toString() {
        return "AuthContext{" +
                "user_id=" + user_id +
                ", session_token='" + session_token + '\'' +
                ", user_id_rank=" + user_id_rank +
                ", user_type=" + user_type +
                ", permissions=" + permissions +
                '}';
    }
}
