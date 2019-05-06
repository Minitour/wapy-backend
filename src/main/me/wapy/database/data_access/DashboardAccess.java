package me.wapy.database.data_access;

import me.wapy.database.Database;
import me.wapy.model.Product;

import java.sql.SQLException;
import java.sql.Timestamp;
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
     * @param storeId
     * @param fromTime start time for traffic
     * @param toTime end time for traffic
     * @return long value of the traffic
     * @throws SQLException
     */
    public Long getTraffic(String storeId, Timestamp fromTime, Timestamp toTime) throws SQLException {

        if (fromTime.after(toTime))
            return 0L;

        List<Map<String,Object>> rs = sql.get(
                "SELECT count(*) as value FROM objects_table WHERE store_id = ? AND timestamp BETWEEN ? and ?",
                storeId, fromTime, toTime
        );

        if (rs.isEmpty())
            return 0L;

        if (debug)
            System.out.println(rs);

        return (Long) rs.get(0).get("value");
    }


    /**
     * Returns the most viewed product in a given store and given time interval
     * @param storeId
     * @param fromTime start time for views
     * @param toTime end time for views
     * @return string value of the most viewed product
     * @throws SQLException
     */
    public Map<String, Integer> getMostViewedProduct(String storeId, Timestamp fromTime, Timestamp toTime) throws SQLException {

        if (fromTime.after(toTime))
            return null;

        String query = "SELECT count(object_id) as most_viewed, object_id \n" +
                "FROM objects_table \n" +
                "WHERE store_id=? AND timestamp BETWEEN ? and ?\n" +
                "GROUP BY object_id\n" +
                "ORDER BY most_viewed DESC\n" +
                "LIMIT 4";

        List<Map<String, Object>> res = sql.get(
                query,
                storeId, fromTime, toTime
        );

        if (res.isEmpty())
            return null;

        if (debug)
            System.out.println(res);

        Map<String, Integer> product = null;
        product.put((String) res.get(0).get("object_id"), (Integer)res.get(0).get("most_viewed"));

        return product;
    }


    /**
     * Resturn the least viewed product given store and time interval
     * @param storeId
     * @param fromTime start time for views
     * @param toTime end time for views
     * @return string value of the most viewed product
     * @throws SQLException
     */
    public Map<String, Integer> getLeastViewedProduct(String storeId, Timestamp fromTime, Timestamp toTime) throws SQLException {

        if (fromTime.after(toTime))
            return null;

        String query = "SELECT count(object_id) as least_viewed, object_id \n" +
                "FROM objects_table \n" +
                "WHERE store_id=? AND timestamp BETWEEN ? and ?\n" +
                "GROUP BY object_id\n" +
                "ORDER BY least_viewed ASC\n" +
                "LIMIT 4";

        List<Map<String, Object>> res = sql.get(
                query,
                storeId, fromTime, toTime
        );

        if (res.isEmpty())
            return null;

        if (debug)
            System.out.println(res);

        Map<String, Integer> product = null;
        product.put((String) res.get(0).get("object_id"), (Integer)res.get(0).get("least_viewed"));

        return product;

    }

    /**
     * Returns the most viewed product reaction given store id and time interval
     * @param storeId
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public Map<String, Integer> getMostViewedProductReaction(String storeId, Timestamp fromTime, Timestamp toTime) throws SQLException {
        if (fromTime.after(toTime))
            return null;


        String query = "SELECT count(object_id) as most_viewed, \n" +
                "object_id FROM images_table\n" +
                "where store_id=? AND\n" +
                "timestamp BETWEEN ? and ?\n" +
                "GROUP BY object_id\n" +
                "ORDER BY most_viewed DESC\n" +
                "LIMIT 1";

        List<Map<String, Object>> res = sql.get(
                query,
                storeId, fromTime, toTime
        );

        if (res.isEmpty())
            return null;

        if (debug)
            System.out.println(res);

        Map<String, Integer> product = null;
        product.put((String) res.get(0).get("object_id"), (Integer)res.get(0).get("most_viewed"));

        return product;
    }

    /**
     * Returns the least viewed product reaction given store id and time interval
     * @param storeId
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public Map<String, Integer> getLeastViewedProductReaction(String storeId, Timestamp fromTime, Timestamp toTime) throws SQLException {
        if (fromTime.after(toTime))
            return null;


        String query = "SELECT count(object_id) as least_viewed, \n" +
                "object_id, FROM images_table\n" +
                "where store_id=? AND\n" +
                "timestamp BETWEEN ? and ?\n" +
                "GROUP BY object_id\n" +
                "ORDER BY least_viewed ASC\n" +
                "LIMIT 1";

        List<Map<String, Object>> res = sql.get(
                query,
                storeId, fromTime, toTime
        );

        if (res.isEmpty())
            return null;

        if (debug)
            System.out.println(res);

        Map<String, Integer> product = null;
        product.put((String) res.get(0).get("object_id"), (Integer)res.get(0).get("least_viewed"));

        return product;


    }

    /**
     * Return the number of people detected watching the window
     * @param storeId
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public Integer getExposure(String storeId, Timestamp fromTime, Timestamp toTime) throws SQLException {
        if (fromTime.after(toTime))
            return null;

        String query = "SELECT count(object_id) as views FROM objects_table\n" +
                "WHERE store_id=? AND timestamp BETWEEN ? and ?";

        List<Map<String, Object>> res = sql.get(
                query,
                storeId, fromTime, toTime
        );

        if (res.isEmpty())
            return null;

        if (debug)
            System.out.println(res);

        return (Integer) res.get(0).get("views");
    }

    /**
     * Returns the number of smiles for specific object for given store and time interval
     * @param storeId
     * @param fromTime
     * @param toTime
     * @param object_id
     * @return
     * @throws SQLException
     */
    public Integer getSmilesForProduct(String storeId, Timestamp fromTime, Timestamp toTime, String object_id) throws SQLException {
        String query = "select count(smile) as value from images_table\n" +
                "where store_id=? and object_id = ? and timestamp between ? and ?\n" +
                "AND smile=1";

        List<Map<String, Object>> res = sql.get(
                query,
                storeId, object_id, fromTime, toTime
        );

        if (res.isEmpty())
            return null;

        if (debug)
            System.out.println(res);

        String r = String.valueOf(res.get(0).get("value"));
        return Integer.valueOf(r);
    }

    /**
     * Returns a list of dictionary with all reactions for given store and time interval
     * @param storeId
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public List<Map<String, Integer>> getReactions(String storeId, Timestamp fromTime, Timestamp toTime) throws SQLException {
        List<Map<String, Integer>> reactions = null;

        String[] emotions = {"calm", "happy", "confused", "disgusted", "angry", "sad"};

        for (String emotion : emotions) {
            // construct the query
            String query = "select count(object_id) as value from images_table\n" +
                    "where store_id=? and timestamp between ? and ?\n" +
                    "AND " + emotion +" >= 50.0";

            // get all records for the query
            List<Map<String, Object>> res = sql.get(
                    query,
                    storeId, fromTime, toTime
            );

            Map<String,Integer> e = null;
            if (!res.isEmpty()) {
                // get the value for the emotion
                String r = String.valueOf(res.get(0).get("value"));

                // construct the pair
                e.put(emotion, Integer.valueOf(r));

            } else {

                // insert 0 for the emotion
                e.put(emotion, 0);
            }

            // add to the reactions
            reactions.add(e);
        }

        return reactions;
    }

    /**
     * Returns all product monitored in window
     * @param storeId
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public List<Map<String, Integer>> getAllProductInWindow(String storeId, Timestamp fromTime, Timestamp toTime) throws SQLException {
        List<Map<String, Integer>> products = null;
        String query = "SELECT object_id , count(object_id) as value FROM objects_table \n" +
                "WHERE store_id = ? AND\n" +
                "timestamp BETWEEN ? and ?\n" +
                "GROUP BY object_id";


        // get all records for the query
        List<Map<String, Object>> res = sql.get(
                query,
                storeId, fromTime, toTime
        );

        if (!res.isEmpty()) {
            for (Map<String, Object> re : res) {

                // getting the values and objects id
                String object_id = String.valueOf(re.get("object_id"));
                Integer value = Integer.valueOf(String.valueOf(re.get("value")));

                // construct the Map of object id and the number of views
                Map<String,Integer> e = null;
                e.put(object_id, value);

                // add to the list of products
                products.add(e);
            }
        }

        return products;

    }


}
