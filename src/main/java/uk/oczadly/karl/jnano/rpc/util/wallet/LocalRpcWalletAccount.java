/*
 * Copyright (c) 2021 Karl Oczadly (karl@oczadly.uk)
 * Licensed under the MIT License
 */

package uk.oczadly.karl.jnano.rpc.util.wallet;

import uk.oczadly.karl.jnano.model.HexData;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.jnano.model.block.Block;
import uk.oczadly.karl.jnano.model.block.StateBlockBuilder;
import uk.oczadly.karl.jnano.rpc.RpcQueryNode;
import uk.oczadly.karl.jnano.rpc.exception.RpcEntityNotFoundException;
import uk.oczadly.karl.jnano.rpc.exception.RpcException;
import uk.oczadly.karl.jnano.rpc.exception.RpcExternalException;
import uk.oczadly.karl.jnano.rpc.request.node.RequestAccountInfo;
import uk.oczadly.karl.jnano.rpc.request.node.RequestBlockInfo;
import uk.oczadly.karl.jnano.rpc.request.node.RequestPending;
import uk.oczadly.karl.jnano.rpc.request.node.RequestProcess;
import uk.oczadly.karl.jnano.rpc.response.ResponseAccountInfo;
import uk.oczadly.karl.jnano.rpc.response.ResponseBlockInfo;
import uk.oczadly.karl.jnano.rpc.response.ResponsePending;
import uk.oczadly.karl.jnano.util.blockproducer.AccountState;
import uk.oczadly.karl.jnano.util.blockproducer.BlockProducer;
import uk.oczadly.karl.jnano.util.blockproducer.LocalWalletAccount;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * This class provides a set of methods for performing wallet actions on an account, without sending the private key
 * to an external RPC node.
 *
 * <p>All of the methods in this class are secure, and will never send or expose any private keys to the RPC server.
 * This should be used when connecting via a third-party RPC provider. The class is also thread safe, though use as a
 * single-threaded execution should be preferred.</p>
 *
 * <p>Due to the asynchronous nature of Nano, you should not use multiple instances representing the same account, nor
 * should you use the same account on another wallet, node or system at the same time — doing so can result in
 * unexpected transaction failures.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Construct a block producer object with your configuration
 * BlockProducer blockProducer = new StateBlockProducer(
 *         BlockProducerSpecification.builder()
 *                 .defaultRepresentative("nano_3caprkc56ebsaakn4j4n7g9p8h358mycfjcyzkrfw1nai6prbyk8ihc5yjjk")
 *                 .workGenerator(new OpenCLWorkGenerator())
 *                 .build()
 * );
 *
 * // Create account using private key
 * LocalRpcWalletAccount account = new LocalRpcWalletAccount(
 *         new HexData("183A1DEDCA9CD37029456C8A2ED31460A0E9A8D18032676010AC11B02A442417"), // Private key
 *         RpcServiceProviders.nanex(), // Use nanex.cc public RPC API
 *         blockProducer); // Using our BlockProducer defined above
 *
 * // Print account info
 * System.out.printf("Using account address %s%n", account.getAccount());
 * System.out.printf("Balance: %s%n", account.getBalance());
 *
 * // Receive all pending funds
 * System.out.printf("Received %,d blocks%n", account.receiveAll().size());
 *
 * // Send funds to another account
 * System.out.printf("Send block hash: %s%n", account.send(
 *         NanoAccount.parseAddress("nano_34prihdxwz3u4ps8qjnn14p7ujyewkoxkwyxm3u665it8rg5rdqw84qrypzk"),
 *         NanoAmount.valueOfNano("0.0001"))
 *         .getHash());
 * }</pre>
 */
public class LocalRpcWalletAccount {
    
