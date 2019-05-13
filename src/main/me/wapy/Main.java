package me.wapy;

import me.wapy.controllers.BoxController;
import me.wapy.controllers.DashboardController;
import me.wapy.controllers.ProductController;
import me.wapy.database.Database;
import me.wapy.utils.JSONTransformer;
import me.wapy.utils.RESTRoute;


import static spark.Spark.port;
import static spark.Spark.post;

/**
 * Created by Antonio Zaitoun on 21/03/2019.
 */
public class Main {

    public static void main(String[] args) {

        // check if any envs are missing.
        checkEnv();

        // init database
        Database.init();

        //TODO: add controllers
        port(8080);
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
                "WAPY_JDBC_PASSWORD"
        };

        for (String variableName : variables) {
            if (System.getenv(variableName) == null)
                throw new RuntimeException("Missing Environment Variable "+ variableName);
        }
    }
}
