package in.bigdolph.jnano.rpc.query.request.ledger;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import in.bigdolph.jnano.rpc.query.request.RPCRequest;
import in.bigdolph.jnano.rpc.query.response.generic.BlocksResponse;
import in.bigdolph.jnano.rpc.query.response.specific.NodeVersionResponse;

public class BlockFetchUncheckedRequest extends RPCRequest<BlocksResponse> {
    
    @Expose
    @SerializedName("count")
    private int count;
    
    
    public BlockFetchUncheckedRequest(int count) {
        super("unchecked", BlocksResponse.class);
        this.count = count;
    }
    
    
    
    public int getCount() {
        return count;
    }
    
}