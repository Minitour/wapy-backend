package me.wapy.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.wapy.database.data_access.BoxAccess;
import me.wapy.model.Product;
import me.wapy.utils.JSONResponse;
import me.wapy.utils.RESTRoute;
import spark.Request;
import spark.Response;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
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

        // get the number of days for time frame
        Integer numberOfDays = body.has("numberOfDays") ? body.get("numberOfDays").getAsInt() : 1;

        // get the to timestamp
        String toTimeString = body.has("toTime") ? body.get("toTime").getAsString() : "";
        Timestamp toTime = !toTimeString.equals("") ? Timestamp.valueOf(toTimeString) : null;

        Calendar cal = Calendar.getInstance();
        cal.setTime(toTime);
        cal.add(Calendar.DATE, numberOfDays*-1);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fromTimeString = dateFormat.format(cal.getTime());
        Timestamp fromTime = Timestamp.valueOf(fromTimeString);

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

            // construct the columns
            JsonArray columns = new JsonArray();
            columns.add("Product");

            // construct the values for the columns
            JsonArray values = new JsonArray();

            // construct the column values
            for (Product product : productList) {
                JsonArray columnValues = new JsonArray();

                // construct the value
                columnValues.add(product.getObject_id());

                // add to the values list of tha table
                values.add(columnValues);
            }

            values = sortJsonArray(values);

            // get the table as json
            JsonObject jsonProducts = getTableAsJson("Products", "Products", columns, values);

            // adding to the json builder
            tablesObject.add(jsonProducts);


            // ---------------------------------------------------------------//
            //  getting most viewed product in window
            // ---------------------------------------------------------------//
            Product product = access.getMostViewedProductInWindow(owner_uid, camera_id, fromTime, toTime);

            Long productValue = 0L;
            if (product != null)
                productValue = product.getValue();

            JsonObject productObject = getProductAsJson("Most Viewed Product", null, productValue, "star", "#feca57", "white", 0L, true, "", false);

            statsObject.add(productObject);

            // ---------------------------------------------------------------//
            //  getting least viewed product in window
            // ---------------------------------------------------------------//
            Product product1 = access.getLeastViewedProductInWindow(owner_uid, camera_id, fromTime, toTime);

            Long product1Value = 0L;
            if (product1 != null)
                product1Value = product1.getValue();

            JsonObject product1Object = getProductAsJson("Least Viewed Product", null, product1Value, "heart-broken", "#576574", "white", 0L, true, "", false);

            statsObject.add(product1Object);
            /*
                response will look like:
                {
                    "box": {
                        "tables": [
                            {
                                "title": "Products",
                                "header": "Product",
                                "columns": [
                                    "Product"
                                ],
                                "values": [
                                    [
                                        "1",
                                        "2",
                                        "3"
                                    ]
                                ]
                            }
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

    /**
     * Return the values as a json object
     * @param title
     * @param strValue
     * @param longValue
     * @param icon
     * @param iconBgColor
     * @param iconColor
     * @param diffValue
     * @param isPositive
     * @param footerText
     * @param showFooter
     * @return
     */
    private JsonObject getProductAsJson(String title, String strValue, Long longValue, String icon, String iconBgColor, String iconColor, Long diffValue, boolean isPositive, String footerText, boolean showFooter) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("title", title);

        if (strValue != null)
            jsonObject.addProperty("value", strValue);
        else
            jsonObject.addProperty("value", longValue);

        jsonObject.addProperty("icon", icon);
        jsonObject.addProperty("iconBgColor", iconBgColor);
        jsonObject.addProperty("iconColor", iconColor);
        jsonObject.addProperty("diffValue", diffValue);
        jsonObject.addProperty("isPositive", isPositive);
        jsonObject.addProperty("footerText", footerText);
        jsonObject.addProperty("showFooter", showFooter);
        return jsonObject;
    }

    private JsonObject getTableAsJson(String title, String header, JsonArray columns, JsonArray values) {
        JsonObject tempProduct = new JsonObject();
        tempProduct.addProperty("title", "Products");
        tempProduct.addProperty("header", "Product");
        tempProduct.add("columns", columns);
        tempProduct.add("values", values);

        return tempProduct;
    }

    private JsonArray sortJsonArray(JsonArray jsonArray) {
        // transform the json array into list
        List<JsonObject> values = new ArrayList<>();

        for (JsonElement columnsValue : jsonArray) {
            JsonObject pair = new JsonObject();
            pair.addProperty("key", columnsValue.getAsJsonArray().get(0).getAsString());
            pair.addProperty("value", columnsValue.getAsJsonArray().get(1).getAsInt());
            values.add(pair);
        }

        values.sort(new Comparator<JsonObject>() {
            @Override
            public int compare(JsonObject o1, JsonObject o2) {
                if (o1.get("value").getAsString().equals(o2.get("value").getAsString())) {
                    return -1;
                } else if (o1.get("value").getAsInt() == o2.get("value").getAsInt()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        // transform back to json array
        JsonArray newValues = new JsonArray();
        for (JsonObject value : values) {
            JsonArray innerArray = new JsonArray();
            innerArray.add(value.get("key").getAsString());
            innerArray.add(value.get("value").getAsInt());
            newValues.add(innerArray);
        }

        return newValues;
    }
}
