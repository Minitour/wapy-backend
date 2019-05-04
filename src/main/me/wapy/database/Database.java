package me.wapy.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import me.wapy.database.exception.AuthException;
import me.wapy.database.exception.DataAccessException;
import me.wapy.database.exception.MissingPermissionsException;
import me.wapy.database.permission.Permissions;
import me.wapy.utils.Config;

import java.io.Closeable;
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


    /**
     * Indicates if the database should strangle the thread.
     * If true the server will not start until a connection pool is established.
     */
    private static final boolean SHOULD_HANG = Config
            .main
            .get("db").getAsJsonObject()
            .get("should_strangle").getAsBoolean();

    /**
     * The primary data source for receiving connections.
     */
    private static HikariDataSource ds;

    /**
     * This is an internal private flag which indicates if the data source is injected or not. False by default.
     */
    private static boolean $is_injected = false;

    /**
     * $protect is a private internal flag meant to prevent the injection of data sources after the creation of the first instance.
     * Once an instance of Database is created this value will always be true.
     */
    private static boolean $protect = false;

    public static void init() {}

    /**
     * This method is used to inject a data source. Do not use unless you are in a testing environment
     * @param ds the data source you wish to inject.
     */
    public static void $test_inject_data_source(HikariDataSource ds) {

        // Injection can happen before creating a new instance.
        if($protect){
            System.err.println("CANNOT INJECT CONNECTION. CONNECTION ALREADY CREATED!");
            return;
        }

        // close data source if exists.
        if (Database.ds != null && !Database.ds.isClosed())
            Database.ds.close();

        // apply injection.
        Database.ds = ds;

        // flag as injected.
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

        String username = System.getenv("WAPY_JDBC_USERNAME");
        String password = System.getenv("WAPY_JDBC_PASSWORD");
        String jdbcUrl = System.getenv("WAPY_JDBC_URL");

        config.setPassword(password);
        config.setUsername(username);
        config.setJdbcUrl(jdbcUrl);

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

    protected ConnectionBox connection;

    protected final SQLHelper sql;

    /**
     * Convenience constructor.
     */
    protected Database() {
        this(null);
    }


    /**
     * Create a Data Access Object.
     * @param database Existing database object.
     */
    protected Database(Database database) {
        this(database,false);
    }

    private boolean isWeak = false;

    protected Database(Database database, boolean isWeak) {
        // set $protect to true to lock connection injection.
        $protect = true;
        this.isWeak = isWeak;

        // if database object is null create a new connection
        if (database == null) {
            initConnection();
        }
        // else use existing connection
        else {
            // make connection point to the existing connection.
            connection = database.connection;

            // retain connection reference count.
            if (!isWeak)
                connection.retain();
        }


        sql = new SQLHelper(connection.getConnection())
                .$_test_mode($is_injected);
    }


    /**
     * Create a new connection object.
     */
    private void initConnection(){
        try {
            // Create a new connection box using a connection fetched from the data source.
            connection = new ConnectionBox(ds.getConnection());
            System.out.println("Connection Created " + getClass().getSimpleName());
        } catch (SQLException e) {
            connection = null;
            e.printStackTrace();
        }
    }

    /**
     * Check if a server is up.
     * @param url Thr url.
     * @param port The port.
     *
     * @return true if the server was pinged false otherwise.
     */
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
        // decrease connection reference count by 1
        if(!isWeak) {
            connection.release();
        }

        // attempt to close the connection (if reference count is not 0 connection will not be closed).
        connection.close();
    }

    /****************************************       Permission Issue    ******************************/


    protected void isContextValidFor(AuthContext context,String permission) throws DataAccessException {
        validateContextIfNeeded(context);
        boolean result = Permissions.hasPermissionFor(permission, context);

        if(!result)
            throw new MissingPermissionsException();
    }


    /**
     * This method runs multiple queries and validates if the combination of a User ID and a Session token is valid.
     * In the case the session token or the user were not found an exception will be thrown.
     *
     * The way this method works is by receiving an "Unvalidated" AuthContext object and calling the validate method on them.
     *
     * This method is the ONLY method that shall call the `AuthContext::validate` method.
     * In the case the context has already been validate this method will automatically return.
     *
     * @param context The authentication context which contains a user_id and a session_token.
     * @throws AuthException In the case a token is not valid.
     */
    protected void validateContextIfNeeded(AuthContext context) throws AuthException{
        // if context is already valid, return and avoid redundant queries.
        if(context.isValid())
            return;

        try {
            // find session
            List<Map<String,Object>> checkSession = sql.get("SELECT `user_id`, `session_token`, `user_firebase_token`" +
                            "FROM `tbl_session` " +
                            "WHERE `user_id` = ? AND `session_token` = ?",
                    context.user_id, context.session_token);

            try {

                // find user roles
                List<Map<String,Object>> data = sql.get(
                        "SELECT `user_type`, `user_id_rank` "+
                                "FROM `tbl_user` "+
                                "WHERE `user_id` = ?;",
                        checkSession.get(0).get("user_id"));


                // extract rank, role and FCM token.
                Integer rank =  data.size() == 0 ? -1 : (Integer) data.get(0).get("user_id_rank");
                Integer user_type =  data.size() == 0 ? -1 : (Integer) data.get(0).get("user_type");
                String optionalToken = checkSession.get(0).containsKey("user_firebase_token") ? (String) checkSession.get(0).get("user_firebase_token") : null;

                // validate context.
                context.validate(rank, user_type, optionalToken);

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

    /**
     * This class is used to encapsulate the connection object that is created and is shared among Database Access Objects.
     * This class will "automatically" manage the reference count of the connection using the methods `retain` and `release`.
     */
    protected static class ConnectionBox implements Closeable {

        private static int auto_id = 0;
        final int connectionId = ++auto_id;

        /**
         * The reference count. Indicates the number of references currently accessing this connection.
         */
        private int reference_count;

        /**
         * Indicates if the object was released or not.
         */
        private boolean isReleased = false;

        /**
         * The JDBC connection.
         */
        final private Connection connection;

        /**
         * Creates a connection box with reference count set to 1.
         * @param connection The connection object.
         */
        ConnectionBox(Connection connection) {
            this.connection = connection;
            this.reference_count = 1;
            System.out.println("[Connection] Connection ID:" + connectionId);
        }

        /**
         * Increment the reference count by 1. Call this function when the connection is passed to share.
         */
        void retain(){
            this.reference_count += 1;
            System.out.println("[Connection] Retained Connection ID:" + connectionId);
        }

        /**
         * Decrement the reference count by 1. Call this when the connection is no longer used by the Access Object.
         * For example in the `close`.
         */
        void release() {
            this.reference_count -= 1;
            System.out.println("[Connection] Released Connection ID:" + connectionId);
        }

        /**
         * @return returns the connection if not already closed. else null.
         */
        public Connection getConnection() {
            if(!isReleased)
                return connection;
            return null;
        }

        @Override
        public void close() throws IOException {
            if(this.reference_count <= 0) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                isReleased = true;
                System.out.println("[Connection] Freed Connection ID:" + connectionId);
            }
        }
    }

}
