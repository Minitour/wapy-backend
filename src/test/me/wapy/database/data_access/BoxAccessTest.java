package me.wapy.database.data_access;

import me.wapy.database.AuthContext;
import me.wapy.database.Database;
import me.wapy.model.Product;
import me.wapy.model.Reaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.*;

public class BoxAccessTest {

    BoxAccess access;
    Timestamp start = Timestamp.valueOf("2019-01-19 03:14:07");
    Timestamp end = Timestamp.valueOf("2019-03-19 03:14:07");

//    @Before
//    public void setUp() throws Exception {
//        Database.init();
//        access = new BoxAccess();
//    }

//    @After
//    public void tearDown() throws Exception {
//        access.close();
//    }

//    @Test
//    public void getAllProductsInWindow() throws Exception {
//        String camera_id = "1";
//        List<Product> productList = access.getAllProductsInWindow(null, camera_id, start, end);
//        for (Product product : productList) {
//            System.out.println(String.format("product: %s", product.getObject_id()));
//        }
//    }
//
//    @Test
//    public void getMostViewedProductInWindow() throws Exception {
//        String camera_id = "1";
//        Product product = access.getMostViewedProductInWindow(null, camera_id, start, end);
//        System.out.println(product.getObject_id());
//    }
//
//    @Test
//    public void getLeastViewedProductInWindow() throws Exception {
//        String camera_id = "1";
//        Product product = access.getLeastViewedProductInWindow(null ,camera_id, start, end);
//        System.out.println(product.getObject_id());
//    }
//
//    @Test
//    public void getAllReactionsPerProductPerBox() throws Exception {
//        String camera_id = "1";
//        String object_id = "1";
//        List<Reaction> reactions = access.getAllReactionsPerProductPerBox(null,object_id, camera_id, start, end);
//        for (Reaction reaction : reactions) {
//            System.out.println(String.format("reaction: %s, value: %s", reaction.getReaction(), reaction.getValue()));
//        }
//    }

}