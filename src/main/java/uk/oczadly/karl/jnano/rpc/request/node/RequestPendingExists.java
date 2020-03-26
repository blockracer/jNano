package uk.oczadly.karl.jnano.rpc.request.node;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.jnano.rpc.RpcRequest;
import uk.oczadly.karl.jnano.rpc.response.ResponseExists;

/**
 * This request class is used to check whether a specified block is still pending.
 * The server responds with a {@link ResponseExists} data object.<br>
 * Calls the internal RPC method {@code pending_exists}.
 *
 * @see <a href="https://docs.nano.org/commands/rpc-protocol/#pending_exists">Official RPC documentation</a>
 */
public class RequestPendingExists extends RpcRequest<ResponseExists> {
    
    @Expose @SerializedName("hash")
    private final String blockHash;
    
    
    /**
     * @param blockHash the block's hash
     */
    public RequestPendingExists(String blockHash) {
        super("pending_exists", ResponseExists.class);
        this.blockHash = blockHash;
    }
    
    
    /**
     * @return the requested block's hash
     */
    public String getBlockHash() {
        return blockHash;
    }
    
}
