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
    public Product getMostViewedProduct(String storeId, Timestamp fromTime, Timestamp toTime) throws SQLException {

        if (fromTime.after(toTime))
            return null;

        String query = "SELECT count(object_id) as most_viewed, object_id \n" +
                "FROM objects_table \n" +
                "WHERE store_id=? AND timestamp BETWEEN ? and ?\n" +
                "GROUP BY object_id\n" +
                "ORDER BY most_viewed DESC\n" +
                "LIMIT 4";

        return getStringQuery(storeId, fromTime, toTime, query);
    }


    /**
     * Resturn the least viewed product given store and time interval
     * @param storeId
     * @param fromTime start time for views
     * @param toTime end time for views
     * @return string value of the most viewed product
     * @throws SQLException
     */
    public Product getLeastViewedProduct(String storeId, Timestamp fromTime, Timestamp toTime) throws SQLException {

        if (fromTime.after(toTime))
            return null;

        String query = "SELECT count(object_id) as least_viewed, object_id \n" +
                "FROM objects_table \n" +
                "WHERE store_id=? AND timestamp BETWEEN ? and ?\n" +
                "GROUP BY object_id\n" +
                "ORDER BY least_viewed ASC\n" +
                "LIMIT 4";

        return getStringQuery(storeId, fromTime, toTime, query);
    }

    /**
     * Returns the most viewed product reaction given store id and time interval
     * @param storeId
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public Product getMostViewedProductReaction(String storeId, Timestamp fromTime, Timestamp toTime) throws SQLException {
        if (fromTime.after(toTime))
            return null;


        String query = "SELECT count(object_id) as most_viewed, \n" +
                "object_id FROM images_table\n" +
                "where store_id=? AND\n" +
                "timestamp BETWEEN ? and ?\n" +
                "GROUP BY object_id\n" +
                "ORDER BY most_viewed DESC\n" +
                "LIMIT 1";

        return getStringQuery(storeId, fromTime, toTime, query);

    }

    /**
     * Returns the least viewed product reaction given store id and time interval
     * @param storeId
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public Product getLeastViewedProductReaction(String storeId, Timestamp fromTime, Timestamp toTime) throws SQLException {
        if (fromTime.after(toTime))
            return null;


        String query = "SELECT count(object_id) as least_viewed, \n" +
                "object_id FROM images_table\n" +
                "where store_id=? AND\n" +
                "timestamp BETWEEN ? and ?\n" +
                "GROUP BY object_id\n" +
                "ORDER BY least_viewed ASC\n" +
                "LIMIT 1";

        return getStringQuery(storeId, fromTime, toTime, query);

    }

    /**
     * Return the String value of the object id for specific query
     * @param storeId
     * @param fromTime
     * @param toTimer
     * @param query
     * @return
     * @throws SQLException
     */
    private Product getStringQuery(String storeId, Timestamp fromTime, Timestamp toTimer, String query) throws SQLException {
        List<Map<String, Object>> res = sql.get(
                query,
                storeId, fromTime, toTimer
        );

        if (res.isEmpty())
            return null;

        if (debug)
            System.out.println(res);

        return new Product(res.get(0));

        //return String.valueOf(res.get(0).get("object_id"));
    }
}
