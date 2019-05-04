package me.wapy;

import me.wapy.database.Database;
import me.wapy.database.data_access.DashboardAccess;

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

        try(DashboardAccess db = new DashboardAccess()) {
            System.out.println(db.getTraffic("1"));
        }catch (Exception e) {
            e.printStackTrace();
        }
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
