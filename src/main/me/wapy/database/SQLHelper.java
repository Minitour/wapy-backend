package me.wapy.database;

import me.wapy.database.sql.Where;

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
    private boolean delete(String table, String where, Object... values) throws SQLException{

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
}
