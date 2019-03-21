package me.wapy.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import me.wapy.database.exception.AuthException;
import me.wapy.database.exception.DataAccessException;
import me.wapy.database.exception.MissingPermissionsException;
import me.wapy.database.permission.Permissions;
import me.wapy.utils.Config;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by Antonio Zaitoun on 03/03/2018.
 */
public abstract class Database implements AutoCloseable{


    private static final boolean SHOULD_HANG = Config.main.get("db").getAsJsonObject().get("should_strangle").getAsBoolean();
    private static HikariDataSource ds;
    private static boolean $is_injected = false;
    private static boolean $protect = false;

    public static void init() {}

    /**
     * This method is used to inject a data source. Do not use unless you are in a testing environment
     * @param ds the data source you wish to inject.
     */
    public static void $test_inject_data_source(HikariDataSource ds) {

        if($protect){
            System.err.println("CANNOT INJECT CONNECTION. CONNECTION ALREADY CREATED!");
            return;
        }

        if (Database.ds != null && !Database.ds.isClosed())
            Database.ds.close();

        Database.ds = ds;
        $is_injected = true;
    }

    /**
     * Get the existing data source. use only in testing environment.
     * @return the current data source.
     */
    public static HikariDataSource $test_existing_data_source() {
        if (!$is_injected)
            return null;

        return Database.ds;
    }

    static {

        HikariConfig config = new HikariConfig("db.properties");
        config.addDataSourceProperty("characterEncoding","utf8");
        config.addDataSourceProperty("useUnicode","true");

        //Check/Ping the mysql sql until its up then connect.

        URI uri = URI.create(config.getJdbcUrl().substring(5));
        System.out.println(config.getJdbcUrl());

        while (SHOULD_HANG && !isServerUp(uri.getHost(),uri.getPort())) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            ds = new HikariDataSource(config);
        }catch (HikariPool.PoolInitializationException ex){
            if (SHOULD_HANG)
                ex.printStackTrace();
        }

    }

    private Connection connection;
    protected final SQLHelper sql;

    protected Database() {
        $protect = true;
        initConnection();
        sql = new SQLHelper(connection)
                .$_test_mode($is_injected);

    }


    protected Database(Database database) {
        $protect = true;
        if (database == null)
            initConnection();
        else
            connection = database.connection;

        sql = new SQLHelper(connection)
                .$_test_mode($is_injected);
    }


    private void initConnection(){
        try {
            connection = ds.getConnection();//DriverManager.getConnection(URL, username, password);
        } catch (SQLException e) {
            connection = null;
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static boolean isServerUp(String url, Integer port) {
        boolean isUp = false;
        try {
            Socket socket = new Socket(url, port);
            // Server is up
            isUp = true;
            socket.close();
        }
        catch (IOException e)
        {
            // Server is down
        }
        return isUp;
    }

    @Override
    public void close() throws Exception {
        if(!connection.isClosed())
            connection.close();
    }

    /****************************************       Permission Issue    ******************************/


    protected void isContextValidFor(AuthContext context,String permission) throws DataAccessException {
        validateContext(context);
        boolean result = Permissions.hasPermissionFor(permission, context);

        if(!result)
            throw new MissingPermissionsException();
    }


    /**
     * This method checks if the given context is valid and returns the user_id_rank and user_type for that given context.
     * In other words, this method validates the session and then returns the rank_id and user_type of the user.
     * @param context The auth context.
     * @return user_type and user_id_rank COMBINED of a given context or -1 if invalid.
     * in the order of user_type first and then user_id_rank
     *
     *              for example: user_type = 1 AND user_id_rank = 3
     *                     the method will return 13
     */
    protected void validateContext(AuthContext context) throws AuthException{

        try {
            List<Map<String,Object>> checkSession = sql.get("SELECT `user_id`, `session_token` " +
                    "FROM `tbl_session` " +
                    "WHERE `user_id` = ? AND `session_token` = ?",
                    context.user_id, context.session_token);

            try {

                List<Map<String,Object>> data = sql.get(
                        "SELECT `user_type`, `user_id_rank` "+
                                "FROM `tbl_user` "+
                                "WHERE `user_id` = ?;",
                        checkSession.get(0).get("user_id"));


                Integer rank =  data.size() == 0 ? -1 : (Integer) data.get(0).get("user_id_rank");
                Integer user_type =  data.size() == 0 ? -1 : (Integer) data.get(0).get("user_type");


                context.validate(rank, user_type);

            } catch (SQLException e) {
                throw new AuthException("Invalid Context B");
            }  catch (Exception e) {
                throw new AuthException("Invalid Context C");
            }

        } catch (SQLException e) {
            throw new AuthException("Invalid Context A");
        }
    }


    protected void isContextValidForBusiness(AuthContextBusiness context) throws DataAccessException {
        int result = validateContextBusiness(context);

        if(result <= 0)
            throw new AuthException("Invalid Business Permissions");
    }


    /**
     * This method checks if the given context is valid for business
     * In other words, this method validates the session
     * @param context The auth context.
     */
    private int validateContextBusiness(AuthContextBusiness context) throws AuthException{

        try {
            List<Map<String, Object>> checkSession = sql.get("SELECT `business_id`, `session_token_business` " +
                            "FROM `tbl_session_business` " +
                            "WHERE `business_id` = ? AND `session_token_business` = ?",
                    context.business_id, context.session_token_business);

            return checkSession.size();

        } catch (SQLException e) {
            throw new AuthException("Invalid Context Business");
        }
    }

}
