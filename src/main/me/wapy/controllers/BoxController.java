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

        // init the three arrays for stats, graphs, tables
        JsonArray statsObject = new JsonArray();
        JsonArray graphsObject = new JsonArray();
        JsonArray tablesObject = new JsonArray();

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
            tablesObject.add(jsonProducts);


            // ---------------------------------------------------------------//
            //  getting most viewed product in window
            // ---------------------------------------------------------------//
            Product product = access.getMostViewedProductInWindow(owner_uid, fromTime, toTime);

            JsonObject productObject = new JsonObject();
            Long productValue = 0L;
            if (product != null)
                productValue = product.getValue();

            productObject.addProperty("title", "Most Viewed Product");
            productObject.addProperty("value", productValue);
            productObject.addProperty("icon", "#172b4d");
            productObject.addProperty("iconBgColor", "#172b4d");
            productObject.addProperty("iconColor", "#172b4d");
            productObject.addProperty("diffValue", "");
            productObject.addProperty("isPositive", true);
            productObject.addProperty("footerText", "");
            productObject.addProperty("showFooter", false);

            statsObject.add(productObject);

            // ---------------------------------------------------------------//
            //  getting least viewed product in window
            // ---------------------------------------------------------------//
            Product product1 = access.getLeastViewedProductInWindow(owner_uid, fromTime, toTime);

            JsonObject product1Object = new JsonObject();
            Long product1Value = 0L;
            if (product1 != null)
                product1Value = product1.getValue();

            product1Object.addProperty("title", "Least Viewed Product");
            product1Object.addProperty("value", product1Value);
            product1Object.addProperty("icon", "#172b4d");
            product1Object.addProperty("iconBgColor", "#172b4d");
            product1Object.addProperty("iconColor", "#172b4d");
            product1Object.addProperty("diffValue", "");
            product1Object.addProperty("isPositive", true);
            product1Object.addProperty("footerText", "");
            product1Object.addProperty("showFooter", false);

            statsObject.add(product1Object);
            /*
                response will look like:
                {
                    "box": {
                        "tables":[
                            [
                                "product1",
                                "product2"
                            ]
                        ],
                        "stats": [
                            {
                                "title": "Most Viewed Product",
                                "value": "string",
                                "icon": "string",
                                "iconBgColor": "string",
                                "iconColor": "string",
                                "diffValue": "",
                                "isPositive": true,
                                "footerText": "",
                                "showFooter": false
                            },
                            {
                                "title": "Least Viewed Product",
                                "value": "string",
                                "icon": "string",
                                "iconBgColor": "string",
                                "iconColor": "string",
                                "diffValue": "",
                                "isPositive": true,
                                "footerText": "",
                                "showFooter": false
                            }
                        ],
                        "graphs": []
                    }
                }
             */

            jsonBuilder.add("stats", statsObject);
            jsonBuilder.add("graphs", graphsObject);
            jsonBuilder.add("tables", tablesObject);

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.add("box", jsonBuilder);

            return JSONResponse.SUCCESS().data(jsonResponse);

        }
    }
}
