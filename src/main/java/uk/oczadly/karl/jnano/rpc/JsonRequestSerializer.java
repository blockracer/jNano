/*
 * Copyright (c) 2020 Karl Oczadly (karl@oczadly.uk)
 * Licensed under the MIT License
 */

package uk.oczadly.karl.jnano.rpc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import uk.oczadly.karl.jnano.internal.JNC;
import uk.oczadly.karl.jnano.rpc.request.RpcRequest;

/**
 * The standard implementation of {@link RpcRequestSerializer}, which serializes requests into a JSON object and adds
 * the command as the {@code action} attribute.
 *
 * <p>Example output JSON (for an account balance request):</p>
 * <pre>{"account":"nano_34qjpc8t1u6wnb584pc4iwsukwa8jhrobpx4oea5gbaitnqafm6qsgoacpiz","action":"account_balance"}</pre>
 */
public class JsonRequestSerializer implements RpcRequestSerializer {
    
    private final Gson gson;
    
    /**
     * Constructs using default jNano GSON instance.
     */
    public JsonRequestSerializer() {
        this(JNC.GSON);
    }
    
    /**
     * @param gson the gson instance
     */
    public JsonRequestSerializer(Gson gson) {
        this.gson = gson;
    }
    
    
    /**
     * @return the gson instance
     */
    public final Gson getGson() {
        return gson;
    }
    
    
    @Override
    public String serialize(RpcRequest<?> request) {
        return serializeJsonObject(request).toString();
    }
    
    
    /**
     * Default serialization method.
     * @param request the request object
     * @return the JSON object
     */
    public JsonObject serializeJsonObject(RpcRequest<?> request) {
        JsonObject obj = gson.toJsonTree(request).getAsJsonObject();
        obj.addProperty("action", request.getActionCommand());
        return obj;
    }

}
