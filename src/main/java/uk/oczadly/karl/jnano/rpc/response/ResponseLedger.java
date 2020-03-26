package uk.oczadly.karl.jnano.rpc.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.jnano.rpc.RpcResponse;

import java.math.BigInteger;
import java.util.LinkedHashMap;

/**
 * This response class contains a set of accounts in the ledger.
 */
public class ResponseLedger extends RpcResponse {
    
    @Expose @SerializedName("accounts")
    private LinkedHashMap<String, AccountInfo> accounts;
    
    
    /**
     * Map follows the structure {@code account address -> information}.
     * @return a map of accounts and their details
     */
    public LinkedHashMap<String, AccountInfo> getAccounts() {
        return accounts;
    }
    
    /**
     * @param account an account's address
     * @return the account's details
     */
    public AccountInfo getAccount(String account) {
        return accounts.get(account.toLowerCase());
    }
    
    
    
    public static class AccountInfo {
        @Expose @SerializedName("frontier")
        private String frontierBlockHash;
    
        @Expose @SerializedName("open_block")
        private String openBlockHash;
    
        @Expose @SerializedName("representative_block")
        private String representativeBlockHash;
    
        @Expose @SerializedName("balance")
        private BigInteger balance;
    
        @Expose @SerializedName("modified_timestamp")
        private int modifiedTimestamp;
    
        @Expose @SerializedName("block_count")
        private int blockCount;
    
        @Expose @SerializedName("representative")
        private String representativeAccount;
    
        @Expose @SerializedName("weight")
        private BigInteger votingWeight;
    
        @Expose @SerializedName("pending")
        private BigInteger balancePending;
    
    
        /**
         * @return the frontier (head) block hash
         */
        public String getFrontierBlockHash() {
            return frontierBlockHash;
        }
    
        /**
         * @return the open (first) block hash
         */
        public String getOpenBlockHash() {
            return openBlockHash;
        }
    
        /**
         * @return the hash of the last block to set this account's representative
         */
        public String getRepresentativeBlockHash() {
            return representativeBlockHash;
        }
    
        /**
         * @return the total number of blocks in this account's blockchain
         */
        public long getBlockCount() {
            return blockCount;
        }
    
        /**
         * @return the UNIX timestamp when this account was last locally modified
         */
        public long getModifiedTimestamp() {
            return modifiedTimestamp;
        }
    
        /**
         * @return the address of this account's representative
         */
        public String getRepresentativeAccount() {
            return representativeAccount;
        }
    
        /**
         * @return the total voting weight delegated to this account
         */
        public BigInteger getVotingWeight() {
            return votingWeight;
        }
    
        /**
         * @return the confirmed balance of this account in RAW
         */
        public BigInteger getBalanceConfirmed() {
            return balance;
        }
    
        /**
         * @return the total pending balance of this account in RAW
         */
        public BigInteger getBalancePending() {
            return balancePending;
        }
        
    }
    
}
