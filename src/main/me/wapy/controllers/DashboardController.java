package me.wapy.controllers;

import com.google.api.client.json.Json;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class DashboardController implements RESTRoute {

    private static final Gson gson = new Gson();

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
                can give us the smiles for product
                and
                can give us the reactions for product
         */

        // get the camera id
        String owner_uid = body.has("owner_uid") ? body.get("owner_uid").getAsString() : "";

        // get the from timestamp
        String fromTimeString = body.has("fromTime") ? body.get("fromTime").getAsString() : "";
        Timestamp fromTime = !fromTimeString.equals("") ? Timestamp.valueOf(fromTimeString) : null;

        // get the to timestamp
        String toTimeString = body.has("toTime") ? body.get("toTime").getAsString() : "";
        Timestamp toTime = !toTimeString.equals("") ? Timestamp.valueOf(toTimeString) : null;

        if (owner_uid.equals("") || fromTime == null || toTime == null)
            return JSONResponse.FAILURE().message("missing parameters");

        // init the json builder to wrap the three objects (stats, graphs, tables)
        JsonObject jsonBuilder = new JsonObject();

        // init the three arrays for stats, graphs, tables
        JsonArray statsObject = new JsonArray();
        JsonArray graphsObject = new JsonArray();
        JsonArray tablesObject = new JsonArray();

        try(DashboardAccess access = new DashboardAccess()) {

            // ---------------------------------------------------------------//
            //  traffic
            // ---------------------------------------------------------------//

            // getting the traffic
            Long counter = access.getTraffic(fromTime, toTime);

            JsonObject trafficObject = getProductAsJson("Traffic", null, null, counter, "#172b4d", "#172b4d", "#172b4d", 0L, true, "", false);

            // adding the traffic number to the stats array
            // will get 0 or above - no nulls
            statsObject.add(trafficObject);


            // ---------------------------------------------------------------//
            //  most viewed product
            // ---------------------------------------------------------------//

            //getting the most viewed product (according to the objects_table)
            Product most_viewed_product = access.getMostViewedProduct(fromTime, toTime);

            Long most_viewed_product_value = 0L;
            if (most_viewed_product != null)
                most_viewed_product_value = most_viewed_product.getValue();

            String pId = "";
            if (most_viewed_product != null)
                pId = most_viewed_product.getObject_id();
            JsonObject mostViewedProductObject = getProductAsJson("Most Viewed Product: " + pId, pId, null, most_viewed_product_value, "#172b4d", "#172b4d", "#172b4d", 0L, true, "", false);

            // append to the stats array
            statsObject.add(mostViewedProductObject);

            // ---------------------------------------------------------------//
            //  least viewed product
            // ---------------------------------------------------------------//

            // getting the least viewed product (according to objects_table)
            Product least_viewed_product = access.getLeastViewedProduct(fromTime, toTime);

            Long least_viewed_product_value = 0L;
            // product can be null -> will not add to the json response
            if (least_viewed_product != null)
                least_viewed_product_value = least_viewed_product.getValue();

            if (least_viewed_product != null)
                pId = least_viewed_product.getObject_id();
            else
                pId = "";
            JsonObject leastViewedProductObject = getProductAsJson("Least Viewed Product: " + pId, pId,null, least_viewed_product_value, "#172b4d", "#172b4d", "#172b4d", 0L, true, "", false);

            // append to the stats array
            statsObject.add(leastViewedProductObject);

            // ---------------------------------------------------------------//
            //  most viewed product reaction
            // ---------------------------------------------------------------//

            // getting the most viewed product (according to images_table)
            Product most_viewed_reaction_product = access.getMostViewedProductReaction(fromTime, toTime);

            Long most_viewed_reaction_product_value = 0L;
            // product can be null -> will not add to the json response
            if (most_viewed_reaction_product != null)
                most_viewed_reaction_product_value = most_viewed_reaction_product.getValue();

            if (most_viewed_reaction_product != null)
                pId = most_viewed_reaction_product.getObject_id();
            else
                pId = "";
            JsonObject mostViewedReactionProductObject = getProductAsJson("Most Viewed Product Reaction: " + pId,pId, null, most_viewed_reaction_product_value, "#172b4d", "#172b4d", "#172b4d", 0L, true, "", false);

            // append to the stats array
            statsObject.add(mostViewedReactionProductObject);

            // ---------------------------------------------------------------//
            //  least viewed product reaction
            // ---------------------------------------------------------------//

            // getting the least viewed product (according to images_table)
            Product least_viewed_reaction_product = access.getLeastViewedProductReaction(fromTime, toTime);

            Long least_viewed_reaction_product_value = 0L;
            if (least_viewed_reaction_product != null)
                least_viewed_reaction_product_value = least_viewed_reaction_product.getValue();

            if (least_viewed_reaction_product != null)
                pId = least_viewed_reaction_product.getObject_id();
            else
                pId = "";
            JsonObject leastViewedReactionProductObject = getProductAsJson("Least Viewed Product Reaction: " + pId,pId, null, least_viewed_reaction_product_value, "#172b4d", "#172b4d", "#172b4d", 0L, true, "", false);

            // append to the stats array
            statsObject.add(leastViewedReactionProductObject);


            // ---------------------------------------------------------------//
            //  exposure
            // ---------------------------------------------------------------//
            Long exposure = access.getExposure(fromTime, toTime);
            List<Long> exposures = new ArrayList<>();
            exposures.add(exposure);

            JsonObject exposureObject = getInitGraphObject("line", "Exposure", false, "Exposure");

            // get the data for the graph
            exposureObject = getGraphData(exposureObject, exposures, null);

            // add the graph to the json response
            graphsObject.add(exposureObject);


            // ---------------------------------------------------------------//
            // all products in window
            /*
            {
                "products": [
                    {"product_1": views},
                    {"product_2": views}
                ]
            }
             */
            // we are getting the products for window and will use later also
            // ---------------------------------------------------------------//
            List<Product> productsList = access.getAllProductInWindow(owner_uid, fromTime, toTime);

            // checking we have a list to append
            if (!productsList.isEmpty()) {

                // construct the columns for the table
                JsonArray columns = new JsonArray();
                columns.add("Product");
                columns.add("Views");

                // init the columns values
                JsonArray productValues = new JsonArray();
                JsonArray viewsValues = new JsonArray();

                try(ProductAccess pAccess = new ProductAccess(access)) {
                    // populate the columns values
                    for (Product product : productsList) {

                        // get the product id
                        if (product.getObject_id() != null)
                            pId = product.getObject_id();
                        else
                            pId = "";
                        productValues.add(pId);

                        // get the views value
                        Long views = pAccess.getTotalViewsPerProduct(owner_uid, pId, fromTime, toTime);
                        viewsValues.add(views);
                    }
                }

                // add the columns into one array
                JsonArray values = new JsonArray();
                values.add(productValues);
                values.add(viewsValues);

                // get the table as a json object
                JsonObject productListObject = getTableAsJson("Products", "Views", columns, values);

                tablesObject.add(productListObject);
            }


            // ---------------------------------------------------------------//
            // reactions per owner
            /*
            {
                "reactions": {
                    "sad": value,
                    "angry": value
                 }

            }
             */
            // ---------------------------------------------------------------//

            // getting all reactions for all products
            List<Reaction> reactions = access.getReactionSummary(owner_uid, fromTime, toTime);

            // get the init json object with the title, header and other
            JsonObject reactionsObject = getInitGraphObject("bar", "Reactions", false, "Reactions");

            // add the data into the json object
            reactionsObject = getGraphData(reactionsObject, null, reactions);

            // append to the graphs
            graphsObject.add(reactionsObject);


            // adding all tables to the json response
            jsonBuilder.add("stats", statsObject);
            jsonBuilder.add("graphs", graphsObject);
            jsonBuilder.add("tables", tablesObject);

            // adding the json builder for final json object that contains the dashboard data
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.add("dashboard", jsonBuilder);

            // return the json response with all the dashboard data we need
            return JSONResponse.SUCCESS().data(jsonResponse);

            /*
            response will be like this:
            "dashboard": {
                "stats": [
                    {
                        "title": "Traffic",
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
                    },
                    {
                        "title": "Most Viewed Product Reaction",
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
                        "title": "Least Viewed Product Reaction",
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
                "graphs":[
                    {
                        "reactions": [
                            "sad": value,
                            "angry": value
                         ]
                    }
                ],
                "tables":[]
            }
             */

        }catch (Exception e) {
            e.printStackTrace();
        }
        return JSONResponse.FAILURE().message("No Traffic");


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
    private JsonObject getProductAsJson(String title, String productId, String strValue, Long longValue, String icon, String iconBgColor, String iconColor, Long diffValue, boolean isPositive, String footerText, boolean showFooter) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("title", title);

        jsonObject.addProperty("productId", productId);

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
        tempProduct.addProperty("title", title);
        tempProduct.addProperty("header", header);
        tempProduct.add("columns", columns);
        tempProduct.add("values", values);

        return tempProduct;
    }

    private JsonObject getInitGraphObject(String type, String name, boolean showLegend, String header) {

        JsonObject object = new JsonObject();
        object.addProperty("type", type);
        object.addProperty("name", name);
        object.addProperty("showLegend", showLegend);
        object.addProperty("header", header);

        return object;
    }

    private JsonObject getGraphData(JsonObject initObject, List<Long> longValues, List<Reaction> reactionValues){
        JsonObject data = new JsonObject();
        switch (initObject.get("type").getAsString()) {
            case "line": {
                data = getLineGraphData(longValues);
                break;
            }
            case "bar": {
                data = getBarGraphData(reactionValues);
                break;
            }
            case "radar": {
                data = getRadarGraphData();
                break;
            }
            case "pie": {
                data = getPieGraphData();
                break;
            }
        }
        initObject.add("data", data);
        return initObject;
    }


    /*
    data: [
       {
        x: 10,
        y: 20
       },
       {
        x: 15,
        y: 10
       }
    ]
     */
    private JsonObject getLineGraphData(List<Long> values) {

        JsonArray jsonArray = new JsonArray();

        for (Long value : values) {

            JsonObject xY = new JsonObject();

            // get the date
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String dateString = dateFormat.format(date);

            // add the values to fields
            xY.addProperty("x", dateString);
            xY.addProperty("y", value);

            jsonArray.add(xY);
        }

        return getDataSetArray(jsonArray);
    }

    private JsonObject getBarGraphData(List<Reaction> reactions) {
        JsonArray jsonArray = new JsonArray();
        for (Reaction reaction : reactions) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("x", reaction.getReaction());
            jsonObject.addProperty("y", reaction.getValue());

            jsonArray.add(jsonObject);
        }

        return getDataSetArray(jsonArray);
    }

    private JsonObject getRadarGraphData() {
        return new JsonObject();
    }

    private JsonObject getPieGraphData() {
        return new JsonObject();
    }

    private JsonObject getDataSetArray(JsonArray arr) {
        JsonObject wrapper = new JsonObject();
        JsonArray dataset = new JsonArray();
        JsonObject data = new JsonObject();

        data.add("data", arr);
        dataset.add(data);

        wrapper.add("dataset", dataset);
        return wrapper;
    }

}

