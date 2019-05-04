package me.wapy.controllers;

import com.google.gson.JsonObject;
import me.wapy.utils.JSONResponse;
import me.wapy.utils.RESTRoute;
import spark.Request;
import spark.Response;

public class DashboardController implements RESTRoute {
    @Override
    public Object handle(Request request, Response response, JsonObject body) throws Exception {
        return JSONResponse.SUCCESS().message("ok");


    }

}
