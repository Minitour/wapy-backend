package me.wapy.controllers;

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

        // get the camera id
        String camera_id = body.has("camera_id") ? body.get("camera_id").getAsString() : "";

        // get the object id from the request parameters
        String object_id = request.params(":id") != null ? request.params(":id") : "";

        // get the from timestamp
        String fromTimeString = body.has("fromTime") ? body.get("fromTime").getAsString() : "";
        Timestamp fromTime = !fromTimeString.equals("") ? Timestamp.valueOf(fromTimeString) : null;

        // get the to timestamp
        String toTimeString = body.has("toTime") ? body.get("toTime").getAsString() : "";
        Timestamp toTime = !toTimeString.equals("") ? Timestamp.valueOf(toTimeString) : null;

        // checking for nulls
        if (object_id.equals("") || fromTime == null || toTime == null || camera_id.equals(""))
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
            Long views = access.getTotalViewsPerProduct(camera_id, object_id, fromTime, toTime);

            JsonObject viewsObject = getProductAsJson("Views", null, views, "#172b4d", "#172b4d", "#172b4d", 0L, true, "", false);

            statsObject.add(viewsObject);


            // ---------------------------------------------------------------//
            //  get total likes per product
            // ---------------------------------------------------------------//
            Long likes = access.getTotalLikesPerProduct(camera_id, object_id, fromTime, toTime);

            JsonObject likesObject = getProductAsJson("Likes", null, likes, "#172b4d", "#172b4d", "#172b4d", 0L, true, "", false);

            statsObject.add(likesObject);

            // ---------------------------------------------------------------//
            //  get total smiles for product
            // ---------------------------------------------------------------//

            Long smiles = access.getSmilesForProduct(fromTime, toTime, object_id, camera_id);

            JsonObject smilesObject = getProductAsJson("Smiles", null, smiles, "#172b4d", "#172b4d", "#172b4d", 0L, true, "", false);

            statsObject.add(smilesObject);

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
}
