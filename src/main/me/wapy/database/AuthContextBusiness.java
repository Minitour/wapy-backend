package me.wapy.database;

import com.google.gson.annotations.Expose;
import me.wapy.utils.swagger.Model;
import me.wapy.utils.swagger.ModelField;

/**
 * Created by Antonio Zaitoun on 09/02/2018.
 */
@Model
public class AuthContextBusiness {

    @ModelField (description = "the business id")
    @Expose
    public final long business_id;

    @ModelField (description = "the business session token")
    @Expose
    public final String session_token_business;


    /*** */

    public AuthContextBusiness(long business_id, String session_token) {
        this.business_id = business_id;
        this.session_token_business = session_token;
    }

    @Override
    public String toString() {
        return "AuthContextBusiness{" +
                "business_id=" + business_id +
                ", session_token_business='" + session_token_business + '\'' +
                '}';
    }
}
