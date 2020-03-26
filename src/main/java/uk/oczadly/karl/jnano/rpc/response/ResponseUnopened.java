package uk.oczadly.karl.jnano.rpc.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.jnano.rpc.RpcResponse;

import java.math.BigInteger;
import java.util.Map;

/**
 * This response class contains a set of unopened accounts.
 */
public class ResponseUnopened extends RpcResponse {
    
    @Expose @SerializedName("accounts")
    private Map<String, BigInteger> accounts;
    
    
    /**
     * Map follows the structure {@code account address -> pending balance}.
     * @return a map of unopened accounts and their total pending balance in RAW
     */
    public Map<String, BigInteger> getAccounts() {
        return accounts;
    }
    
    /**
     * @param accountAddress an unopened account's address
     * @return the pending balance of the account in RAW, or null if not present in the response
     */
    public BigInteger getPendingBalance(String accountAddress) {
        return this.accounts.get(accountAddress.toLowerCase());
    }
    
}
