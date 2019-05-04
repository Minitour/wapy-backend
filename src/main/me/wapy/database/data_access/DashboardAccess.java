package me.wapy.database.data_access;

import me.wapy.database.Database;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by Antonio Zaitoun on 04/05/2019.
 */
public class DashboardAccess extends Database {

    public DashboardAccess() {
    }

    public DashboardAccess(Database database) {
        super(database);
    }

    public DashboardAccess(Database database, boolean isWeak) {
        super(database, isWeak);
    }


    /**
     * Returns the traffic for a given store.
     *
     * TODO: @Tomer add time parameters.
     *
     * @param storeId
     * @return
     * @throws SQLException
     */
    public Long getTraffic(String storeId) throws SQLException {
        List<Map<String,Object>> rs = sql.get(
                "select count(*) as value from objects_table where store_id = ?",
                storeId
        );

        if (rs.isEmpty())
            return 0L;

        return (Long) rs.get(0).get("value");
    }
}
