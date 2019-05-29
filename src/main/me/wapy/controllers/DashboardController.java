package me.wapy.controllers;

import com.google.api.client.json.Json;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.util.Pair;
import me.wapy.database.data_access.DashboardAccess;
import me.wapy.database.data_access.ProductAccess;
import me.wapy.model.Product;
import me.wapy.model.Reaction;
import me.wapy.utils.JSONResponse;
import me.wapy.utils.RESTRoute;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.lang.reflect.Type;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

public class DashboardController implements RESTRoute {

    private static final Gson gson = new Gson();

    @Override
    public Object handle(Request request, Response response, JsonObject body) throws Exception {

        /*
        body structure:
        {
            "owner_uid" : "1",
            "toTime": "2019-04-12 12:34:12",
            "numberOfDays": 7
        }

        ## this is the minimal info we need to send the controller results

        ## from the owner_uid we will get all the product in window ->
                can give us the smiles for product
                and
                can give us the reactions for product
         */

        // get the camera id
        String owner_uid = body.has("owner_uid") ? body.get("owner_uid").getAsString() : "";

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
            JsonObject mostViewedProductObject = getProductAsJson("Most Viewed Product: " + pId, pId, null, most_viewed_product_value, "star", "#feca57", "white", 0L, true, "", false);

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
            JsonObject leastViewedProductObject = getProductAsJson("Least Viewed Product: " + pId, pId,null, least_viewed_product_value, "heart-broken", "#576574", "white", 0L, true, "", false);

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
            JsonObject mostViewedReactionProductObject = getProductAsJson("Most Viewed Product Reaction: " + pId,pId, null, most_viewed_reaction_product_value, "fire", "#f39c12", "white", 0L, true, "", false);

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
            JsonObject leastViewedReactionProductObject = getProductAsJson("Least Viewed Product Reaction: " + pId,pId, null, least_viewed_reaction_product_value, "meh", "#9b59b6", "white", 0L, true, "", false);

            // append to the stats array
            statsObject.add(leastViewedReactionProductObject);


            // ---------------------------------------------------------------//
            //  exposure
            // ---------------------------------------------------------------//

            List<Long> exposures = new ArrayList<>();


            JsonObject exposureObject = getInitGraphObject("line", "Exposure", false, "Exposure");

            JsonArray labels = generateLineChartLabels(fromTime, toTime, numberOfDays);

            for (int i=1; i<labels.size(); i++) {
                String stringFromtime = formatDate(labels.get(i-1).getAsString());
                String stringToTime = formatDate(labels.get(i).getAsString());
                Timestamp tempFromTime = Timestamp.valueOf(stringFromtime);
                Timestamp tempToTime = Timestamp.valueOf(stringToTime);
                Long exposure = access.getExposure(tempFromTime, tempToTime);
                exposures.add(exposure);
            }

            // get the data for the graph
            exposureObject = getGraphData(exposureObject, exposures, null,"Exposure", fromTime, toTime, numberOfDays);

            String titleText = "Exposure over time";

            exposureObject = getOptionsForGraph(exposureObject, titleText, false);

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

                // get the product list as json array
                JsonArray columnsValues = getProductListAsJsonArray(access, productsList, owner_uid, fromTime, toTime);

                // sort the array by views
                columnsValues = sortJsonArray(columnsValues);

                // get the table as a json object
                JsonObject productListObject = getTableAsJson("Products", "Views", columns, columnsValues);

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
            reactionsObject = getGraphData(reactionsObject, null, reactions, "Reactions", fromTime, toTime, numberOfDays);

            String titleTextReactions = "Reactions Bar";

            reactionsObject = getOptionsForGraph(reactionsObject, titleTextReactions, false);

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
        return JSONResponse.FAILURE().message("Error");


    }

