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
import java.util.List;
import java.util.Map;
import java.util.Set;

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

            JsonObject trafficObject = getProductAsJson("Traffic", null, counter, "#172b4d", "#172b4d", "#172b4d", 0L, true, "", false);

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

            JsonObject mostViewedProductObject = getProductAsJson("Most Viewed Product", null, most_viewed_product_value, "#172b4d", "#172b4d", "#172b4d", 0L, true, "", false);

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

            JsonObject leastViewedProductObject = getProductAsJson("Least Viewed Product", null, least_viewed_product_value, "#172b4d", "#172b4d", "#172b4d", 0L, true, "", false);

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

            JsonObject mostViewedReactionProductObject = getProductAsJson("Most Viewed Product Reaction", null, most_viewed_reaction_product_value, "#172b4d", "#172b4d", "#172b4d", 0L, true, "", false);

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

            JsonObject leastViewedReactionProductObject = getProductAsJson("Least Viewed Product Reaction", null, least_viewed_reaction_product_value, "#172b4d", "#172b4d", "#172b4d", 0L, true, "", false);

            // append to the stats array
            statsObject.add(leastViewedReactionProductObject);


            // ---------------------------------------------------------------//
            //  exposure
            // ---------------------------------------------------------------//
            Long exposure = access.getExposure(fromTime, toTime);

            // adding the exposure to the json response
            // will get 0 and above -> no nulls
            //jsonBuilder.addProperty("exposure", exposure);


            // ---------------------------------------------------------------//
            // all products in window
            /*
            {
                "products": [
                    {
                        "owner_uid" : "",
                        "object_id" : "",
                        "store_id" : "",
                        "timestamp" : "",
                        "value" : "",
                    }
                ]
            }
             */
            // we are getting the products for window and will use later also
            // ---------------------------------------------------------------//
            List<Product> productsList = access.getAllProductInWindow(owner_uid, fromTime, toTime);

            // checking we have a list to append
            if (!productsList.isEmpty()) {

                // construct an array of products as json array -> will append as property: products
                JsonArray jsonProducts = new JsonArray();
                for (Product product : productsList) {
                    //jsonProducts.add(getProductAsJson(product));
                }

                //jsonBuilder.add("products_in_window", jsonProducts);
            }


            // ---------------------------------------------------------------//
            // we will return the json we collected so far if we dont have a product list
            // the next functions are dependent that there are objects in the product list
            // ---------------------------------------------------------------//
//            if (productsList.isEmpty()) {
//                return JSONResponse.FAILURE().data(jsonBuilder);
//            }


            // ---------------------------------------------------------------//
            // smiles per product
            /*
            {
                "smiles_per_product": {
                    "product_id_1" : "",
                    "product_id_2" : ""

                }
            }
             */
            // assumption: if got here we have product list not empty
            // ---------------------------------------------------------------//
            JsonObject smilePerProduct = new JsonObject();

//            try(ProductAccess pAccess = new ProductAccess(access)){
//                for (Product product : productsList) {
//                    // getting the info from the product
//                    String product_id = product.getObject_id();
//
//                    // getting the smiles for the product
//                    Long smilesForProduct = pAccess.getSmilesForProduct(fromTime, toTime, product_id, owner_uid);
//
//                    smilePerProduct.addProperty(product_id, smilesForProduct);
//                }
//
//                // adding the smiles to the json response
//                //jsonBuilder.add("smiles_per_product", smilePerProduct);
//            }



            // ---------------------------------------------------------------//
            // reactions per product
            /*
            {
                "products_reactions": {
                    "product_id_1": {
                            "sad": "value",
                            "angry": "value"
                     },
                     "product_id_2": {
                            "sad": "value",
                            "angry": "value"
                     }
                 }

            }
             */
            // ---------------------------------------------------------------//

            // getting all reactions for all products
//            List<Map<String,List<Reaction>>> productsReactionsList = access.getReactionsPerProduct(owner_uid, fromTime, toTime);
//
//            // json with products and the reactions corresponding to the product
//            JsonObject jsonProductReactions = new JsonObject();
//
//            for (Map<String, List<Reaction>> stringListMap : productsReactionsList) {
//
//                // getting the name of the product
//                Set productName = stringListMap.keySet();
//
//                // getting the reactions
//                List<Reaction> reactionsForProduct = stringListMap.get(productName.toString());
//
//                // convert the reactions to json object
//                JsonObject jsonReactions = new JsonObject();
//                for (Reaction reaction : reactionsForProduct) {
//                    jsonReactions.addProperty(reaction.getReaction(), reaction.getValue());
//                }
//
//                // adding the json with product and list of reactions as json
//                jsonProductReactions.add(productName.toString(), jsonReactions);
//
//            }

            // adding the reactions to the json response
            //jsonBuilder.add("products_reactions", jsonProductReactions);

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
                "graphs":[],
                "tables":[]
            }

            LEFT TO FIX:
             "dashboard": {

                    "exposure": "value",
                    "products_in_window": [
                        {
                            "owner_uid" : "",
                            "object_id" : "",
                            "store_id" : "",
                            "timestamp" : "",
                            "value" : "",
                        }
                    ],
                    "smiles_per_product": {
                        "product_id_1" : "",
                        "product_id_2" : ""

                    },
                    "products_reactions": [
                        "product_id_1": {
                                "sad": "value",
                                "angry": "value"
                         },
                         "product_id_2": {
                                "sad": "value",
                                "angry": "value"
                         }
                     ]
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

