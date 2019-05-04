package me.wapy.database.data_access;

import me.wapy.database.Database;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Antonio Zaitoun on 04/05/2019.
 */
public class DashboardAccessTest {

    DashboardAccess db;

    @Before
    public void setup(){
        Database.init();
        db = new DashboardAccess();
    }

    @Test
    public void getTraffic() throws Exception {
        Long val1 = db.getTraffic("1");

        assertTrue(val1 == 4);
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }
}