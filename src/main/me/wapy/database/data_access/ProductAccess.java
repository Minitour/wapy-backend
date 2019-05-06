package me.wapy.database.data_access;

import me.wapy.database.Database;
import me.wapy.model.Product;
import me.wapy.model.Reaction;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductAccess extends Database {


    private boolean debug = false;

    public ProductAccess(){

    }

    public ProductAccess(Database database, boolean isWeak) {
        super(database, isWeak);
    }

    public ProductAccess(Database database) {
        super(database);
    }

    /**
     * Returns all reactions for specific product given store_id, object_id and time interval
     * @param objectId
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public List<Reaction> getAllReactionsPerProduct(String storeId, String objectId, Timestamp fromTime, Timestamp toTime) throws SQLException{
        String[] emotions = {"calm", "happy", "confused", "disgusted", "angry", "sad"};
        List<Reaction> allReactions = new ArrayList<>();

        for (String emotion : emotions) {
            String query = "select count(object_id), " + emotion + " as value from images_table\n" +
                    "where store_id=? AND timestamp between ? and ?\n" +
                    "AND <reaction> >= 50.0 AND object_id=?";

            // getting the results for the query
            List<Map<String, Object>> res = sql.get(
                    query,
                    storeId, fromTime, toTime, objectId
            );

            // checking for validation of result
            if (res.isEmpty()) {
                return null;
            }

            if (debug) {
                System.out.println(res);
            }

            // construct the reaction map object
            Reaction reaction = new Reaction(res.get(0));

            // add the reaction to the list
            allReactions.add(reaction);
        }

        return allReactions;

    }

}