    private static final NanoAmount DEFAULT_THRESHOLD = NanoAmount.valueOfRaw("1000000000000000000000000");
    private static final int RECEIVE_BATCH_SIZE = 15;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    private final RpcQueryNode rpcClient;
    private final LocalWalletAccount account;
    private final Lock lock = new ReentrantLock(true);
    private volatile boolean hasRetrievedState = false;
    
    
    /**
     * Constructs a new local RPC wallet account.
     * @param privateKey    the private key of the account
     * @param rpcClient     the RPC client where requests will be sent to
     * @param blockProducer the block producer
     */
    public LocalRpcWalletAccount(HexData privateKey, RpcQueryNode rpcClient, BlockProducer blockProducer) {
        if (privateKey == null) throw new IllegalArgumentException("Private key cannot be null.");
        if (rpcClient == null) throw new IllegalArgumentException("RPC client cannot be null.");
        if (blockProducer == null) throw new IllegalArgumentException("BlockProducer cannot be null.");
        this.rpcClient = rpcClient;
        this.account = new LocalWalletAccount(privateKey, blockProducer);
    }
    
    
    /**
     * Returns the account which this wallet represents.
     * @return the account of this wallet
     */
    public final NanoAccount getAccount() {
        return account.getAccount();
    }
    
    /**
     * Returns the secret private key of the account.
     * @return the private key
     */
    public final HexData getPrivateKey() {
        return account.getPrivateKey();
    }
    
    /**
     * Returns the block producer which constructs blocks for this account.
     * @return the block producer object
     */
    public final BlockProducer getBlockProducer() {
        return account.getBlockProducer();
    }
    
    /**
     * Returns the RPC client which will execute the remote wallet operations.
     * @return the RPC client
     */
    public final RpcQueryNode getRpcClient() {
        return rpcClient;
    }
    
