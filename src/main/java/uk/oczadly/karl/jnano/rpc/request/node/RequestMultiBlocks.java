package uk.oczadly.karl.jnano.rpc.request.node;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.jnano.rpc.RpcRequest;
import uk.oczadly.karl.jnano.rpc.response.ResponseBlocksMap;

/**
 * This request class is used to fetch a JSON representation of the specified block hashes.
 * The server responds with a {@link ResponseBlocksMap} data object.<br>
 * Calls the internal RPC method {@code blocks}.
 *
 * @see <a href="https://docs.nano.org/commands/rpc-protocol/#blocks">Official RPC documentation</a>
 */
public class RequestMultiBlocks extends RpcRequest<ResponseBlocksMap> {
    
    @Expose @SerializedName("json_block")
    private final boolean json = true;
    
    
    @Expose @SerializedName("hashes")
    private final String[] blockHashes;
    
    
    /**
     * @param blockHashes the block hashes
     */
    public RequestMultiBlocks(String... blockHashes) {
        super("blocks", ResponseBlocksMap.class);
        this.blockHashes = blockHashes;
    }
    
    
    /**
     * @return the requested block hashes
     */
    public String[] getBlockHashes() {
        return blockHashes;
    }
    
}
