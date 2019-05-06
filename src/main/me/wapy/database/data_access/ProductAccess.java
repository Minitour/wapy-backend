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
    public List<Reaction> getAllReactionsPerProduct(String objectId, Timestamp fromTime, Timestamp toTime) throws SQLException{
        String[] emotions = {"calm", "happy", "confused", "disgusted", "angry", "sad"};
        List<Reaction> allReactions = new ArrayList<>();

        for (String emotion : emotions) {
            String query = "select count(object_id) as value, " + emotion + " as reaction from images_table\n" +
                    "where timestamp between ? and ?\n" +
                    "AND " + emotion + " >= 50.0 AND object_id=?";

            // getting the results for the query
            List<Map<String, Object>> res = sql.get(
                    query,
                    fromTime, toTime, objectId
            );

            // checking for validation of result
            if (res.isEmpty()) {
                continue;
            }

            if (debug) {
                System.out.println(res);
            }

            Float value = (Float)res.get(0).get("reaction");
            // construct the reaction map object
            Reaction reaction = new Reaction(emotion, value.longValue());

            // add the reaction to the list
            allReactions.add(reaction);
        }

        return allReactions;

    }

    /**
     * Returns the total views for specific product in the window in given time interval
     * @param objectId
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public Long getTotalViewsPerProduct(String objectId, Timestamp fromTime, Timestamp toTime) throws SQLException {

        String query = "SELECT count(*) as value FROM objects_table \n" +
                "WHERE\n" +
                "timestamp BETWEEN ? and ? AND object_id =?";

        // getting the results for the query
        List<Map<String, Object>> res = sql.get(
                query,
                fromTime, toTime, objectId
        );

        // checking for validation of result
        if (res.isEmpty()) {
            return null;
        }

        if (debug) {
            System.out.println(res);
        }

        return (Long) res.get(0).get("value");

    }

    /**
     * Returns the amount of people who liked the specific product
     * @param objectId
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public Long getTotalLikesPerProduct(String objectId, Timestamp fromTime, Timestamp toTime) throws SQLException {
        List<String> emotions = new ArrayList<>();
        emotions.add("calm");
        emotions.add("happy");

        Long counter = 0L;

        try(DashboardAccess access = new DashboardAccess(this)) {

            // checking if the person is smiling
            Long c = access.getSmilesForProduct(fromTime, toTime, objectId);
            if (c > 0)
                counter += c;

            // getting all reactions for object
            List<Reaction> reactions = access.getReactionsPerProduct(objectId, fromTime, toTime);

            // checking if one of the reactions are in the friendly zone
            for (Reaction reaction : reactions) {
                if (emotions.contains(reaction.getReaction())) {
                    counter += 1L;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return counter;
    }


    public List<Product> getAllProductInWindow(String cameraId, Timestamp fromTime, Timestamp toTime) throws SQLException {
        try(DashboardAccess access = new DashboardAccess(this)) {
            return access.getAllProductInWindow(cameraId, fromTime, toTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
