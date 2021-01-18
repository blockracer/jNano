/*
 * Copyright (c) 2020 Karl Oczadly (karl@oczadly.uk)
 * Licensed under the MIT License
 */

package uk.oczadly.karl.jnano.model.block;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uk.oczadly.karl.jnano.internal.JNH;
import uk.oczadly.karl.jnano.internal.NanoConst;
import uk.oczadly.karl.jnano.model.HexData;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.block.interfaces.IBlockAccount;
import uk.oczadly.karl.jnano.model.block.interfaces.IBlockRepresentative;
import uk.oczadly.karl.jnano.model.block.interfaces.IBlockSource;
import uk.oczadly.karl.jnano.model.work.WorkSolution;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents an {@code open} block, and the associated data.
 *
 * <p>Note that this is a legacy block and has since been officially deprecated. For new blocks, use
 * {@link StateBlock state} blocks.</p>
 */
public class OpenBlock extends Block implements IBlockSource, IBlockAccount, IBlockRepresentative {
    
    /** A function which converts a {@link JsonObject} into a {@link OpenBlock} instance. */
    public static final Function<JsonObject, OpenBlock> DESERIALIZER = json -> new OpenBlock(
            (HexData)JNH.getJson(json, "signature", HexData::new),
            JNH.getJson(json, "work", WorkSolution::new),
            JNH.getJson(json, "source", HexData::new),
            JNH.getJson(json, "account", NanoAccount::parseAddress),
            JNH.getJson(json, "representative", NanoAccount::parseAddress));
    
    private static final BlockIntent INTENT = new BlockIntent(false, true, true, true, false, false);
    private static final BlockIntent INTENT_GENESIS = new BlockIntent(false, true, true, true, false, true);
    
    
    @Expose @SerializedName("source")
    private final HexData sourceBlockHash;
    
    @Expose @SerializedName("account")
    private final NanoAccount accountAddress;
    
    @Expose @SerializedName("representative")
    private final NanoAccount representativeAccount;
    
    
    @Deprecated(forRemoval = true)
    public OpenBlock(String signature, WorkSolution work, String sourceBlockHash, NanoAccount accountAddress,
                     NanoAccount representativeAccount) {
        this(new HexData(signature), work, new HexData(sourceBlockHash), accountAddress, representativeAccount);
    }
    
    public OpenBlock(HexData signature, WorkSolution work, HexData sourceBlockHash, NanoAccount accountAddress,
                     NanoAccount representativeAccount) {
        super(BlockType.OPEN, signature, work);
    
        if (sourceBlockHash == null) throw new IllegalArgumentException("Source block hash cannot be null.");
        if (!JNH.isValidLength(sourceBlockHash, NanoConst.LEN_HASH_B))
            throw new IllegalArgumentException("Source block hash is an invalid length.");
        if (accountAddress == null) throw new IllegalArgumentException("Block account cannot be null.");
        if (representativeAccount == null) throw new IllegalArgumentException("Block representative cannot be null.");
        
        this.sourceBlockHash = sourceBlockHash;
        this.accountAddress = accountAddress;
        this.representativeAccount = representativeAccount;
    }
    
    
    @Override
    public final HexData getSourceBlockHash() {
        return sourceBlockHash;
    }
    
    @Override
    public final NanoAccount getAccount() {
        return accountAddress;
    }
    
    @Override
    public final NanoAccount getRepresentative() {
        return representativeAccount;
    }
    
    @Override
    public BlockIntent getIntent() {
        if (getSourceBlockHash().toHexString().equals(getAccount().toPublicKey())
                && getAccount().equals(getRepresentative())) {
            return INTENT_GENESIS; // Genesis block special case
        }
        return INTENT;
    }
    
    @Override
    public boolean contentEquals(Block block) {
        if (!(block instanceof OpenBlock)) return false;
        OpenBlock ob = (OpenBlock)block;
        return super.contentEquals(ob)
                && Objects.equals(getAccount(), ob.getAccount())
                && Objects.equals(getRepresentative(), ob.getRepresentative())
                && Objects.equals(getSourceBlockHash(), ob.getSourceBlockHash());
    }
    
    @Override
    protected byte[] calculateHash() {
        return hashBlake2b(
                getSourceBlockHash().toByteArray(),
                getRepresentative().getPublicKeyBytes(),
                getAccount().getPublicKeyBytes());
    }
    
    
    /**
     * Parses an {@code open} block from a given JSON string using the default deserializer.
     * @param json the JSON data to parse from
     * @return a new {@link OpenBlock} constructed from the given JSON data
     * @throws BlockDeserializer.BlockParseException if the block cannot be correctly parsed
     * @see BlockDeserializer
     * @see Block#parse(String)
     */
    public static OpenBlock parse(String json) {
        return JNH.tryRethrow(Block.parse(json), b -> (OpenBlock)b,
                e -> new BlockDeserializer.BlockParseException("Block is not an open block.", e));
    }
    
    /**
     * Parses an {@code open} block from a given {@link JsonObject} instance using the default deserializer.
     * @param json the JSON data to parse from
     * @return a new {@link OpenBlock} constructed from the given JSON data
     * @throws BlockDeserializer.BlockParseException if the block cannot be correctly parsed
     * @see BlockDeserializer
     * @see Block#parse(JsonObject)
     */
    public static OpenBlock parse(JsonObject json) {
        return JNH.tryRethrow(Block.parse(json), b -> (OpenBlock)b,
                e -> new BlockDeserializer.BlockParseException("Block is not an open block.", e));
    }
    
}
