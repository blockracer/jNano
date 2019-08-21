package uk.oczadly.karl.jnano.rpc.request.wallet;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.jnano.rpc.request.RpcRequest;
import uk.oczadly.karl.jnano.rpc.response.ResponseSuccessful;

public class RequestSearchPending extends RpcRequest<ResponseSuccessful> {
    
    @Expose @SerializedName("wallet")
    private String walletId;
    
    
    public RequestSearchPending(String walletId) {
        super("search_pending", ResponseSuccessful.class);
        this.walletId = walletId;
    }
    
    
    public String getWalletId() {
        return walletId;
    }
    
}