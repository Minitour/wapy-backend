package me.wapy.database.data_access;

import me.wapy.database.Database;
import me.wapy.model.Product;
import me.wapy.model.Reaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Antonio Zaitoun on 04/05/2019.
 */
public class DashboardAccessTest {

    DashboardAccess db;

//    Timestamp start = Timestamp.valueOf("2019-01-19 03:14:07");
//    Timestamp end = Timestamp.valueOf("2019-03-19 03:14:07");
//    @Before
//    public void setup(){
//        Database.init();
//        db = new DashboardAccess();
//    }
//
//
//    public void getTraffic() throws Exception {
//        Long val1 = db.getTraffic(null, start, end);
//        System.out.println(val1);
//        //assertTrue(val1 == 5);
//    }
//
//
//    public void getMostViewedProduct() throws Exception {
//        Product val = db.getMostViewedProduct(null, start, end);
//        if (val != null)
//            System.out.println(val.getObject_id());
//    }
//
//
//    public void getLeastViewedProduct() throws Exception {
//        Product val = db.getLeastViewedProduct(null, start, end);
//        if (val != null)
//            System.out.println(val.getObject_id());
//    }
//
//
//    public void getMostViewedProductReaction() throws Exception {
//        Product val = db.getMostViewedProductReaction(null, start, end);
//        if (val != null)
//            System.out.println(val.getObject_id());
//    }
//
//
//    public void getLeastViewedProductReaction() throws Exception {
//        Product val = db.getLeastViewedProductReaction(null, start, end);
//        if (val != null)
//            System.out.println(val.getObject_id());
//    }
//
//
//    public void getExposure() throws Exception {
//        Long val = db.getExposure(null, start, end);
//        if (val != null)
//            System.out.println(val);
//    }

//    @Test
//    public void getSmilesForProduct() throws Exception {
//        String object_id = "1";
//        String camera_id = "1";
//        Long val = db.getSmilesForProduct(start, end, object_id, camera_id);
//        System.out.println(val);
//    }

//    @Test
//    public void getReactionsPerProduct() throws Exception {
//        String object_id = "1";
//        String camera_id = "1";
//        List<Reaction> reactions = db.getReactionsPerProduct(camera_id, object_id, start, end);
//        for (Reaction reaction : reactions) {
//            System.out.println(String.format("key: %s , value: %s", String.valueOf(reaction.getReaction()), String.valueOf(reaction.getValue())));
//        }
//    }

//    @Test
//    public void getAllProductInWindow() throws Exception {
//        String cameraId = "1";
//        List<Product> products = db.getAllProductInWindow(cameraId, start, end);
//        for (Product product : products) {
//            System.out.println(String.format("product: %s ", product.getObject_id()));
//        }
//    }

//    @After
//    public void tearDown() throws Exception {
//        db.close();
//    }
}