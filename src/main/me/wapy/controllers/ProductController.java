package me.wapy.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.wapy.database.data_access.DashboardAccess;
import me.wapy.database.data_access.ProductAccess;
import me.wapy.model.Product;
import me.wapy.model.Reaction;
import me.wapy.utils.JSONResponse;
import me.wapy.utils.RESTRoute;
import spark.Request;
import spark.Response;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ProductController implements RESTRoute {

    private static final Gson gson = new Gson();

    @Override
    public Object handle(Request request, Response response, JsonObject body) throws Exception {

        /*
        body structure:
        {
            "camera_id" : "1",
            "object_id": "1",
            "fromTime": "2019-04-12 12:34:12",
            "toTime": "2019-04-12 12:45:12"
        }

        ## this is the minimal info we need to send the controller results

        ## from the camera_id we will get all the product in window ->
                can get total views per product
                and
                can get total likes per product
                and
                can give us the smiles for product
                and
                can give us the reactions for product
         */

        // get the response as json object for extraction
        JsonObject jsonObject = gson.fromJson(body, JsonObject.class);

        // get the camera id
        String camera_id = jsonObject.get("camera_id").getAsString();

        // get the camera id
        String object_id = jsonObject.get("object_id").getAsString();

        // get the from timestamp
        String fromTimeString = jsonObject.get("fromTime").getAsString();
        Timestamp fromTime = Timestamp.valueOf(fromTimeString);

        // get the to timestamp
        String toTimeString = jsonObject.get("toTime").getAsString();
        Timestamp toTime = Timestamp.valueOf(toTimeString);

        JsonObject jsonBuilder = new JsonObject();

        // ---------------------------------------------------------------//
        //  we will get all products in window
        //  if we dont have any products we will not continue to the next functions
        // ---------------------------------------------------------------//


        try(ProductAccess access = new ProductAccess()) {
            List<Product> productList;

            // try to get the product list in window
            try(DashboardAccess dAccess = new DashboardAccess(access)) {
                productList = dAccess.getAllProductInWindow(camera_id, fromTime, toTime);
            }

            // return Fail in case there are no products in window
            if (productList.isEmpty())
                return JSONResponse.FAILURE().message("No products in window");

            // ---------------------------------------------------------------//
            //  get all views per product
            // ---------------------------------------------------------------//
            Long views = access.getTotalViewsPerProduct(camera_id, object_id, fromTime, toTime);

            jsonBuilder.addProperty("views", views);


            // ---------------------------------------------------------------//
            //  get total likes per product
            // ---------------------------------------------------------------//
            Long likes = access.getTotalLikesPerProduct(camera_id, object_id, fromTime, toTime);

            jsonBuilder.addProperty("likes", likes);

            // ---------------------------------------------------------------//
            //  get total smiles for product
            // ---------------------------------------------------------------//

            Long smiles = access.getSmilesForProduct(fromTime, toTime, object_id, camera_id);

            jsonBuilder.addProperty("smiles", smiles);

        }

        // construct the json to return
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.add(object_id, jsonBuilder);
        return JSONResponse.SUCCESS().data(jsonResponse);

        /*
        response will look like this:
            "object_id_1": {
                "views": "value",
                "likes": "value"
            }
        */
    }
}
