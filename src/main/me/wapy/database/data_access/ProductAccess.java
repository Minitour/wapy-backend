package me.wapy.database.data_access;

import me.wapy.database.AuthContext;
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
     * Returns the total views for specific product in the window in given time interval
     * @param objectId
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public Long getTotalViewsPerProduct(String cameraId, String objectId, Timestamp fromTime, Timestamp toTime) throws SQLException {

        String query = "SELECT count(*) as value FROM objects_table \n" +
                "WHERE\n" +
                "timestamp BETWEEN ? and ? AND object_id =? AND camera_id=?";

        // getting the results for the query
        List<Map<String, Object>> res = sql.get(
                query,
                fromTime, toTime, objectId, cameraId
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
    public Long getTotalLikesPerProduct(String cameraId, String objectId, Timestamp fromTime, Timestamp toTime) throws SQLException {
        List<String> emotions = new ArrayList<>();
        emotions.add("calm");
        emotions.add("happy");

        Long counter = 0L;

        // checking if the person is smiling
        Long c = getSmilesForProduct(fromTime, toTime, objectId, cameraId);
        if (c > 0)
            counter += c;

        try(DashboardAccess access = new DashboardAccess(this)) {

            // getting all reactions for object
            List<Reaction> reactions = access.getReactionsPerProduct(cameraId, objectId, fromTime, toTime);

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

    /**
     * Returns the number of smiles for specific object for given store and time interval
     * @param fromTime
     * @param toTime
     * @param object_id
     * @return
     * @throws SQLException
     */
    public Long getSmilesForProduct(Timestamp fromTime, Timestamp toTime, String object_id, String camera_id) throws SQLException {
        String query = "select count(smile) as value from images_table\n" +
                "where object_id = ? and timestamp between ? and ?\n" +
                "AND smile=1 AND camera_id=?";

        List<Map<String, Object>> res = sql.get(
                query,
                object_id, fromTime, toTime, camera_id
        );

        if (res.isEmpty())
            return 0L;

        if (debug)
            System.out.println(res);

        return (Long) res.get(0).get("value");
    }

}
