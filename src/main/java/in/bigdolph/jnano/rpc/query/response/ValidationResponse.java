package in.bigdolph.jnano.rpc.query.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import in.bigdolph.jnano.rpc.query.response.RPCResponse;

public class ValidationResponse extends RPCResponse {

    @Expose
    @SerializedName("valid")
    private boolean isValid;
    
    
    public boolean isValid() {
        return isValid;
    }
    
}