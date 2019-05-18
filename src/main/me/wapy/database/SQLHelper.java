package me.wapy.database;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Antonio Zaitoun on 23/02/2018.
 */
public class SQLHelper {

    private final Connection connection;
    private boolean $testMode = false;

    public SQLHelper $_test_mode(boolean on){
        $testMode = on;
        return this;
    }

    public SQLHelper(Connection connection) {
        this.connection = connection;
    }

    /**
     * Use this method to make Database2 Queries.
     *
     * Usage Example:
     *      @code { get("SELECT * FROM USERS WHERE EMAIL = ?",email) }
     *
     * @param query The Query String.
     * @return A List Of Hash Maps of Type (String:Object)
     * @throws SQLException
     */
    public List<Map<String,Object>> get(String query, Object...args) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);

        int index = 1;

        for(Object val : args)
            /* Handle Collection */
            if (val.getClass() == ArrayList.class) {
                for (String v: (ArrayList<String>) val) {
                    if (isInteger(v))
                        statement.setObject(index++,Integer.valueOf(v));
                    else
                        statement.setObject(index++,v);
                }
            }/* End Handle Collection */
            else
                statement.setObject(index++,val);

        ResultSet set = statement.executeQuery();

        String[] columns = new String[set.getMetaData().getColumnCount()];

        for (int i = 1; i <= columns.length; i++ )
            columns[i - 1] = set.getMetaData().getColumnLabel(i);


        List<Map<String,Object>> data = new ArrayList<>();

        while (set.next()){
            Map<String,Object> map = new HashMap<>();
            for(String name : columns)
                map.put($testMode ? name.toLowerCase() : name,set.getObject(name));

            data.add(map);

        }

        try{
            //close result set
            if(!set.isClosed())
                set.close();

            if(!statement.isClosed())
                statement.close();

        }catch (Exception e){
            e.printStackTrace();
        }

        return data;
    }

    /**
     * read works the same way that get works except that it converts the data into JSON elements rather then list of HashMaps.
     * Usage Example:
     *      @code { get("SELECT * FROM USERS WHERE EMAIL = ?",email) }
     *
     * @param query The Query String.
     * @param args The arguments sent to the query.
     * @return JsonArray containing the result.
     * @throws SQLException
     */
    public JsonArray read(String query, Object... args) throws SQLException {
        // create prepared statement
        PreparedStatement statement = connection.prepareStatement(query);
        Gson gson = new Gson();

        int index = 1;

        for(Object val : args)
            statement.setObject(index++, val);

        //Execute query and get result set
        ResultSet set = statement.executeQuery();

        // create array to store column names
        String[] columns = new String[set.getMetaData().getColumnCount()];

        // get column names
        for (int i = 1; i <= columns.length; i++ )
            columns[i - 1] = set.getMetaData().getColumnLabel(i);

        JsonArray array = new JsonArray();

        while (set.next()){
            JsonObject object = new JsonObject();
            for (String column : columns) {
                if ($testMode) {
                    object.add(column.toLowerCase(),
                            gson.toJsonTree(set.getObject(column)));
                } else {
                    object.add(column,
                            gson.toJsonTree(set.getObject(column)));
                }
            }
            array.add(object);
        }

        try{
            //close result set
            if(!set.isClosed())
                set.close();

            if(!statement.isClosed())
                statement.close();

        }catch (Exception e){
            e.printStackTrace();
        }

        return array;
    }

    @SuppressWarnings("Do not use this method unless you are in the testing environment!")
    public boolean raw(String query) throws SQLException {
        if(!$testMode) {
            throw new SQLException("Not in testing environment!");
        }

        try(PreparedStatement statement = connection.prepareStatement(query)){

            return statement.execute();
        }
    }

    /**
     *
     * Use this method to insert data into a certain table.
     *
     * Usage Example:
     *      @code {
     *          insert("SESSIONS",
     *               new Column("ID",id),
     *               new Column("SESSION_TOKEN",token),
     *               new Column("CREATION_DATE",new Date())
     *          );
     *      }
     *
     *      @see Column
     *
     *
     * @param table The name of the table.
     * @param values Var args of Pairs of Type (String:Object). Use Column for ease of use.
     * @param <T> The type of the generated key.
     * @return
     * @throws SQLException
     */
    public <T> T insert(String table, Column...values) throws SQLException {
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            StringBuilder builder1 = new StringBuilder();
            StringBuilder builder2 = new StringBuilder();

            for (Column value : values) {
                if (value.shouldIgnore()) {
                    builder1.append(value.getKey()).append(",");
                    builder2.append("?").append(",");
                }
            }

            builder1.deleteCharAt(builder1.length() - 1);
            builder2.deleteCharAt(builder2.length() - 1);

            String query = "INSERT INTO " + table + " (" + builder1.toString() + ") VALUES (" + builder2.toString() + ");";

            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            int index = 1;
            for (Column obj : values)
                if (obj.shouldIgnore())
                    statement.setObject(index++, obj.getValue());

            statement.executeUpdate();

            rs = statement.getGeneratedKeys();

            return rs.next() ? (T) rs.getObject(1) : null;
        }finally {

            if (rs != null && !rs.isClosed()) rs.close();

            if (statement != null && !statement.isClosed()) statement.close();
        }
    }

    /**
     *
     * @param table
     * @param dbObject
     * @param <T>
     * @return
     * @throws SQLException
     */
    public <T> T insert(String table,DBObject dbObject) throws SQLException {
        return insert(table,dbObject.db_columns());
    }

    /**
     *
     * @param object
     * @param <T>
     * @return
     * @throws SQLException
     */
    public <T> T insert(DBObject object) throws SQLException{
        return insert(object.db_table(),object.db_columns());
    }

    /**]
     * Inserts multiple DBObjects of the same type into the database with a single query.
     *
     * @param entries The objects to insert. Must be of the same type.
     * @throws SQLException
     */
    public void insertMany(List<DBObject> entries) throws SQLException {
        if(entries.isEmpty()) {
            return;
        }
        DBObject first = entries.get(0);
        String tableName = first.db_table();

        List<Column[]> columns = new ArrayList<>();

        for (DBObject entry : entries) {
            columns.add(entry.db_columns());
        }

        insertMany(tableName,columns);
    }

    /***
     * This method is used to insert multiple entries of the same type into a table with a single query.
     *
     * @param table The name of the table.
     * @param entries A list of columns.
     * @throws SQLException
     */
    public void insertMany(String table, List<Column[]> entries) throws SQLException {

        if(entries.isEmpty()) {
            return;
        }

        Column[] values = entries.get(0);

        PreparedStatement statement = null;

        try {
            // create field names builder. will contain
            StringBuilder fieldNamesBuilder = new StringBuilder();

            // create template builder. will contain "(?,?,?),"
            StringBuilder templateBuilder = new StringBuilder();

            for (int i = 0; i < values.length; i++) {
                boolean isLast = i == values.length - 1;

                Column value = values[i];

                if (value.shouldIgnore()) {
                    fieldNamesBuilder.append(value.getKey());
                    templateBuilder.append("?");

                    if(!isLast) {
                        fieldNamesBuilder.append(",");
                        templateBuilder.append(",");
                    }
                }
            }

            //fieldNameBuilder = "key1,key2,key3"

            templateBuilder.insert(0,"(");
            templateBuilder.append("),");

            // templateBuilder = "(?,?,?),"


            for (int i = 1; i < entries.size(); i++) {
                Column[] columns = entries.get(i);

                templateBuilder.append("("); //  "(?,?,?),("

                for (int i1 = 0; i1 < columns.length; i1++) {
                    boolean isLast = i1 == columns.length - 1;

                    Column value = columns[i1];

                    if (value.shouldIgnore()) {
                        templateBuilder.append("?");

                        if (!isLast) {
                            templateBuilder.append(",");
                        }
                    }
                }


                templateBuilder.append("),"); //  "(?,?,?),(?,?,?),"
            }

            // remove comma at the end.
            templateBuilder.deleteCharAt(templateBuilder.length() - 1);

            // create query string
            String query = String.format(
                    "INSERT INTO %s (%s) VALUES %s;",
                    table,
                    fieldNamesBuilder.toString(),
                    templateBuilder.toString()
            );

            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            int index = 1;
            for (Column[] entry : entries)
                for (Column obj : entry)
                    if (obj.shouldIgnore())
                        statement.setObject(index++, obj.getValue());


            statement.executeUpdate();
        }finally {
            if (statement != null && !statement.isClosed()) statement.close();
        }
    }

    /**
     * Use this method to update an existing entry.
     *
     * @param table The table
     * @param where The condition
     * @param values The values to set/update
     * @return
     * @throws SQLException
     */
    public boolean update(String table, Where where, Column...values) throws SQLException {
        PreparedStatement statement = null;
        try {
            StringBuilder builder = new StringBuilder();

            for (Column value : values)
                if (value.shouldIgnore())
                    builder.append(value.getKey()).append(" = ").append("?,");

            if (builder.length() > 1)
                builder.deleteCharAt(builder.length() - 1);

            String query = "UPDATE " + table + " SET " + builder.toString() + " WHERE " + where.syntax;
            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            int index = 1;
            for (Column obj : values)
                if (obj.shouldIgnore())
                    statement.setObject(index++, obj.getValue());

            for (Object o : where.values)
                statement.setObject(index++, o);

            return statement.executeUpdate() != 0;
        }finally {
            if(statement!= null && !statement.isClosed()) statement.close();
        }
    }

    /**
     * Use this method to delete entries.
     *
     * Usage Example:
     *      @code {
     *
     *          //Example for deleting a session via token or id
     *          delete("SESSIONS","ID = ? OR TOKEN = ?",id,token);
     *
     *          //Simple Example for Deleting an account with a certain email.
     *          delete("ACCOUNTS","EMAIL = ?",email);
     *
     *          //Deleting multiple accounts with ids 32,542,22
     *          delete("ACCOUNTS","ID in (?, ?, ?)",32,542,22);
     *      }
     *
     * @param table The name of the table.
     * @param where The predicate/condition.
     * @param values The values.
     * @throws SQLException
     */
    public boolean delete(String table, String where, Object... values) throws SQLException{

        PreparedStatement statement = null;

        try {
            String query = "DELETE FROM " + table + " WHERE " + where;


            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            int index = 1;
            for (Object obj : values)
                statement.setObject(index++, obj);

            return statement.executeUpdate() != 0;
        }finally {
            if (statement != null && !statement.isClosed())
                statement.close();
        }
    }

    /**
     * Use this method to delete multiple entries. Instead of inserting many wildcards use `#` operator.
     *
     * @code {
     *
     *          deleteMany("ACCOUNTS","ID in (#)",32,542,22)
     *
     *          //Is the same as:
     *          delete("ACCOUNTS","ID in (?, ?, ?)",32,542,22);
     *      }
     *
     * @param table
     * @param where
     * @param values
     * @return
     * @throws SQLException
     */
    public void deleteMany(String table, String where, String... values) throws SQLException{
        StringBuilder builder = new StringBuilder();

        for(String ignored : values)
            builder.append("?").append(",");

        builder.deleteCharAt(builder.length()-1);

        delete(table, where.replace("#", builder.toString()), values);
    }

    public void beginTransaction(TransactionCallback callback) throws SQLException {

        // store current auto commit state to restore it later.
        boolean isAutoCommit = connection.getAutoCommit();

        SQLException thrownException = null;

        connection.setAutoCommit(false);
        try {
            // perform sql operations
            callback.beginTransaction(this);

            // commit changes if exception was not thrown.
            connection.commit();

        }
        catch (SQLException e1){
            thrownException = e1;
            connection.rollback();
        }
        catch (Exception e){
            connection.rollback();
        } finally {

            // restore connection to original state.
            connection.setAutoCommit(isAutoCommit);

            // throw exception
            if (thrownException != null)
                throw thrownException;
        }
    }

    /**
     * this method checks if the string was integer
     * @param input
     * @return
     */
    private boolean isInteger(String input)
    {
        try
        {
            Integer.parseInt( input );
            return true;
        }
        catch( Exception e)
        {
            return false;
        }
    }

    @FunctionalInterface
    public interface TransactionCallback {
        void beginTransaction(SQLHelper sql) throws SQLException;
    }

}
