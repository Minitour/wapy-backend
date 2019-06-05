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
    public List<Product> getAllProductsInWindow(String owner_uid, String camera_id, Timestamp fromTime, Timestamp toTime) throws SQLException {
        List<Product> rawProducts = new ArrayList<>();
        try(DashboardAccess access = new DashboardAccess(this)) {
            rawProducts = access.getAllProductInWindow(owner_uid, fromTime, toTime);

            List<Product> products = new ArrayList<>();
            for (Product rawProduct : rawProducts) {
                if (rawProduct.getCamera_id().equals(camera_id))
                    products.add(rawProduct);
            }
            return products;

        }catch (Exception e) {
            e.printStackTrace();
        }
        return rawProducts;
    }

    /**
     * Return the most viewed product in the specific window
     * @param owner_uid
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public Product getMostViewedProductInWindow(String owner_uid, String camera_id, Timestamp fromTime, Timestamp toTime) throws SQLException {
        if (fromTime.after(toTime))
            return null;

        String query = "SELECT count(object_id) as value, object_id, timestamp, camera_id \n" +
                "FROM objects_table \n" +
                "WHERE timestamp BETWEEN ? and ?\n" +
                "AND owner_uid=? and \n" +
                "camera_id = ? \n" +
                "GROUP BY object_id\n" +
                "ORDER BY value DESC\n" +
                "LIMIT 1";

        List<Map<String, Object>> res = sql.get(
                query,
                fromTime, toTime, owner_uid, camera_id
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
    public Product getLeastViewedProductInWindow(String owner_uid, String camera_id, Timestamp fromTime, Timestamp toTime) throws SQLException {
        if (fromTime.after(toTime))
            return null;

        String query = "SELECT count(object_id) as value, object_id \n" +
                "FROM objects_table \n" +
                "WHERE timestamp BETWEEN ? and ?\n" +
                "AND owner_uid=? \n" +
                "and camera_id = ? \n" +
                "GROUP BY object_id\n" +
                "ORDER BY value ASC\n" +
                "LIMIT 1";

        List<Map<String, Object>> res = sql.get(
                query,
                fromTime, toTime, owner_uid, camera_id
        );

        if (res.isEmpty())
            return null;

        if (debug)
            System.out.println(res);

        Product product = new Product(res.get(0));

        return product;
    }
}