    private JsonArray getProductListAsJsonArray(DashboardAccess access, List<Product> productsList, String owner_uid, Timestamp fromTime, Timestamp toTime) {
        String pId;
        JsonArray columnsValues = new JsonArray();

        try(ProductAccess pAccess = new ProductAccess(access)) {
            for (Product product : productsList) {
                JsonArray values = new JsonArray();
                // get the product id
                if (product.getObject_id() != null)
                    pId = product.getObject_id();
                else
                    pId = "";
                values.add(pId);

                // get the views value
                Long views = pAccess.getTotalViewsPerProduct(owner_uid, pId, fromTime, toTime);
                values.add(views);

                columnsValues.add(values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return columnsValues;
    }


    private JsonArray sortJsonArray(JsonArray columnsValues) {
        // transform the json array into list
        List<JsonObject> values = new ArrayList<>();
        //List<Pair<String, Integer>> values = new ArrayList<>();

        for (JsonElement columnsValue : columnsValues) {
            JsonObject pair = new JsonObject();
            pair.addProperty("key", columnsValue.getAsJsonArray().get(0).getAsString());
            pair.addProperty("value", columnsValue.getAsJsonArray().get(1).getAsInt());
            //Pair<String, Integer> pair = new Pair<>(columnsValue.getAsJsonArray().get(0).getAsString(), columnsValue.getAsJsonArray().get(1).getAsInt());
            values.add(pair);
        }

        values.sort(new Comparator<JsonObject>() {
            @Override
            public int compare(JsonObject o1, JsonObject o2) {
                if (o1.get("value").getAsInt() > o2.get("value").getAsInt()) {
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

    private JsonObject getGraphData(JsonObject initObject, List<Long> longValues, List<Reaction> reactionValues, String innerLabel, Timestamp fromTime, Timestamp toTime, Integer numberOfDays){
        JsonObject data = new JsonObject();
        JsonArray labels = new JsonArray();
        switch (initObject.get("type").getAsString()) {
            case "line": {
                data = getLineGraphData(longValues, innerLabel, new JsonArray());
                labels = generateLineChartLabels(fromTime, toTime, numberOfDays);
                labels.remove(0);
                data.add("labels", labels);
                break;
            }
            case "bar": {
                JsonArray colors = generateLineColors();
                data = getBarGraphData(reactionValues, innerLabel, colors);
                labels = generateBarChartLabels(reactionValues);
                data.add("labels", labels);

                break;
            }
            case "radar": {
                data = getRadarGraphData(reactionValues);
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
    private JsonObject getLineGraphData(List<Long> values, String innerLabel, JsonArray BgColors) {

        JsonArray jsonArray = new JsonArray();
        for (Long value : values) {

            jsonArray.add(value);
        }

        return getDataSetObject(jsonArray, innerLabel, BgColors, "line");
    }

    private JsonObject getBarGraphData(List<Reaction> reactions, String innerLabel, JsonArray BgColors) {
        JsonArray jsonArray = new JsonArray();
        for (Reaction reaction : reactions) {
            jsonArray.add(reaction.getValue());
        }

        return getDataSetObject(jsonArray, innerLabel, BgColors, "bar");
    }

    private JsonObject getRadarGraphData(List<Reaction> reactions) {
        // no use in dashboard
        return new JsonObject();
    }

    private JsonObject getPieGraphData() {
        return new JsonObject();
    }

    private JsonObject getDataSetObject(JsonArray arr, String label, JsonArray colors, String chartType) {
        JsonObject wrapper = new JsonObject();
        JsonArray dataset = new JsonArray();
        JsonObject data = new JsonObject();

        data.add("data", arr);
        data.addProperty("label", label);

        if (chartType.equals("line")) {
            data.addProperty("borderColor", "#3498db");
            data.addProperty("backgroundColor", "transparent");
            data.addProperty("fill", false);
        } else {
            data.add("backgroundColor", colors);

        }


        dataset.add(data);

        wrapper.add("dataset", dataset);
        return wrapper;
    }

    private JsonArray generateLineChartLabels(Timestamp fromTime,Timestamp toTime, Integer numberOfDays){
        Long toTimeLong = toTime.getTime();
        Long fromTimeLong = fromTime.getTime();

        // get the total diff between the dates
        Long diffTimes = toTimeLong - fromTimeLong;

        // get the diff between each label
        Long diffBetweenLabels = diffTimes / numberOfDays;

        JsonArray labels = new JsonArray();

        for (int i=-1; i< numberOfDays; i++) {
            labels.add(formatLabels(i, fromTimeLong, diffBetweenLabels));
        }
        return labels;

    }

    private String formatLabels(int i, Long time, Long diff) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");

        Long newDate = time + diff * i;
        String dateString = dateFormat.format(newDate);
        Timestamp newTimestamp = Timestamp.valueOf(dateString);
        String newDateString = dateFormat1.format(newTimestamp);

        return newDateString;
    }


    private JsonArray generateBarChartLabels(List<Reaction> reactionValues) {
        JsonArray labels = new JsonArray();
        for (Reaction reactionValue : reactionValues) {
            String reaction = reactionValue.getReaction();
            reaction = reaction.substring(0, 1).toUpperCase() + reaction.substring(1);
            labels.add(reaction);
        }
        return labels;
    }


    private JsonObject getOptionsForGraph(JsonObject exposureObject, String titleText, boolean displayLegend) {
        JsonObject options = new JsonObject();
        JsonObject legend = new JsonObject();
        JsonObject title = new JsonObject();

        if (!titleText.equals("")) {
            title.addProperty("display", true);
        } else {
            title.addProperty("display", false);
        }
        title.addProperty("text", titleText);

        legend.addProperty("display", displayLegend);

        options.add("legend", legend);
        options.add("title", title);

        exposureObject.add("options", options);
        return exposureObject;
    }

    private String formatDate(String dateToConvert) {
        Timestamp temp = Timestamp.valueOf(dateToConvert + " 00:00:00");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(temp);

    }

    private JsonArray generateLineColors() {
        JsonArray colors = new JsonArray();

        colors.add("#2ecc71");  // calm
        colors.add("#f1c40f");  // happy
        colors.add("#34495e");  // confused
        colors.add("#8e44ad");  // disgust
        colors.add("#e74c3c");  // anger
        colors.add("#0079DB");  // sad
        colors.add("#e67e22");  // surprised

        return colors;
    }

}

