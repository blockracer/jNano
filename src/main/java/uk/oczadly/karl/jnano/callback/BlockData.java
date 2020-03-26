package uk.oczadly.karl.jnano.callback;

import com.google.gson.annotations.JsonAdapter;
import uk.oczadly.karl.jnano.internal.gsonadapters.CallbackBlockTypeDeserializer;
import uk.oczadly.karl.jnano.model.block.Block;
import uk.oczadly.karl.jnano.model.block.BlockType;

import java.math.BigInteger;

/**
 * This class encapsulates a variety of data relating to newly-processed blocks. Instances of this class
 * are generated and returned by a {@link BlockCallbackServer} as new blocks are received.
 */
@JsonAdapter(CallbackBlockTypeDeserializer.class)
public class BlockData {
    
    private final String rawJson, accountAddress, blockHash;
    private final Block block;
    private final BlockType subtype;
    private final boolean isSend;
    private final BigInteger amount;
    
    public BlockData(String rawJson, String accountAddress, String blockHash, Block block, BlockType subtype, boolean isSend, BigInteger amount) {
        this.rawJson = rawJson;
        this.accountAddress = accountAddress;
        this.blockHash = blockHash;
        this.block = block;
        this.subtype = subtype;
        this.amount = amount;
        this.isSend = isSend;
    }
    
    
    /**
     * @return the raw JSON block received from the node
     */
    public String getRawJson() {
        return rawJson;
    }
    
    
    /**
     * @return the account who the block belongs to
     */
    public String getAccountAddress() {
        return accountAddress;
    }
    
    /**
     * @return the identifying hash of the block
     */
    public String getBlockHash() {
        return blockHash;
    }
    
    /**
     * @return the block's contents
     */
    public Block getBlockContents() {
        return block;
    }
    
    /**
     * Returns the legacy block type if the block is of universal state type.
     *
     * @return the subtype of the block, or null if not a state block
     */
    public BlockType getSubtype() {
        return subtype;
    }
    
    /**
     * Returns whether or not the block is sending funds to another account.
     *
     * @return if the block is a SEND transaction
     */
    public boolean isSendTransaction() {
        return isSend;
    }
    
    /**
     * @return the value of funds involved, or null if non-transactional
     */
    public BigInteger getTransactionalAmount() {
        return amount;
    }
    
}
