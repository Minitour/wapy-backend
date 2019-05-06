package me.wapy.database.data_access;

import me.wapy.database.Database;
import me.wapy.model.Product;

import java.sql.SQLException;
import java.sql.Timestamp;
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
     * @param cameraId
     * @param fromTime
     * @param toTime
     * @return
     */
    public List<Product> getAllProductsInWindow(String cameraId, Timestamp fromTime, Timestamp toTime) throws SQLException {

        try(DashboardAccess access = new DashboardAccess(this)) {
            List<Product> products = access.getAllProductInWindow(cameraId, fromTime, toTime);
            return products;

        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return the most viewed product in the specific window
     * @param cameraId
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public Product getMostViewedProductInWindow(String cameraId, Timestamp fromTime, Timestamp toTime) throws SQLException {
        if (fromTime.after(toTime))
            return null;

        String query = "SELECT count(object_id) as value, object_id \n" +
                "FROM objects_table \n" +
                "WHERE timestamp BETWEEN ? and ?\n" +
                "AND camera_id=?\n" +
                "GROUP BY object_id\n" +
                "ORDER BY value DESC\n" +
                "LIMIT 1";

        List<Map<String, Object>> res = sql.get(
                query,
                fromTime, toTime, cameraId
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
     * @param cameraId
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public Product getLeastViewedProductInWindow(String cameraId, Timestamp fromTime, Timestamp toTime) throws SQLException {
        if (fromTime.after(toTime))
            return null;

        String query = "SELECT count(object_id) as value, object_id \n" +
                "FROM objects_table \n" +
                "WHERE timestamp BETWEEN ? and ?\n" +
                "AND camera_id=?\n" +
                "GROUP BY object_id\n" +
                "ORDER BY value ASC\n" +
                "LIMIT 1";

        List<Map<String, Object>> res = sql.get(
                query,
                fromTime, toTime, cameraId
        );

        if (res.isEmpty())
            return null;

        if (debug)
            System.out.println(res);

        Product product = new Product(res.get(0));

        return product;
    }

}
