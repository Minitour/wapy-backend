package me.wapy.database.data_access;

import me.wapy.database.Database;
import me.wapy.model.Product;

import java.sql.Timestamp;
import java.util.List;

public class BoxAccess extends Database {

    public BoxAccess() {

    }
    public BoxAccess(Database database, boolean isWeak) {
        super(database, isWeak);
    }

    public BoxAccess(Database database) {
        super(database);
    }

    public List<Product> getAllProductsInWindow(String storeId, String cameraId, Timestamp fromTime, Timestamp toTime) {
        String query = "";
        return null;
    }

}