    @Override
    public String toString() {
        return "LocalRpcWalletAccount{" + getAccount() + '}';
    }
    
    
    /**
     * Forcefully refreshes the internal cached state of the account by calling the {@link RequestAccountInfo} RPC
     * query.
     *
     * <p>Most implementations should never need to call this method, as the state will automatically be retrieved when
     * necessary through the other action methods.</p>
     *
     * @throws WalletActionException if an error occurs with the RPC query
     */
    public void refreshState() throws WalletActionException {
        lock.lock();
        try {
            // Retrieve state from RPC
            ResponseAccountInfo info = rpcClient.processRequest(new RequestAccountInfo(getAccount().toAddress()));
            AccountState state = AccountState.fromAccountInfo(info);
            account.updateState(state);
            hasRetrievedState = true;
        } catch (RpcEntityNotFoundException e) {
            // Account isn't open
            account.updateState(AccountState.UNOPENED);
            hasRetrievedState = true;
        } catch (RpcException e) {
            throw new WalletActionException("Couldn't retrieve account state information.", e);
        } catch (IOException e) {
            throw new WalletActionException("Connection error with RPC client.", e);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Returns the current balance of this account, not include any pending amounts. Will return a value of zero if the
     * account hasn't been opened yet.
     *
     * <p>Note that this balance may include unconfirmed amounts. This shouldn't be a problem, as only people with
     * access to this account's private key can reverse these transactions.</p>
     *
     * @return the current balance of the account
     * @throws WalletActionException if an error occurs with the RPC query when retrieving the account state
     */
    public NanoAmount getBalance() throws WalletActionException {
        return initState().getBalance();
    }
    
    /**
     * Returns the hash of the current frontier block of this account, or an empty value if the account hasn't been
     * opened yet.
     *
     * @return the current account frontier block hash
     * @throws WalletActionException if an error occurs with the RPC query when retrieving the account state
     */
    public Optional<HexData> getFrontierHash() throws WalletActionException {
        return Optional.ofNullable(initState().getFrontierHash());
    }
    
    
    /**
     * Attempts to send an amount of funds to the specified account.
     *
     * <p>Calling this method will construct and sign a new block, generate the appropriate work for it, and publish the
     * block to the network via RPC.</p>
     *
     * @param destination the destination account
     * @param amount      the amount of funds to send to the account
     * @return the generated and published {@code send} block
     * @throws WalletActionException if an error occurs with the RPC query, work generation, block processing, or if
     *                               there are not enough funds available in the account
     */
    public Block send(NanoAccount destination, NanoAmount amount) throws WalletActionException {
        return processBlock(() -> account.createSend(destination, amount));
    }
    
    /**
     * Attempts to send <em>all</em> of the balance to the specified account, returning an empty value if there are
     * no remaining funds to send.
     *
     * <p>This method will not receive/send any pending blocks; that can be done by calling {@link #receiveAll()}
     * prior to sending funds.</p>
     *
     * <p>Calling this method will construct and sign a new block, generate the appropriate work for it, and publish the
     * block to the network via RPC.</p>
     *
     * @param destination the destination account
     * @return the generated and published {@code send} block, or empty if the account has no funds to send
     * @throws WalletActionException if an error occurs with the RPC query, work generation or block processing
     */
    public Optional<Block> sendAll(NanoAccount destination) throws WalletActionException {
        return processBlockOptional(() -> account.createSendAll(destination));
    }
    
    /**
     * Attempts to receive the specified pending block.
     *
     * <p>Calling this method will construct and sign a new block, generate the appropriate work for it, and publish the
     * block to the network via RPC.</p>
     *
     * @param sourceHash the hash of the pending {@code send} block
     * @return the generated and published {@code receive} block
     * @throws WalletActionException if an error occurs with the RPC queries, work generation, block processing, or the
     *                               specified block could not be found in the ledger
     */
    public Block receive(HexData sourceHash) throws WalletActionException {
        lock.lock();
        try {
            ResponseBlockInfo pendingBlockInfo;
            try {
                pendingBlockInfo = rpcClient.processRequest(new RequestBlockInfo(sourceHash.toHexString()));
            } catch (IOException e) {
                throw new WalletActionException("Connection error with RPC client.", e);
            } catch (RpcException e) {
                throw new WalletActionException("Couldn't retrieve pending block info.", e);
            }
            if (pendingBlockInfo.getBalance() == null) {
                throw new WalletActionException("Specified block is not a send block.");
            }
            return receive(sourceHash, pendingBlockInfo.getAmount());
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Attempts to receive a batch of pending blocks with at least {@code 0.000001 NANO} in value.
     *
     * <p>Calling this method will construct and sign a set of new blocks, generate the appropriate work for them, and
     * publish the blocks to the network via RPC.</p>
     *
     * @param count the maximum number of blocks to receive in this batch
     * @return a set containing the generated and published {@code receive} blocks
     * @throws WalletActionException if an error occurs with the RPC queries, work generation or block processing
     */
    public Set<Block> receiveBatch(int count) throws WalletActionException {
        return receiveBatch(count, DEFAULT_THRESHOLD);
    }
    
    /**
     * Attempts to receive a batch of pending blocks with a value greater than or equal to the specified threshold
     * amount.
     *
     * <p>Calling this method will construct and sign a set of new blocks, generate the appropriate work for them, and
     * publish the blocks to the network via RPC.</p>
     *
     * @param count     the maximum number of blocks to receive in this batch
     * @param threshold the minimum amount threshold
     * @return a set containing the generated and published {@code receive} blocks
     * @throws WalletActionException if an error occurs with the RPC queries, work generation or block processing
     */
    public Set<Block> receiveBatch(int count, NanoAmount threshold) throws WalletActionException {
        lock.lock();
        try {
            // Fetch pending blocks
            ResponsePending pending;
            try {
                pending = rpcClient.processRequest(new RequestPending(
                        getAccount().toAddress(), count, threshold.getAsRaw(), false, true, true));
            } catch (RpcException e) {
                throw new WalletActionException("Couldn't retrieve pending blocks list.", e);
            } catch (IOException e) {
                throw new WalletActionException("Connection error with RPC client.", e);
            }
            // Receive blocks
            Set<Block> published = new HashSet<>();
            for (Map.Entry<HexData, ResponsePending.PendingBlock> block : pending.getPendingBlocks().entrySet()) {
                published.add(receive(block.getKey(), block.getValue().getAmount()));
            }
            return published;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Attempts to receive all pending blocks with at least {@code 0.000001 NANO} in value.
     *
     * <p>Calling this method will construct and sign a set of new blocks, generate the appropriate work for them, and
     * publish the blocks to the network via RPC.</p>
     *
     * <p>Note: if a large amount of transactions are pending, or an attacker continues to send funds to this
     * account, this method may block and continue indefinitely. Receive operations are performed in small batches to
     * allow other operations to proceed between each batch.</p>
     *
     * @return a set containing the generated and published {@code receive} blocks
     * @throws WalletActionException if an error occurs with the RPC queries, work generation or block processing
     */
    public Set<Block> receiveAll() throws WalletActionException {
        return receiveAll(DEFAULT_THRESHOLD);
    }
    
    /**
     * Attempts to receive all pending blocks with a value greater than or equal to the specified threshold amount.
     *
     * <p>Calling this method will construct and sign a set of new blocks, generate the appropriate work for them, and
     * publish the blocks to the network via RPC.</p>
     *
     * <p>Note: if a large amount of transactions are pending, or an attacker continues to send funds to this
     * account, this method may block and continue indefinitely. Receive operations are performed in small batches to
     * allow other operations to proceed between each batch.</p>
     *
     * @param threshold the minimum amount threshold
     * @return a set containing the generated and published {@code receive} blocks
     * @throws WalletActionException if an error occurs with the RPC queries, work generation or block processing
     */
    public Set<Block> receiveAll(NanoAmount threshold) throws WalletActionException {
        Set<Block> batch, published = new HashSet<>();
        do {
            batch = receiveBatch(RECEIVE_BATCH_SIZE, threshold);
            published.addAll(batch);
        } while (!batch.isEmpty());
        return published;
    }
    
    private Block receive(HexData sourceHash, NanoAmount amount) throws WalletActionException {
        return processBlock(() -> account.createReceive(sourceHash, amount));
    }
    
    /**
     * Changes the representative of the account to the specified representative address.
     *
     * <p>Calling this method will construct and sign a new block, generate the appropriate work for it, and publish the
     * block to the network via RPC.</p>
     *
     * @param representative the new representative
     * @return the generated and published representative change block, or empty if the representative is already set
     *         to the specified account
     * @throws WalletActionException if an error occurs with the RPC query, work generation or block processing
     */
    public Optional<Block> changeRepresentative(NanoAccount representative) throws WalletActionException {
        return processBlockOptional(() -> account.createChangeRepresentative(representative));
    }
    
    
    private Optional<Block> processBlockOptional(Supplier<Optional<Block>> blockSupplier) throws WalletActionException {
        return Optional.ofNullable(processBlock(() -> blockSupplier.get().orElse(null)));
    }
    
    private Block processBlock(Supplier<Block> blockSupplier) throws WalletActionException {
        lock.lock();
        try {
            initState();
            for (int attempt = 0; attempt < MAX_RETRY_ATTEMPTS; attempt++) {
                try {
                    // Create block
                    Block block = blockSupplier.get();
                    if (block == null) return null;
                    
                    // Publish block to network
                    rpcClient.processRequest(new RequestProcess(block, false, false));
                    return block;
                } catch (RpcExternalException e) {
                    if (e.getRawMessage().equals("Fork") || e.getRawMessage().equals("Gap previous block")) {
                        refreshState(); // Refresh state if invalid 'previous' field and retry
                    } else {
                        throw e;
                    }
                } catch (BlockProducer.BlockCreationException e) {
                    throw new WalletActionException(e.getMessage(), e);
                }
            }
            throw new WalletActionException("Previous block was incorrect, retried too many times. " +
                    "Is the account being concurrently used elsewhere?");
        } catch (StateBlockBuilder.BlockCreationException e) {
            throw new WalletActionException("Couldn't construct block.", e);
        } catch (IOException e) {
            throw new WalletActionException("Connection error with RPC client.", e);
        } catch (RpcException e) {
            throw new WalletActionException("Couldn't publish block.", e);
        } finally {
            lock.unlock();
        }
    }
    
    private AccountState initState() throws WalletActionException {
        if (!hasRetrievedState) {
            lock.lock();
            try {
                if (!hasRetrievedState)
                    refreshState();
            } finally {
                lock.unlock();
            }
        }
        return account.getState();
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocalRpcWalletAccount)) return false;
        LocalRpcWalletAccount that = (LocalRpcWalletAccount)o;
        return Objects.equals(rpcClient, that.rpcClient) && Objects.equals(getBlockProducer(), that.getBlockProducer())
                && Objects.equals(getPrivateKey(), that.getPrivateKey());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getPrivateKey());
    }
    
}
