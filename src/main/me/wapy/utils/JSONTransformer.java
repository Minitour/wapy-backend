package me.wapy.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import spark.ResponseTransformer;

import java.util.Map;


public class JSONTransformer implements ResponseTransformer {
    private Gson gson = new Gson();
    @Override
    public String render(Object model) {

        if (model instanceof JSONResponse && ((JSONResponse) model).isInjected()) {
            JsonObject m1 = gson.toJsonTree(model).getAsJsonObject();
            Map<String,Object> vals = ((JSONResponse) model).injected;
            vals.forEach((s, o) -> {

                if (o == null) {
                    m1.remove(s);
                    return;
                }
                
                if (o instanceof Number){
                    m1.addProperty(s,(Number) o);
                    return;
                }

                if (o instanceof String) {
                    m1.addProperty(s, (String) o);
                    return;
                }

                if (o instanceof Boolean) {
                    m1.addProperty(s, (Boolean) o);
                    return;
                }

                if (o instanceof Character) {
                    m1.addProperty(s, (Character) o);
                    return;
                }

                JsonElement element = gson.toJsonTree(o);
                m1.add(s,element);

            });


            return m1.toString();

        }else {
            return gson.toJson(model);
        }
    }
}
