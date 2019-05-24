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
                String.valueOf(fromTime), String.valueOf(toTime)
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
     * @return
     * @throws SQLException
     */
    public List<Product> getSmilesForProduct(Timestamp fromTime, Timestamp toTime, String owner_uid) throws SQLException {

        List<Product> productList = getAllProductInWindow(owner_uid, fromTime, toTime);

        if (productList.isEmpty())
            return new ArrayList<Product>();

        try(ProductAccess access = new ProductAccess(this)) {
            for (Product product : productList) {

                Long smiles = access.getSmilesForProduct(fromTime, toTime, product.getObject_id(), owner_uid);
                product.setValue(smiles);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return productList;
    }

    /**
     * Returns a list of dictionary with all reactions for given store and time interval
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public List<Map<String,List<Reaction>>> getReactionsPerProduct(String owner_uid, Timestamp fromTime, Timestamp toTime) throws SQLException {
        List<Map<String,List<Reaction>>> productsReactions = new ArrayList<>();

        List<Product> productList = getAllProductInWindow(owner_uid, fromTime, toTime);

        if (productList.isEmpty())
            return productsReactions;

        try(BoxAccess access = new BoxAccess(this)) {
            Map<String, List<Reaction>> productReaction = new HashMap<>();
            for (Product product : productList) {

                // getting all reactions for product
                List<Reaction> reactions = access.getAllReactionsPerProductPerBox(product.getObject_id(), owner_uid, fromTime, toTime);

                // construct a hashmap
                productReaction.put(product.getObject_id(), reactions);

                // add to the list of product with reactions
                productsReactions.add(productReaction);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return productsReactions;
    }

    /**
     * Returns all product monitored in window
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public List<Product> getAllProductInWindow(String owner_uid, Timestamp fromTime, Timestamp toTime) throws SQLException {
        List<Product> products = new ArrayList<>() ;
        String query = "SELECT object_id, camera_id, timestamp FROM objects_table \n" +
                "WHERE owner_uid = ? and\n" +
                "timestamp BETWEEN ? and ?\n" +
                "GROUP BY object_id";


        // get all records for the query
        List<Map<String, Object>> res = sql.get(
                query,
                owner_uid, fromTime, toTime
        );

        if (!res.isEmpty()) {
            for (Map<String, Object> re : res) {

                Product product = new Product(re);

                products.add(product);

            }
        }
        return products;
    }

    /**
     * Gets all reactions in a given time frame based on account id.
     *
     * Response example:
     *
     * | value | type  |
     * | ----- | ----- |
     * | 232   | happy |
     * | 13    | sad   |
     * | 51    | calm  |
     *
     *
     * @param userId The id of the account.
     * @param fromTime The start time.
     * @param toTime The end time.
     * @return
     * @throws SQLException
     */
    public List<Reaction> getReactionSummary(String userId, Timestamp fromTime, Timestamp toTime) throws SQLException {
        String[] fields = {"calm","happy","confused","disgusted","angry","sad","surprised"};
        String template = "(select count(*) as value, '%s' as type from images_table where %s > 50 and owner_uid = ? and timestamp between ? and ?)";
        List<Object> args = new ArrayList<>();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < fields.length; i++) {

            // create query from template.
            String reaction = fields[i];
            query.append(String.format(template,reaction,reaction));

            // add arguments.
            args.add(userId);
            args.add(fromTime);
            args.add(toTime);

            // append union all only if not the last entry.
            if(i < fields.length - 1)
                query.append(" UNION ALL ");
        }
        System.out.println(query.toString());
        // run the query.
        List<Map<String,Object>> rs = sql.get(query.toString(),args.toArray());

        // parse results
        List<Reaction> reactions = new ArrayList<>();
        for (Map<String, Object> r : rs) {
            Long value = (Long) r.get("value");
            String type = (String) r.get("type");
            reactions.add(new Reaction(type,value));
        }

        return reactions;
    }

}
