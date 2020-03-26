package uk.oczadly.karl.jnano.rpc.request.node;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.jnano.rpc.RpcRequest;
import uk.oczadly.karl.jnano.rpc.response.ResponseWorkValidation;

/**
 * This request class is used to check whether the specified work is valid for the specified block.
 * The server responds with a {@link ResponseWorkValidation} data object.<br>
 * Calls the internal RPC method {@code work_validate}.
 *
 * @see <a href="https://docs.nano.org/commands/rpc-protocol/#work_validate">Official RPC documentation</a>
 */
public class RequestWorkValidate extends RpcRequest<ResponseWorkValidation> {
    
    @Expose @SerializedName("work")
    private final String workSolution;
    
    @Expose @SerializedName("hash")
    private final String blockHash;
    
    
    @Expose @SerializedName("difficulty")
    private final String difficulty;
    
    @Expose @SerializedName("multiplier")
    private final Double multiplier;
    
    
    /**
     * @param workSolution  the computed work value
     * @param blockHash     the block's hash
     */
    public RequestWorkValidate(String workSolution, String blockHash) {
        this(workSolution, blockHash, null, null);
    }
    
    /**
     * Constructs a work validation request with a specific difficulty value.
     * @param workSolution  the computed work value
     * @param blockHash     the block's hash
     * @param difficulty    the difficulty value
     */
    public RequestWorkValidate(String workSolution, String blockHash, String difficulty) {
        this(workSolution, blockHash, difficulty, null);
    }
    
    /**
     * Constructs a work validation request with a specific difficulty multiplier.
     * @param workSolution  the computed work value
     * @param blockHash     the block's hash
     * @param multiplier    the difficulty multiplier
     */
    public RequestWorkValidate(String workSolution, String blockHash, Double multiplier) {
        this(workSolution, blockHash, null, multiplier);
    }
    
    private RequestWorkValidate(String workSolution, String blockHash, String difficulty, Double multiplier) {
        super("work_validate", ResponseWorkValidation.class);
        this.workSolution = workSolution;
        this.blockHash = blockHash;
        this.difficulty = difficulty;
        this.multiplier = multiplier;
    }
    
    
    /**
     * @return the requested work solution
     */
    public String getWorkSolution() {
        return workSolution;
    }
    
    /**
     * @return the requested block hash
     */
    public String getBlockHash() {
        return blockHash;
    }
    
    /**
     * @return the requested work difficulty
     */
    public String getDifficulty() {
        return difficulty;
    }
    
    /**
     * @return the requested work multiplier
     */
    public Double getMultiplier() {
        return multiplier;
    }
    
}
