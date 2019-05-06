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

public class ProductAccessTest {

    ProductAccess db;

    Timestamp start = Timestamp.valueOf("2019-01-19 03:14:07");
    Timestamp end = Timestamp.valueOf("2019-03-19 03:14:07");

    @Before
    public void setUp() throws Exception {
        Database.init();
        db = new ProductAccess();
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void getAllReactionsPerProduct() throws Exception {
        String objectId = "1";
        List<Reaction> reactions = db.getAllReactionsPerProduct(null, objectId, start, end);
        for (Reaction reaction : reactions) {
            System.out.println(String.format("reaction: %s, value: %s", reaction.getReaction(), String.valueOf(reaction.getValue())));
        }
    }

    @Test
    public void getTotalViewsPerProduct() throws Exception {
        String objectId = "1";
        Long val = db.getTotalViewsPerProduct(null, objectId, start, end);
        System.out.println(val);
    }

    @Test
    public void getTotalLikesPerProduct() throws Exception {
        String objectId = "2";
        Long val = db.getTotalLikesPerProduct(null, objectId, start, end);
        System.out.println(val);
    }

    @Test
    public void getAllProductInWindow() throws Exception {
        String cameraId = "1";
        List<Product> val = db.getAllProductInWindow(null, cameraId, start, end);
        for (Product product : val) {
            System.out.println(String.format("product: %s", product.getObject_id()));
        }
    }

}