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
            "camera_id" : "1",
            "fromTime": "2019-04-12 12:34:12",
            "toTime": "2019-04-12 12:45:12"
        }

        ## this is the minimal info we need to send the controller results

        ## from the camera_id we will get all the product in window ->
                can give us the smiles for product
                and
                can give us the reactions for product
         */

        // get the response as json object for extraction
        JsonObject jsonObject = gson.fromJson(body, JsonObject.class);

        // get the camera id
        String camera_id = jsonObject.get("camera_id").getAsString();

        // get the from timestamp
        String fromTimeString = jsonObject.get("fromTime").getAsString();
        Timestamp fromTime = Timestamp.valueOf(fromTimeString);

        // get the to timestamp
        String toTimeString = jsonObject.get("toTime").getAsString();
        Timestamp toTime = Timestamp.valueOf(toTimeString);

        JsonObject jsonBuilder = new JsonObject();

        try(DashboardAccess access = new DashboardAccess()) {

            // ---------------------------------------------------------------//
            //  traffic
            // ---------------------------------------------------------------//

            // getting the traffic
            Long counter = access.getTraffic(fromTime, toTime);

            // adding the traffic number to the json response
            // will get 0 or above - no nulls
            jsonBuilder.addProperty("traffic", counter);


            // ---------------------------------------------------------------//
            //  most viewed product
            // ---------------------------------------------------------------//

            //getting the most viewed product (according to the objects_table)
            Product most_viewed_product = access.getMostViewedProduct(fromTime, toTime);

            if (most_viewed_product != null) {
                // parsing the product for the json response
                JsonObject jsonProduct = getProductAsJson(most_viewed_product);

                // append to the json response
                jsonBuilder.add("most_viewed_product", jsonProduct);
            }

            // ---------------------------------------------------------------//
            //  least viewed product
            // ---------------------------------------------------------------//

            // getting the least viewed product (according to objects_table)
            Product least_viewed_product = access.getLeastViewedProduct(fromTime, toTime);

            // product can be null -> will not add to the json response
            if (least_viewed_product != null) {
                // parsing the product for the json response
                JsonObject jsonProduct = getProductAsJson(least_viewed_product);

                // append to the json response
                jsonBuilder.add("least_viewed_product", jsonProduct);
            }

            // ---------------------------------------------------------------//
            //  most viewed product reaction
            // ---------------------------------------------------------------//

            // getting the most viewed product (according to images_table)
            Product most_viewed_reaction_product = access.getMostViewedProductReaction(fromTime, toTime);

            // product can be null -> will not add to the json response
            if (most_viewed_reaction_product != null) {
                // parsing the product for the json response
                JsonObject jsonProduct = getProductAsJson(most_viewed_reaction_product);

                // append to the json response
                jsonBuilder.add("most_viewed_reaction_product", jsonProduct);
            }

            // ---------------------------------------------------------------//
            //  least viewed product reaction
            // ---------------------------------------------------------------//

            // getting the least viewed product (according to images_table)
            Product least_viewed_reaction_product = access.getLeastViewedProductReaction(fromTime, toTime);

            if (least_viewed_reaction_product != null) {
                // parsing the product for the json response
                JsonObject jsonProduct = getProductAsJson(least_viewed_reaction_product);

                // append to the json response
                jsonBuilder.add("least_viewed_reaction_product", jsonProduct);
            }


            // ---------------------------------------------------------------//
            //  exposure
            // ---------------------------------------------------------------//
            Long exposure = access.getExposure(fromTime, toTime);

            // adding the exposure to the json response
            // will get 0 and above -> no nulls
            jsonBuilder.addProperty("exposure", exposure);


            // ---------------------------------------------------------------//
            // all products in window
            /*
            {
                "products": [
                    {
                        "camera_id" : "",
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
            List<Product> productsList = access.getAllProductInWindow(camera_id, fromTime, toTime);

            // checking we have a list to append
            if (!productsList.isEmpty()) {

                // construct an array of products as json array -> will append as property: products
                JsonArray jsonProducts = new JsonArray();
                for (Product product : productsList) {
                    jsonProducts.add(getProductAsJson(product));
                }

                jsonBuilder.add("products_in_window", jsonProducts);
            }


            // ---------------------------------------------------------------//
            // we will return the json we collected so far if we dont have a product list
            // the next functions are dependent that there are objects in the product list
            // ---------------------------------------------------------------//
            if (productsList.isEmpty()) {
                return JSONResponse.FAILURE().data(jsonBuilder);
            }


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

            try(ProductAccess pAccess = new ProductAccess(access)){
                for (Product product : productsList) {
                    // getting the info from the product
                    String product_id = product.getObject_id();

                    // getting the smiles for the product
                    Long smilesForProduct = pAccess.getSmilesForProduct(fromTime, toTime, product_id, camera_id);

                    smilePerProduct.addProperty(product_id, smilesForProduct);
                }

                // adding the smiles to the json response
                jsonBuilder.add("smiles_per_product", smilePerProduct);
            }



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
            List<Map<String,List<Reaction>>> productsReactionsList = access.getReactionsPerProduct(camera_id, fromTime, toTime);

            // json with products and the reactions corresponding to the product
            JsonObject jsonProductReactions = new JsonObject();

            for (Map<String, List<Reaction>> stringListMap : productsReactionsList) {

                // getting the name of the product
                Set productName = stringListMap.keySet();

                // getting the reactions
                List<Reaction> reactionsForProduct = stringListMap.get(productName.toString());

                // convert the reactions to json object
                JsonObject jsonReactions = new JsonObject();
                for (Reaction reaction : reactionsForProduct) {
                    jsonReactions.addProperty(reaction.getReaction(), reaction.getValue());
                }

                // adding the json with product and list of reactions as json
                jsonProductReactions.add(productName.toString(), jsonReactions);

            }

            // adding the reactions to the json response
            jsonBuilder.add("products_reactions", jsonProductReactions);


            // adding the json builder for final json object that contains the dashboard data
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.add("dashboard", jsonBuilder);

            // return the json response with all the dashboard data we need
            return JSONResponse.SUCCESS().message(String.valueOf(jsonResponse));

            /*
            response will be like this:
             "dashboard": {
                    "traffic": "",
                    "most_viewed_product": {
                        "camera_id" : "",
                        "object_id" : "",
                        "store_id" : "",
                        "timestamp" : "",
                        "value" : "",
                    },
                    "least_viewed_product": {
                        "camera_id" : "",
                        "object_id" : "",
                        "store_id" : "",
                        "timestamp" : "",
                        "value" : "",
                    },
                    "most_viewed_reaction_product": {
                        "camera_id" : "",
                        "object_id" : "",
                        "store_id" : "",
                        "timestamp" : "",
                        "value" : "",
                    },
                     "least_viewed_reaction_product": {
                        "camera_id" : "",
                        "object_id" : "",
                        "store_id" : "",
                        "timestamp" : "",
                        "value" : "",
                    },
                    "exposure": "value",
                    "products_in_window": [
                        {
                            "camera_id" : "",
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

    private JsonObject getProductAsJson(Product product) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("camera_id", product.getCamera_id());
        jsonObject.addProperty("object_id", product.getObject_id());
        jsonObject.addProperty("store_id", product.getStore_id());
        jsonObject.addProperty("timestamp", product.getTimestamp().toString());
        jsonObject.addProperty("value", product.getValue());
        return jsonObject;

        /*
        {
            "camera_id" : "",
            "object_id" : "",
            "store_id" : "",
            "timestamp" : "",
            "value" : "",
        }
         */
    }

}

