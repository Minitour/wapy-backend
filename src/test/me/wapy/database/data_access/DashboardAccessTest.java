package me.wapy.database.data_access;

import me.wapy.database.Database;
import me.wapy.model.Product;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

/**
 * Created by Antonio Zaitoun on 04/05/2019.
 */
public class DashboardAccessTest {

    DashboardAccess db;

    Timestamp start = new Timestamp(20190103203045L);
    Timestamp end = new Timestamp(20190103203046L);
    @Before
    public void setup(){
        Database.init();
        db = new DashboardAccess();
    }

    @Test
    public void getTraffic() throws Exception {
        Long val1 = db.getTraffic("1", start, end);
        System.out.println(val1);
        //assertTrue(val1 == 5);
    }

    @Test
    public void getMostViewedProduct() throws Exception {
        Product val = db.getMostViewedProduct("1", start, end);
        if (val != null)
            System.out.println(val.getObject_id());
    }

    @Test
    public void getLeastViewedProduct() throws Exception {
        Product val = db.getLeastViewedProduct("1", start, end);
        if (val != null)
            System.out.println(val.getObject_id());
    }

    @Test
    public void getMostViewedProductReaction() throws Exception {
        Product val = db.getMostViewedProductReaction("1", start, end);
        if (val != null)
            System.out.println(val.getObject_id());
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }
}