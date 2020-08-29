/*
 * Copyright (c) 2020 Karl Oczadly (karl@oczadly.uk)
 * Licensed under the MIT License
 */

package uk.oczadly.karl.jnano.rpc.request.node;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.jnano.rpc.request.RpcRequest;
import uk.oczadly.karl.jnano.rpc.response.ResponseBlockCount;

/**
 * This request class is used to request the total number of blocks in the ledger.
 * <br>Calls the RPC command {@code block_count}, and returns a {@link ResponseBlockCount} data object.
 *
 * @see <a href="https://docs.nano.org/commands/rpc-protocol/#block_count">Official RPC documentation</a>
 */
public class RequestBlockCount extends RpcRequest<ResponseBlockCount> {
    
    @Expose @SerializedName("include_cemented")
    private final boolean includeCemented = true;
    
    
    public RequestBlockCount() {
        super("block_count", ResponseBlockCount.class);
    }
    
}
