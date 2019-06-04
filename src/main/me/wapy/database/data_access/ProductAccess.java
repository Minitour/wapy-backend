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
    public Long getTotalViewsPerProduct(String owner_uid, String objectId, Timestamp fromTime, Timestamp toTime) throws SQLException {

        String query = "select count(object_id) as value FROM objects_table " +
                "WHERE (timestamp BETWEEN ? and ?) AND (object_id = ?) AND (owner_uid = ?)";

        // getting the results for the query
        List<Map<String, Object>> res = sql.get(
                query,
                String.valueOf(fromTime), String.valueOf(toTime), objectId, owner_uid
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
     * Returns a list of dictionary with all reactions for given store and time interval
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public List<Reaction> getReactionsPerProduct(String owner_uid, String product_id, Timestamp fromTime, Timestamp toTime) throws SQLException {
        List<Reaction> productsReactions = new ArrayList<>();
/*
select count(object_id) from images_table
where calm > 50.0 and happy > 50.0 and surprised > 50.0 and object_id =
 */
        String query = "SELECT object_id, count(object_id) as likes from images_table " +
                "WHERE ((calm > 50.0 and happy > 50.0 and surprised > 50.0) or (smile = 1)) and owner_uid = ? and object_id = ? and timestamp BETWEEN ? and ?";

        // get all records for the query
        List<Map<String, Object>> res = sql.get(
                query,
                owner_uid, product_id, fromTime, toTime
        );

        if (!res.isEmpty()) {
            for (Map<String, Object> re : res) {

                Long value = (Long) res.get(0).get("likes");
                String key = res.get(0).get("object_id").toString();
                Reaction reaction = new Reaction(key, value);

                productsReactions.add(reaction);

            }
        }

        return productsReactions;
    }

    /**
     * Returns the number of smiles for specific object for given store and time interval
     * @param fromTime
     * @param toTime
     * @param object_id
     * @return
     * @throws SQLException
     */
    public Long getSmilesForProduct(Timestamp fromTime, Timestamp toTime, String object_id, String owner_uid) throws SQLException {
        String query = "select count(smile) as value from images_table\n" +
                "where object_id = ? and timestamp between ? and ?\n" +
                "AND smile=1 AND owner_uid=?";

        List<Map<String, Object>> res = sql.get(
                query,
                object_id, fromTime, toTime, owner_uid
        );

        if (res.isEmpty())
            return 0L;

        if (debug)
            System.out.println(res);

        return (Long) res.get(0).get("value");
    }

    /**
     * Gets all reactions in a given time frame based on account id and product id.
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
    public List<Reaction> getProductReactionSummary(String userId, String object_id, Timestamp fromTime, Timestamp toTime) throws SQLException {
        String[] fields = {"calm","happy","confused","disgusted","angry","sad","surprised"};
        String template = "(select count(*) as value, '%s' as type from images_table where %s > 50 and owner_uid = ? and object_id = ? and timestamp between ? and ?)";
        List<Object> args = new ArrayList<>();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < fields.length; i++) {

            // create query from template.
            String reaction = fields[i];
            query.append(String.format(template,reaction,reaction));

            // add arguments.
            args.add(userId);
            args.add(object_id);
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
