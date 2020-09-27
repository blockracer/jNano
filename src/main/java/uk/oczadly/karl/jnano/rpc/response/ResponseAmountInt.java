/*
 * Copyright (c) 2020 Karl Oczadly (karl@oczadly.uk)
 * Licensed under the MIT License
 */

package uk.oczadly.karl.jnano.rpc.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import uk.oczadly.karl.jnano.internal.gsonadapters.SingleValueJsonAdapter;

import java.math.BigInteger;


/**
 * This response class contains a single amount of Nano.
 */
@JsonAdapter(SingleValueJsonAdapter.class)
public class ResponseAmountInt extends RpcResponse {
    
    @Expose
    private BigInteger amount;
    
    
    /**
     * @return the amount of Nano in RAW
     */
    public BigInteger getAmount() {
        return amount;
    }
    
}