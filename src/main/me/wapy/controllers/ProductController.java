package me.wapy.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.wapy.database.data_access.ProductAccess;
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
            Long likes = access.getReactionsPerProduct(owner_uid, object_id, fromTime, toTime);

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

            JsonObject reactionsObject = getInitGraphObject("radar", "Product - Reactions Map", false, "Reactions");

            reactionsObject = getGraphData(reactionsObject, null, reactions,"Reaction", fromTime, toTime, numberOfDays);

            reactionsObject = getOptionsForGraph(reactionsObject, "Product Reactions", false);

            graphsObject.add(reactionsObject);


            // ---------------------------------------------------------------//
            //  get views per time period
            // ---------------------------------------------------------------//

            JsonObject views_over_time_object = getInitGraphObject("line", "Views Over Time", false, "Views");
            JsonArray labels = generateLineChartLabels(fromTime, toTime, numberOfDays);
            List<Long> views_over_time = new ArrayList<>();

            for (int i=0; i<labels.size() - 1; i++) {
                String stringFromtime = formatDate(labels.get(i).getAsString());
                String stringToTime = formatDate(labels.get(i+1).getAsString());
                Timestamp tempFromTime = Timestamp.valueOf(stringFromtime);
                Timestamp tempToTime = Timestamp.valueOf(stringToTime);
                Long exposure = access.getTotalViewsPerProduct(owner_uid, object_id, tempFromTime, tempToTime);
                views_over_time.add(exposure);
            }

            // setting the graph for the product
            views_over_time_object = getGraphData(views_over_time_object, views_over_time, null, "Views", fromTime, toTime, numberOfDays);

            // getting the option for the graph
            views_over_time_object = getOptionsForGraph(views_over_time_object, "Views", false);

            graphsObject.add(views_over_time_object);

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

    private JsonObject getGraphData(JsonObject initObject, List<Long> longValues, List<Reaction> reactionValues, String innerLabel, Timestamp fromTime, Timestamp toTime, Integer numberOfDays){
        JsonObject data = new JsonObject();
        JsonArray labels = new JsonArray();
        switch (initObject.get("type").getAsString()) {
            case "line": {
                labels = generateLineChartLabels(fromTime, toTime, numberOfDays);
                labels.remove(0);
                data = getLineGraphData(longValues, innerLabel, new JsonArray(), labels);
                data.add("labels", labels);
                break;
            }
            case "bar": {
                data = getBarGraphData(reactionValues);
                break;
            }
            case "radar": {
                JsonArray colors = new JsonArray();
                labels = generateRadarLabels(reactionValues);
                data = getRadarGraphData(reactionValues, colors);
                data.add("labels", labels);
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


    private JsonObject getLineGraphData(List<Long> values, String innerLabel, JsonArray BgColors, JsonArray labels) {

        JsonArray jsonArray = new JsonArray();
        int index = 0;
        for (Long value : values) {

            JsonObject xY = new JsonObject();

            String dateString = labels.get(index).getAsString();

            // add the values to fields
            xY.addProperty("x", dateString);
            xY.addProperty("y", value);

            jsonArray.add(xY);

            index += 1;
        }

        return getDataSetObject(jsonArray, innerLabel, BgColors, "line");
    }

    private JsonObject getBarGraphData(List<Reaction> reactions) {
        return new JsonObject();
    }

    private JsonObject getRadarGraphData(List<Reaction> reactions, JsonArray colors) {
        JsonArray valuesArray = new JsonArray();
        for (Reaction reaction : reactions) {
            valuesArray.add(reaction.getValue());
        }

        return getDataSetObject(valuesArray, "Reaction", colors, "radar");
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
        } else if (chartType.equals("bar")) {
            data.add("backgroundColor", colors);

        } else if (chartType.equals("radar")) {
            data.addProperty("fill", "true");
            data.addProperty("borderColor", "#3498db");
            data.addProperty("backgroundColor", "#3361gb");
            data.addProperty("pointBorderColor", "#3498db");
            data.addProperty("pointBackgroundColor", "#3443db");

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

        for (int i=0; i< numberOfDays; i++) {
            labels.add(formatLabels(i, fromTimeLong, diffBetweenLabels));
        }
        labels.add(formatLabels(0, toTimeLong, 0L));

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

    private JsonArray generateRadarLabels(List<Reaction> reactionValues){
        JsonArray labels = new JsonArray();
        for (Reaction reactionValue : reactionValues) {
            labels.add(reactionValue.getReaction());
        }
        return labels;
    }

    private String formatDate(String dateToConvert) {
        Timestamp temp = Timestamp.valueOf(dateToConvert + " 00:00:00");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(temp);

    }
}
