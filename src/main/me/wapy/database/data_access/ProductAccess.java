package me.wapy.database.data_access;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
                fromTime, toTime, objectId, owner_uid
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
    public Long getReactionsPerProduct(String owner_uid, String product_id, Timestamp fromTime, Timestamp toTime) throws SQLException {
/*
select count(object_id) from images_table
where calm > 50.0 and happy > 50.0 and surprised > 50.0 and object_id =
 */
        String query = "SELECT count(object_id) as likes from images_table " +
                "WHERE ((calm > 50.0 and happy > 50.0 and surprised > 50.0) or (smile = 1)) and owner_uid = ? and object_id = ? and timestamp BETWEEN ? and ?";

        // get all records for the query
        List<Map<String, Object>> res = sql.get(
                query,
                owner_uid, product_id, fromTime, toTime
        );

        Long likes = 0L;

        if (!res.isEmpty()) {

            likes += (Long) res.get(0).get("likes");
        }

        return likes;
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

    public JsonObject getProductViewsPerGender(String owner_uid, String productId, Timestamp fromTime, Timestamp toTime) throws SQLException {

        String query = "SELECT gender, count(gender) as views FROM images_table WHERE owner_uid = ? and object_id = ? and timestamp between ? and ? GROUP BY gender";

        List<Map<String, Object>> res = sql.get(
                query,
                owner_uid, productId, fromTime, toTime
        );

        if (res.isEmpty())
            return null;

        if (debug)
            System.out.println(res);

        Long maleViews = 0L;
        Long femaleViews = 0L;

        for (Map<String, Object> re : res) {
            if (re.get("gender").toString().equals("m")) {
                maleViews = (Long) re.get("views");
            } else{
                femaleViews = (Long) re.get("views");
            }
        }

        JsonObject views = new JsonObject();
        views.addProperty("male", maleViews);
        views.addProperty("female", femaleViews);

        return views;

    }


    public JsonArray getAgeRangeValuesForProduct(String owner_uid, String productId, Timestamp fromTime, Timestamp toTime) throws SQLException {
//        String[] fields = {"calm","happy","confused","disgusted","angry","sad","surprised"};
        String template = "SELECT '%s' as group_id, count(*) as age FROM wapy_db.images_table where ((age_low + age_high) / 2) between ? and ? and owner_uid = ? and object_id = ? and timestamp between ? and ?";

        //String template = "(select count(*) as value, '%s' as type from images_table where %s > 50 and owner_uid = ? and timestamp between ? and ?)";
        List<Object> args = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        int groupSize = 8;
        int gapSize = 10;

        for (int i = 0; i < groupSize; i++) {

            // create query from template.
            String range = i < groupSize - 1
                    ? String.format("Age %d-%d",(i+1) * gapSize,(i+1) * gapSize + gapSize)
                    : String.format("Age %d+",(i+1) * gapSize);

            query.append(String.format(template,range));

            // add arguments.
            args.add(((i+1) * gapSize));
            args.add((i+1) * gapSize + gapSize);
            args.add(owner_uid);
            args.add(productId);
            args.add(fromTime);
            args.add(toTime);

            // append union all only if not the last entry.
            if(i < groupSize - 1)
                query.append(" UNION ALL ");
        }
        // run the query.
//        List<Map<String,Object>> rs = sql.get(query.toString(),args.toArray());
        return sql.read(query.toString(),args.toArray());
    }


}
