package uk.oczadly.karl.jnano.rpc.request.node;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.jnano.rpc.RpcRequest;
import uk.oczadly.karl.jnano.rpc.response.ResponseOnlineRepresentatives;

/**
 * This request class is used to fetch a list of online representative accounts and their voting weight.
 * The server responds with a {@link ResponseOnlineRepresentatives} data object.<br>
 * Calls the internal RPC method {@code representatives_online}.
 *
 * @see <a href="https://docs.nano.org/commands/rpc-protocol/#representatives_online">Official RPC documentation</a>
 */
public class RequestRepresentativesOnline extends RpcRequest<ResponseOnlineRepresentatives> {
    
    @Expose @SerializedName("weight")
    private final boolean weight = true;
    
    
    public RequestRepresentativesOnline() {
        super("representatives_online", ResponseOnlineRepresentatives.class);
    }
    
}
