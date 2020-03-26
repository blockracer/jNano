package uk.oczadly.karl.jnano.rpc.exception;

import com.google.gson.JsonSyntaxException;

/**
 * Thrown if the node returns an invalid JSON response.
 */
public class RpcInvalidResponseException extends RpcException {
    
    private final String response;
    
    public RpcInvalidResponseException(String response, JsonSyntaxException source) {
        super("Unable to parse JSON response.", source);
        this.response = response;
    }
    
    
    public String getResponseBody() {
        return response;
    }
    
}
