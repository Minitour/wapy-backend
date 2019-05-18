package me.wapy;

import com.google.gson.JsonObject;
import me.wapy.controllers.BoxController;
import me.wapy.controllers.DashboardController;
import me.wapy.controllers.ProductController;
import me.wapy.database.Database;
import me.wapy.utils.Config;
import me.wapy.utils.JSONResponse;
import me.wapy.utils.JSONTransformer;
import me.wapy.utils.RESTRoute;
import spark.Request;
import spark.Response;

import static spark.Spark.*;

/**
 * Created by Antonio Zaitoun on 21/03/2019.
 */
public class Main {

    public static void main(String[] args) {

        // check if any envs are missing.
        checkEnv();

        // init database
        Database.init();

        port(Config.main.get("port").getAsInt());

        // setup redirect
        get("/", new RESTRoute() {
            @Override
            public Object handle(Request request, Response response, JsonObject body) {
                response.redirect("https://wapy.me");
                return "";
            }

            @Override
            public boolean isProtected() {
                return false;
            }
        });

        // engine status
        get("/status","application/json", new RESTRoute() {
            @Override
            public Object handle(Request request, Response response, JsonObject body) {
                return JSONResponse.SUCCESS();
            }

            @Override
            public boolean isProtected() {
                return false;
            }
        },new JSONTransformer());



        //TODO: add controllers
        make("/dashboard", new DashboardController());
        make("/product/:id", new ProductController());
        make("/box/:id", new BoxController());


    }

    public static void make(String route, RESTRoute controller) {
        post(route, "application/json", controller, new JSONTransformer());
    }

    private static void checkEnv(){
        String[] variables = {
                "WAPY_JDBC_URL",
                "WAPY_JDBC_USERNAME",
                "WAPY_JDBC_PASSWORD",
                "WAPY_JWT_SECRET"
        };

        for (String variableName : variables) {
            if (System.getenv(variableName) == null)
                throw new RuntimeException("Missing Environment Variable "+ variableName);
        }
    }
}
