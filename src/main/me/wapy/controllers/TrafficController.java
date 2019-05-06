package me.wapy.controllers;

import com.google.gson.JsonObject;
import me.wapy.database.AuthContext;
import me.wapy.database.data_access.DashboardAccess;
import me.wapy.utils.JSONResponse;
import me.wapy.utils.RESTRoute;
import net.sf.json.JSON;
import spark.Request;
import spark.Response;

import java.sql.Time;
import java.sql.Timestamp;

public class TrafficController implements RESTRoute {
    @Override
    public Object handle(Request request, Response response, JsonObject body) throws Exception {

        AuthContext context = extractFromBody(body);

        Timestamp fromTime = Timestamp.valueOf(body.get("fromTime").getAsString());
        Timestamp toTime = Timestamp.valueOf(body.get("toTime").getAsString());

        try(DashboardAccess access = new DashboardAccess()) {
            Long counter = access.getTraffic(context, fromTime, toTime);

            return JSONResponse.SUCCESS().message(String.valueOf(counter));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return JSONResponse.FAILURE().message("No Traffic");


    }

}

