package uk.oczadly.karl.jnano.rpc;

import com.google.gson.*;
import uk.oczadly.karl.jnano.internal.JNH;
import uk.oczadly.karl.jnano.rpc.exception.*;
import uk.oczadly.karl.jnano.rpc.response.RpcResponse;

import java.lang.reflect.Field;

public class RpcResponseDeserializerImpl implements RpcResponseDeserializer {
    
    private static volatile Field RESPONSE_JSON_FIELD;
    
    private Gson gson;
    
    public RpcResponseDeserializerImpl() {
        this(JNH.GSON);
    }
    
    public RpcResponseDeserializerImpl(Gson gson) {
        this.gson = gson;
    }
    
    
    public Gson getGsonInstance() {
        return gson;
    }
    
    
    @Override
    public <R extends RpcResponse> R deserialize(String response, Class<R> responseClass) throws RpcException {
        JsonObject responseJson;
        try {
            responseJson = JsonParser.parseString(response).getAsJsonObject(); // Parse response
    
            // Check for returned RPC error
            JsonElement errorElement = responseJson.get("error");
            if (errorElement != null)
                throw parseException(errorElement.getAsString());
    
            // Deserialize response
            R responseObj = gson.fromJson(responseJson, responseClass);
            populateJsonField(responseObj, responseJson);
    
            return responseObj;
        } catch (JsonParseException ex) {
            throw new RpcInvalidResponseException(response, ex); // If unable to parse
        }
    }
    
    
    public RpcException parseException(String msg) {
        String msgLc = msg.toLowerCase();
        
        // Check and parse error type
        switch (msgLc) {
            case "wallet is locked":
            case "wallet locked":
                return new RpcWalletLockedException();             // Wallet locked
            case "insufficient balance":
                return new RpcInvalidArgumentException(msg + "."); // Invalid/bad argument
            case "rpc control is disabled":
                return new RpcControlDisabledException();          // RPC control disabled
            case "unable to parse json":
                return new RpcInvalidRequestJsonException(         // Invalid request body
                        "The RPC server was unable to parse the JSON request.");
            case "unknown command":
                return new RpcUnknownCommandException();           // Unknown command
            case "invalid header: body limit exceeded":
                return new RpcInvalidRequestJsonException(         // JSON too long
                        "The request JSON exceeded the configured maximum length.");
        }
        
        if (msgLc.startsWith("bad") || msgLc.startsWith("invalid") || msgLc.endsWith("invalid")
                || msgLc.endsWith("required")) {
            return new RpcInvalidArgumentException(msg + ".");    // Invalid/bad argument
        } else if (msgLc.contains("not found")) {
            return new RpcEntityNotFoundException(msg + ".");     // Unknown referenced entity
        } else if (msgLc.endsWith("is disabled")) {
            return new RpcFeatureDisabledException(msg + ".");    // Feature is disabled
        } else if (msgLc.contains("json")) {
            return new RpcInvalidRequestJsonException(msg + "."); // Disallowed/invalid JSON request
        } else if (msgLc.startsWith("internal")) {
            return new RpcInternalException(msg + ".");           // Internal server error
        }
        
        return new RpcException(msg.isEmpty() ? null : (msg + ".")); // Default to base exception
    }
    
    
    private void populateJsonField(RpcResponse response, JsonObject json) {
        if (RESPONSE_JSON_FIELD == null) {
            try {
                RESPONSE_JSON_FIELD = RpcResponse.class.getDeclaredField("rawJson");
                RESPONSE_JSON_FIELD.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        if (RESPONSE_JSON_FIELD != null) {
            try {
                RESPONSE_JSON_FIELD.set(response, json);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

}