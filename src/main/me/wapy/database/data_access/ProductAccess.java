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
     * Returns the amount of people who liked the specific product
     * @param objectId
     * @param fromTime
     * @param toTime
     * @return
     * @throws SQLException
     */
    public Long getTotalLikesPerProduct(String owner_uid, String objectId, Timestamp fromTime, Timestamp toTime) throws SQLException {
        List<String> emotions = new ArrayList<>();
        emotions.add("calm");
        emotions.add("happy");

        Long counter = 0L;

        // checking if the person is smiling
        Long c = getSmilesForProduct(fromTime, toTime, objectId, owner_uid);
        if (c > 0)
            counter += c;

        try(BoxAccess access = new BoxAccess(this)) {

            // getting all reactions for object
            List<Reaction> reactions = access.getAllReactionsPerProductPerBox(objectId, owner_uid, fromTime, toTime);

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
