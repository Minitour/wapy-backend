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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ProductController implements RESTRoute {

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
                can get total views per product
                and
                can get total likes per product
                and
                can give us the smiles for product
                and
                can give us the reactions for product
         */

        String owner_uid = body.has("owner_uid") ? body.get("owner_uid").getAsString() : "";

        // get the object id from the request parameters
        String object_id = request.params(":id") != null ? request.params(":id") : "";

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

        // checking for nulls
        if (object_id.equals("") || fromTime == null || toTime == null || owner_uid.equals(""))
            return JSONResponse.FAILURE().message("missing parameters");

        JsonObject jsonBuilder = new JsonObject();

        // init the three arrays for stats, graphs, tables
        JsonArray statsObject = new JsonArray();
        JsonArray graphsObject = new JsonArray();
        JsonArray tablesObject = new JsonArray();

        // ---------------------------------------------------------------//
        //  we will get all products in window
        //  if we dont have any products we will not continue to the next functions
        // ---------------------------------------------------------------//


        try(ProductAccess access = new ProductAccess()) {
            // ---------------------------------------------------------------//
            //  get all views per product
            // ---------------------------------------------------------------//
            Long views = access.getTotalViewsPerProduct(owner_uid, object_id, fromTime, toTime);

            JsonObject viewsObject = getProductAsJson("Views", null, views, "#172b4d", "#172b4d", "#172b4d", 0L, true, "", false);

            statsObject.add(viewsObject);


            // ---------------------------------------------------------------//
            //  get total likes per product
            // ---------------------------------------------------------------//
            Long likes = access.getTotalLikesPerProduct(owner_uid, object_id, fromTime, toTime);

            JsonObject likesObject = getProductAsJson("Likes", null, likes, "#172b4d", "#172b4d", "#172b4d", 0L, true, "", false);

            statsObject.add(likesObject);

            // ---------------------------------------------------------------//
            //  get total smiles for product
            // ---------------------------------------------------------------//

            Long smiles = access.getSmilesForProduct(fromTime, toTime, object_id, owner_uid);

            JsonObject smilesObject = getProductAsJson("Smiles", null, smiles, "#172b4d", "#172b4d", "#172b4d", 0L, true, "", false);

            statsObject.add(smilesObject);

            // ---------------------------------------------------------------//
            //  get all reactions for product
            // ---------------------------------------------------------------//
            List<Reaction> reactions = access.getProductReactionSummary(owner_uid, object_id, fromTime, toTime);

            JsonObject reactionsObject = getInitGraphObject("radar", "Reactions Radar", false, "Reactions");

            reactionsObject = getGraphData(reactionsObject, null, reactions);

            graphsObject.add(reactionsObject);
        }

        // construct the json to return
        jsonBuilder.add("stats", statsObject);
        jsonBuilder.add("graphs", graphsObject);
        jsonBuilder.add("tables", tablesObject);

        JsonObject jsonResponse = new JsonObject();
        jsonResponse.add("product", jsonBuilder);
        return JSONResponse.SUCCESS().data(jsonResponse);

        /*
        response will look like this:
            "product" {
                "stats": [
                    {
                        "title": "Views",
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
                        "title": "Likes",
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
                        "title": "Smiles",
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
                "graphs": [],
                "tables": []
        */
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
    private JsonObject getLineGraphData(List<Long> values) {

        JsonArray jsonArray = new JsonArray();
        JsonArray labels = new JsonArray();
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

        return getDataSetArray(jsonArray, null, "");
    }

    private JsonObject getBarGraphData(List<Reaction> reactions) {
        return new JsonObject();
    }

    private JsonObject getRadarGraphData(List<Reaction> reactions) {
        JsonArray valuesArray = new JsonArray();
        JsonArray labels = new JsonArray();
        for (Reaction reaction : reactions) {
            labels.add(reaction.getReaction());
            valuesArray.add(reaction.getValue());
        }

        return getDataSetArray(valuesArray, labels, "labels");
    }

    private JsonObject getPieGraphData() {
        return new JsonObject();
    }

    private JsonObject getDataSetArray(JsonArray arr, JsonArray additionalArr, String additionalFieldName) {
        JsonObject wrapper = new JsonObject();
        JsonArray dataset = new JsonArray();
        JsonObject data = new JsonObject();

        data.add("data", arr);
        data.addProperty("label", "");
        dataset.add(data);

        wrapper.add("dataset", dataset);
        if(additionalArr != null)
            wrapper.add(additionalFieldName, additionalArr);
        return wrapper;
    }
}
