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

public class BoxAccess extends Database {

    private boolean debug = false;

    public BoxAccess() {

    }
    public BoxAccess(Database database, boolean isWeak) {
        super(database, isWeak);
    }

    public BoxAccess(Database database) {
        super(database);
    }

    /**
     * Return all product in specific window
     * @param owner_uid
     * @param fromTime
     * @param toTime
     * @return
     */
    public List<Product> getAllProductsInWindow(String owner_uid, Timestamp fromTime, Timestamp toTime) throws SQLException {

        try(DashboardAccess access = new DashboardAccess(this)) {
            List<Product> products = access.getAllProductInWindow(owner_uid, fromTime, toTime);
            return products;

        }catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Return the most viewed product in the specific window
     * @param owner_uid
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public Product getMostViewedProductInWindow(String owner_uid, Timestamp fromTime, Timestamp toTime) throws SQLException {
        if (fromTime.after(toTime))
            return null;

        String query = "SELECT count(object_id) as value, object_id \n" +
                "FROM objects_table \n" +
                "WHERE timestamp BETWEEN ? and ?\n" +
                "AND owner_uid=?\n" +
                "GROUP BY object_id\n" +
                "ORDER BY value DESC\n" +
                "LIMIT 1";

        List<Map<String, Object>> res = sql.get(
                query,
                fromTime, toTime, owner_uid
        );

        if (res.isEmpty())
            return null;

        if (debug)
            System.out.println(res);

        Product product = new Product(res.get(0));

        return product;
    }

    /**
     * Return the most viewed product in the specific window
     * @param owner_uid
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public Product getLeastViewedProductInWindow(String owner_uid, Timestamp fromTime, Timestamp toTime) throws SQLException {
        if (fromTime.after(toTime))
            return null;

        String query = "SELECT count(object_id) as value, object_id \n" +
                "FROM objects_table \n" +
                "WHERE timestamp BETWEEN ? and ?\n" +
                "AND owner_uid=?\n" +
                "GROUP BY object_id\n" +
                "ORDER BY value ASC\n" +
                "LIMIT 1";

        List<Map<String, Object>> res = sql.get(
                query,
                fromTime, toTime, owner_uid
        );

        if (res.isEmpty())
            return null;

        if (debug)
            System.out.println(res);

        Product product = new Product(res.get(0));

        return product;
    }

    /**
     * Return all reactions for specific product and box in a given time interval
     * @param objectId
     * @param owner_uid
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public List<Reaction> getAllReactionsPerProductPerBox(String objectId, String owner_uid, Timestamp fromTime, Timestamp toTime) throws SQLException {
        String[] emotions = {"calm", "happy", "confused", "disgusted", "angry", "sad"};
        List<Reaction> allReactions = new ArrayList<>();

        String query = "select count(object_id) as value, " + String.join(",", emotions) + " from images_table\n" +
                "where timestamp between ? and ? AND ";

        for (String emotion : emotions) {
            query += emotion + " >= 50.0 AND ";
        }

        query += "object_id=? and owner_uid=?";

        // getting the results for the query
        List<Map<String, Object>> res = sql.get(
                query,
                fromTime, toTime, objectId, owner_uid
        );

        // checking for validation of result
        if (res.isEmpty()) {
            return new ArrayList<>();
        }

        if (debug) {
            System.out.println(res);
        }
        System.out.println(res);
        for (String emotion : emotions) {
            Float value = (Float)res.get(0).get("value");
            Reaction reaction = new Reaction(emotion, value.longValue());
            allReactions.add(reaction);
        }

        return allReactions;
    }
}
