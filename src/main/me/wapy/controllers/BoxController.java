package me.wapy.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.wapy.database.data_access.BoxAccess;
import me.wapy.model.Product;
import me.wapy.utils.JSONResponse;
import me.wapy.utils.RESTRoute;
import spark.Request;
import spark.Response;

import java.sql.Timestamp;
import java.util.List;

public class BoxController implements RESTRoute {
    @Override
    public Object handle(Request request, Response response, JsonObject body) throws Exception {
        /*
            body structure:
            {
                "owner_uid" : "1",
                "fromTime": "2019-04-12 12:34:12",
                "toTime": "2019-04-12 12:45:12"
            }

            ## this is the minimal info we need to send the controller results

            ## from the owner_uid we will get all the product in window ->
                    can give us total time looking at all products in this specific window

         */

        // get the owner_uid
        String owner_uid = body.has("owner_uid") ? body.get("owner_uid").getAsString() : "";

        // get the camera id
        String camera_id = request.params(":id") != null ? request.params(":id") : "";

        // get the from timestamp
        String fromTimeString = body.has("fromTime") ? body.get("fromTime").getAsString() : "";
        Timestamp fromTime = !fromTimeString.equals("") ? Timestamp.valueOf(fromTimeString) : null;

        // get the to timestamp
        String toTimeString = body.has("toTime") ? body.get("toTime").getAsString() : "";
        Timestamp toTime = !toTimeString.equals("") ? Timestamp.valueOf(toTimeString) : null;

        if (owner_uid.equals("") || camera_id.equals("") || fromTime == null || toTime == null)
            return JSONResponse.FAILURE().message("missing parameters");

        JsonObject jsonBuilder = new JsonObject();

        try(BoxAccess access = new BoxAccess()) {

            // ---------------------------------------------------------------//
            //  getting all products in window (specific camera)
            // ---------------------------------------------------------------//
            List<Product> productList = access.getAllProductsInWindow(owner_uid, camera_id, fromTime, toTime);

            JsonArray jsonProducts = new JsonArray();
            for (Product product : productList) {
                jsonProducts.add(product.getObject_id());
            }

            // adding to the json builder
            jsonBuilder.add("products_in_window", jsonProducts);


            // ---------------------------------------------------------------//
            //  getting most viewed product in window
            // ---------------------------------------------------------------//
            Product product = access.getMostViewedProductInWindow(owner_uid, fromTime, toTime);

            if (product != null) {
                JsonObject jsonProduct = new JsonObject();
                jsonProduct.addProperty("product_name", product.getObject_id());
                jsonProduct.addProperty("value", product.getValue());
                jsonBuilder.add("most_viewed_product",jsonProduct);
            }


            // ---------------------------------------------------------------//
            //  getting least viewed product in window
            // ---------------------------------------------------------------//
            Product product1 = access.getLeastViewedProductInWindow(owner_uid, fromTime, toTime);

            if (product1 != null) {
                JsonObject jsonProduct = new JsonObject();
                jsonProduct.addProperty("product_name", product1.getObject_id());
                jsonProduct.addProperty("value", product1.getValue());
                jsonBuilder.add("least_viewed_product",jsonProduct);
            }
            /*
                response will look like:
                {
                    "box": {
                        "products_in_window": [
                            "product1",
                            "product2"
                        ],
                        "most_viewed_product": {
                            "product_name": "",
                            "value": ""
                        },
                        "least_viewed_product": {
                            "product_name": "",
                            "value": ""
                        }
                    }
                }
             */

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.add("box", jsonBuilder);

        }

        return null;
    }
}
