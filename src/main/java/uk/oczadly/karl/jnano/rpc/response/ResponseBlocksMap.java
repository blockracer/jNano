package uk.oczadly.karl.jnano.rpc.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.jnano.model.block.Block;
import uk.oczadly.karl.jnano.rpc.RpcResponse;

import java.util.Map;

/**
 * This response class contains a map of block hashes and their contents.
 */
public class ResponseBlocksMap extends RpcResponse {
    
    @Expose @SerializedName("blocks")
    private Map<String, Block> blocks;
    
    
    /**
     * Map follows the structure {@code hash -> block}.
     * @return a list of blocks
     */
    public Map<String, Block> getBlocks() {
        return blocks;
    }
    
    /**
     * @param blockHash the block's hash
     * @return the block's contents
     */
    public Block getBlock(String blockHash) {
        return this.blocks.get(blockHash.toUpperCase());
    }
    
}
