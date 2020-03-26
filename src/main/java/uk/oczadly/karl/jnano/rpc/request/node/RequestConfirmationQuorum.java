package uk.oczadly.karl.jnano.rpc.request.node;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.jnano.rpc.RpcRequest;
import uk.oczadly.karl.jnano.rpc.response.ResponseConfirmationQuorum;

/**
 * This request class is used to request information about the network state.
 * The server responds with a {@link ResponseConfirmationQuorum} data object.<br>
 * Calls the internal RPC method {@code confirmation_quorum}.
 *
 * @see <a href="https://docs.nano.org/commands/rpc-protocol/#confirmation_quorum">Official RPC documentation</a>
 */
public class RequestConfirmationQuorum extends RpcRequest<ResponseConfirmationQuorum> {
    
    @Expose @SerializedName("peer_details")
    private final boolean peerDetails = true;
    
    @Expose @SerializedName("peers_stake_required")
    private final boolean peersStakeRequired = true;
    
    
    public RequestConfirmationQuorum() {
        super("confirmation_quorum", ResponseConfirmationQuorum.class);
    }
    
}
