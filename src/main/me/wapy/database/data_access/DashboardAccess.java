package me.wapy.database.data_access;

import me.wapy.database.AuthContext;
import me.wapy.database.Database;
import me.wapy.model.Product;
import me.wapy.model.Reaction;

import javax.print.DocFlavor;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Antonio Zaitoun on 04/05/2019.
 */
public class DashboardAccess extends Database {

    private boolean debug = false;

    public DashboardAccess() {
    }

    public DashboardAccess(Database database) {
        super(database);
    }

    public DashboardAccess(Database database, boolean isWeak) {
        super(database, isWeak);
    }


    /**
     * Returns the traffic for a given store and given time interval
     * @param fromTime start time for traffic
     * @param toTime end time for traffic
     * @return long value of the traffic
     * @throws SQLException
     */
    public Long getTraffic(Timestamp fromTime, Timestamp toTime) throws SQLException {

        if (fromTime.after(toTime))
            return 0L;

        List<Map<String,Object>> rs = sql.get(
                "SELECT count(*) as value FROM objects_table WHERE timestamp BETWEEN ? and ?",
                fromTime, toTime
        );

        if (rs.isEmpty())
            return 0L;

        if (debug)
            System.out.println(rs);

        return (Long) rs.get(0).get("value");
    }


    /**
     * Returns the most viewed product in a given store and given time interval
     * @param fromTime start time for views
     * @param toTime end time for views
     *
     *               {
     *                 "object_id" : "",
     *                  "value": 0
     *               }
     * @return string value of the most viewed product
     * @throws SQLException
     */
    public Product getMostViewedProduct(Timestamp fromTime, Timestamp toTime) throws SQLException {

        if (fromTime.after(toTime))
            return null;

        String query = "SELECT count(object_id) as value, object_id \n" +
                "FROM objects_table \n" +
                "WHERE timestamp BETWEEN ? and ?\n" +
                "GROUP BY object_id\n" +
                "ORDER BY value DESC\n" +
                "LIMIT 1";

        List<Map<String, Object>> res = sql.get(
                query,
                fromTime, toTime
        );

        if (res.isEmpty())
            return null;

        if (debug)
            System.out.println(res);

        Product product = new Product(res.get(0));

        return product;
    }


    /**
     * Resturn the least viewed product given store and time interval
     * @param fromTime start time for views
     * @param toTime end time for views
     * @return string value of the most viewed product
     * @throws SQLException
     */
    public Product getLeastViewedProduct(Timestamp fromTime, Timestamp toTime) throws SQLException {

        if (fromTime.after(toTime))
            return null;

        String query = "SELECT count(object_id) as value, object_id \n" +
                "FROM objects_table \n" +
                "WHERE timestamp BETWEEN ? and ?\n" +
                "GROUP BY object_id\n" +
                "ORDER BY value ASC\n" +
                "LIMIT 1";

        List<Map<String, Object>> res = sql.get(
                query,
                fromTime, toTime
        );

        if (res.isEmpty())
            return null;

        if (debug)
            System.out.println(res);

        Product product = new Product(res.get(0));

        return product;

    }

    /**
     * Returns the most viewed product reaction given store id and time interval
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public Product getMostViewedProductReaction(Timestamp fromTime, Timestamp toTime) throws SQLException {
        if (fromTime.after(toTime))
            return null;


        String query = "SELECT count(object_id) as value, \n" +
                "object_id FROM images_table\n" +
                "where\n" +
                "timestamp BETWEEN ? and ?\n" +
                "GROUP BY object_id\n" +
                "ORDER BY value DESC\n" +
                "LIMIT 1";

        List<Map<String, Object>> res = sql.get(
                query,
                fromTime, toTime
        );

        if (res.isEmpty())
            return null;

        if (debug)
            System.out.println(res);

        Product product = new Product(res.get(0));

        return product;
    }

    /**
     * Returns the least viewed product reaction given store id and time interval
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public Product getLeastViewedProductReaction(Timestamp fromTime, Timestamp toTime) throws SQLException {
        if (fromTime.after(toTime))
            return null;


        String query = "SELECT count(object_id) as value, \n" +
                "object_id FROM images_table\n" +
                "where \n" +
                "timestamp BETWEEN ? and ?\n" +
                "GROUP BY object_id\n" +
                "ORDER BY value ASC\n" +
                "LIMIT 1";

        List<Map<String, Object>> res = sql.get(
                query,
                fromTime, toTime
        );

        if (res.isEmpty())
            return null;

        if (debug)
            System.out.println(res);

        Product product = new Product(res.get(0));

        return product;


    }

    /**
     * Return the number of people detected watching the window
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public Long getExposure(Timestamp fromTime, Timestamp toTime) throws SQLException {
        if (fromTime.after(toTime))
            return null;

        String query = "SELECT count(object_id) as views FROM objects_table\n" +
                "WHERE timestamp BETWEEN ? and ?";

        List<Map<String, Object>> res = sql.get(
                query,
                fromTime, toTime
        );

        if (res.isEmpty())
            return 0L;

        if (debug)
            System.out.println(res);

        return (Long) res.get(0).get("views");
    }

    /**
     * Returns the number of smiles for specific object for given store and time interval
     * @param fromTime
     * @param toTime
     * @param object_id
     * @return
     * @throws SQLException
     */
    public Long getSmilesForProduct(Timestamp fromTime, Timestamp toTime, String object_id) throws SQLException {
        String query = "select count(smile) as value from images_table\n" +
                "where object_id = ? and timestamp between ? and ?\n" +
                "AND smile=1";

        List<Map<String, Object>> res = sql.get(
                query,
                object_id, fromTime, toTime
        );

        if (res.isEmpty())
            return 0L;

        if (debug)
            System.out.println(res);

        return (Long) res.get(0).get("value");
    }

    /**
     * Returns a list of dictionary with all reactions for given store and time interval
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public List<Reaction> getReactionsPerProduct(String object_id, Timestamp fromTime, Timestamp toTime) throws SQLException {
        List<Reaction> reactions = new ArrayList<>();

        String[] emotions = {"calm", "happy", "confused", "disgusted", "angry", "sad"};

        for (String emotion : emotions) {
            // construct the query
            String query = "select " + emotion + " as reaction, count(object_id) as value from images_table\n" +
                    "where timestamp between ? and ?\n" +
                    "AND " + emotion +" >= 50.0 AND object_id=?";

            // get all records for the query
            List<Map<String, Object>> res = sql.get(
                    query,
                    fromTime, toTime, object_id
            );

            if (!res.isEmpty()) {

                // construct the reaction object
                Float value = (Float)res.get(0).get("reaction");
                Reaction reaction = new Reaction(emotion, value.longValue());

                // add reaction to the list
                reactions.add(reaction);

            }
        }

        return reactions;
    }

    /**
     * Returns all product monitored in window
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public List<Product> getAllProductInWindow(String cameraId, Timestamp fromTime, Timestamp toTime) throws SQLException {
        List<Product> products = new ArrayList<>() ;
        String query = "SELECT object_id FROM objects_table \n" +
                "WHERE camera_id=? and\n" +
                "timestamp BETWEEN ? and ?\n" +
                "GROUP BY object_id";


        // get all records for the query
        List<Map<String, Object>> res = sql.get(
                query,
                cameraId, fromTime, toTime
        );

        if (!res.isEmpty()) {
            for (Map<String, Object> re : res) {

                Product product = new Product(re);

                products.add(product);

            }
        }
        return products;
    }

}
