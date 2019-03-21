package me.wapy.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.wapy.database.AuthContext;
import me.wapy.database.AuthContextBusiness;
import me.wapy.database.exception.AuthException;
import me.wapy.database.exception.DataAccessException;
import me.wapy.database.exception.MissingParametersException;
import me.wapy.database.exception.MissingPermissionsException;
import spark.Request;
import spark.Response;
import spark.Route;


@FunctionalInterface
public interface RESTRoute extends Route {

    @Override
    default Object handle(Request request, Response response) {
        response.header("Content-Type", "application/json");
        try {
            return handle(request, response, new Gson().fromJson(request.body(), JsonObject.class));
        }
        catch (MissingParametersException e){
            return ErrorCode
                    .MISSING_PARAMETERS
                    .toJsonResponse();
        }
        catch(AuthException e){
            return ErrorCode
                    .INVALID_CONTEXT
                    .toJsonResponse();
        }
        catch (MissingPermissionsException e) {
            return ErrorCode
                    .NO_PERMISSION
                    .toJsonResponse();
        }
        catch (DataAccessException e){
            return ErrorCode
                    .DATA_SOURCE_ERROR
                    .toJsonResponse();
        }
        catch (Exception e) {
            return ErrorCode
                    .UNKNOWN_ERROR
                    .toJsonResponse();
        }
    }

    Object handle(Request request, Response response, JsonObject body) throws Exception;

    default AuthContext extractFromBody(JsonObject body) {
        try {
            long id = body.get("user_id").getAsLong();
            String sessionToken = body.get("session_token").getAsString();
            return new AuthContext(id, sessionToken);
        } catch (NullPointerException e) {
            return null;
        }
    }

    default AuthContextBusiness extractFromBodyBusiness(JsonObject body) {
        try {
            long id = body.get("business_id").getAsLong();
            String sessionToken = body.get("session_token_business").getAsString();
            return new AuthContextBusiness(id, sessionToken);
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Use this method to check that parameters are not null.
     *
     * @param parameters The parameters
     */
    default void require(Object... parameters) {
        for (Object object : parameters)
            if (object == null)
                throw new MissingParametersException();
    }

    default void requireKeys(JsonObject body, String... keys){
        for (String key : keys)
            if(!body.has(key))
                throw new MissingParametersException();
    }


    default String getString(JsonObject body, String key, DefaultValue<String> safeGuard) {
        try {
            return body.get(key).getAsString();
        } catch (NullPointerException | ClassCastException | IllegalStateException ignored) {
            return safeGuard.getDefaultIfNotFound();
        }
    }

    default Number getNumber(JsonObject body, String key, DefaultValue<Number> safeGuard) {
        try {
            return body.get(key).getAsNumber();
        } catch (NullPointerException | ClassCastException | IllegalStateException ignored) {
            return safeGuard.getDefaultIfNotFound();
        }
    }

    default boolean getBoolean(JsonObject body, String key, DefaultValue<Boolean> safeGuard) {
        try {
            return body.get(key).getAsBoolean();
        } catch (NullPointerException | ClassCastException | IllegalStateException ignored) {
            return safeGuard.getDefaultIfNotFound();
        }
    }


    @FunctionalInterface
    interface DefaultValue<T> {
        T getDefaultIfNotFound();
    }
}