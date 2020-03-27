package uk.oczadly.karl.jnano.rpc;

import com.google.gson.*;
import uk.oczadly.karl.jnano.internal.JNanoHelper;
import uk.oczadly.karl.jnano.rpc.exception.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * <p>This class represents a connection to a specified Nano node endpoint, with the main purpose of sending and
 * queuing RPC requests.</p>
 * <p>To use this class, set the endpoint address, port and authorization token (if configured) in the constructor,
 * and then pass request arguments to one of the {@code processRequest()} methods. Asynchronous requests can also be
 * accomplished using one of the {@code processRequestAsync} methods, which can take a callback, as well as
 * returning a future object representing the response.</p>
 * <p>Below is an example of a synchronous query which creates a new account from a provided wallet ID:</p>
 * <pre>
 *  try {
 *      // Configure a connection to localhost:7076
 *      RpcQueryNode node = new RpcQueryNode();
 *
 *      // Construct the request (and pass query arguments)
 *      RequestAccountCreate request = new RequestAccountCreate(
 *              "B4ECF585D887B590907949C41F73BB11AA0BD4FD98563CC5D810EF26FAAD948E"); // Wallet ID
 *
 *      // Send request to the node synchronously and retrieve response
 *      ResponseAccount response = node.processRequest(request);
 *
 *      // Output new account
 *      System.out.println("New account: " + response.getAccountAddress());
 *  } catch (RpcException | IOException e) {
 *      e.printStackTrace();
 *  }
 * </pre>
 */
public class RpcQueryNode {
    
    protected static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    
    private final URL address;
    
    private volatile String authToken;
    
    
    /**
     * Constructs a new query node with the address {@code 127.0.0.1:7076}.
     *
     * @throws MalformedURLException if the address cannot be parsed
     */
    public RpcQueryNode() throws MalformedURLException {
        this((String)null);
    }
    
    /**
     * Constructs a new query node with the address {@code 127.0.0.1:7076} and specified authorization token.
     *
     * @param authToken the authorization token to be sent with queries
     * @throws MalformedURLException if the address cannot be parsed
     */
    public RpcQueryNode(String authToken) throws MalformedURLException {
        this("::1", 7076, authToken); // Local address and default port
    }
    
    /**
     * Constructs a new query node with the provided address and port.
     *
     * @param address the address of the node
     * @param port    the port which the node is listening on
     * @throws MalformedURLException if the address cannot be parsed
     */
    public RpcQueryNode(String address, int port) throws MalformedURLException {
        this(address, port, null);
    }
    
    /**
     * Constructs a new query node with the given address, port and authorization token.
     *
     * @param address   the address of the node
     * @param port      the port which the node is listening on
     * @param authToken the authorization token to be sent with queries
     * @throws MalformedURLException if the address cannot be parsed
     */
    public RpcQueryNode(String address, int port, String authToken) throws MalformedURLException {
        this(new URL("HTTP", address, port, ""), authToken);
    }
    
    /**
     * Constructs a new query node with the given address (as a URL).
     *
     * @param address the HTTP URL (address and port) which the node is listening on
     */
    public RpcQueryNode(URL address) {
        this(address, null);
    }
    
    /**
     * Constructs a new query node with the given address (as a URL) and authorization token.
     *
     * @param address   the HTTP URL (address and port) which the node is listening on
     * @param authToken the authorization token to be sent with queries
     */
    public RpcQueryNode(URL address, String authToken) {
        this.address = address;
        this.authToken = authToken;
    }
    
    
    /**
     * @return the address of this node's RPC listener
     */
    public final URL getAddress() {
        return this.address;
    }
    
    
    /**
     * @return the authorization token to be sent to the RPC server, or null if not configured
     */
    public final String getAuthToken() {
        return this.authToken;
    }
    
    /**
     * Sets the authorization token to be used with future requests.
     *
     * @param authToken the new token to be used for queries, or null to remove
     */
    public final void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    
    
    /**
     * Sends a query request to the node via RPC. This method will not timeout as long as the connection remains open.
     *
     * @param request the query request to send to the node
     * @return the successful reponse from the node
     *
     * @throws IOException  if an error occurs with the connection to the node
     * @throws RpcException if the node returns a non-successful response
     */
    public <Q extends RpcRequest<R>, R extends RpcResponse> R processRequest(Q request)
            throws IOException, RpcException {
        return this.processRequest(request, null);
    }
    
    /**
     * Sends a query request to the node via RPC.
     *
     * @param request the query request to send to the node
     * @param timeout the timeout for the request in milliseconds, or null for none
     * @return the successful reponse from the node
     *
     * @throws IOException  if an error occurs with the connection to the node
     * @throws RpcException if the node returns a non-successful response
     * @see <a href="https://github.com/koczadly/jNano/wiki/Query-requests#command-lookup-table">See the GitHub wiki
     * for a list of supported request operations.</a>
     */
    public <Q extends RpcRequest<R>, R extends RpcResponse> R processRequest(Q request, Integer timeout)
            throws IOException, RpcException {
        if (request == null)
            throw new IllegalArgumentException("Request argument must not be null.");
        if (timeout != null && timeout < 0)
            throw new IllegalArgumentException("Timeout period must be positive or null.");
        
        String requestJsonStr = this.serializeRequestToJSON(request); // Serialise the request into JSON
        return this.processRequestRaw(requestJsonStr, timeout, request.getResponseClass());
    }
    
    
    /**
     * Sends a query request to the node via RPC. The request will not timeout as long as the connection remains open.
     *
     * @param request the query request to send to the node
     * @return a future instance representing the response data/exception
     *
     * @see <a href="https://github.com/koczadly/jNano/wiki/Query-requests#command-lookup-table">See the GitHub wiki
     * for a list of supported request operations.</a>
     */
    public <Q extends RpcRequest<R>, R extends RpcResponse> Future<R> processRequestAsync(Q request) {
        return this.processRequestAsync(request, null, null);
    }
    
    /**
     * Sends a query request to the node via RPC.
     *
     * @param request the query request to send to the node
     * @param timeout the timeout for the request in milliseconds, or null for none
     * @return a future instance representing the response data/exception
     *
     * @see <a href="https://github.com/koczadly/jNano/wiki/Query-requests#command-lookup-table">See the GitHub wiki
     * for a list of supported request operations.</a>
     */
    public <Q extends RpcRequest<R>, R extends RpcResponse> Future<R> processRequestAsync(Q request, Integer timeout) {
        return this.processRequestAsync(request, timeout, null);
    }
    
    
    /**
     * Sends a query request to the node via RPC. The request will not timeout as long as the connection remains open.
     *
     * @param request  the query request to send to the node
     * @param callback the callback to execute after the request has completed (or null for no callback)
     * @return a future instance representing the response data/exception
     *
     * @see <a href="https://github.com/koczadly/jNano/wiki/Query-requests#command-lookup-table">See the GitHub wiki
     * for a list of supported request operations.</a>
     */
    public <Q extends RpcRequest<R>, R extends RpcResponse> Future<R> processRequestAsync(Q request,
                                                                                          QueryCallback<R> callback) {
        return this.processRequestAsync(request, null, callback);
    }
    
    /**
     * Sends a query request to the node via RPC.
     *
     * @param request  the query request to send to the node
     * @param timeout  the timeout for the request in milliseconds, or null for none
     * @param callback the callback to execute after the request has completed (or null for no callback)
     * @return a future instance representing the response data/exception
     *
     * @see <a href="https://github.com/koczadly/jNano/wiki/Query-requests#command-lookup-table">See the GitHub wiki
     * for a list of supported request operations.</a>
     */
    @SuppressWarnings("removal")
    public <Q extends RpcRequest<R>, R extends RpcResponse> Future<R> processRequestAsync(Q request, Integer timeout,
                                                                                          QueryCallback<R> callback) {
        if (request == null)
            throw new IllegalArgumentException("Request argument must not be null.");
        if (timeout != null && timeout < 0)
            throw new IllegalArgumentException("Timeout period must be positive or null.");
        
        return RpcQueryNode.EXECUTOR_SERVICE.submit(() -> {
            try {
                R response = RpcQueryNode.this.processRequest(request, timeout);
                if (callback != null) {
                    callback.onResponse(response);
                }
                return response;
            } catch (RpcException ex) {
                if (callback != null) {
                    callback.onRpcFailure(ex);
                    callback.onFailure(ex);
                }
                throw ex; // Re-throw for Future object
            } catch (IOException ex) {
                if (callback != null) {
                    callback.onIoFailure(ex);
                    callback.onFailure(ex);
                }
                throw ex; // Re-throw for Future object
            }
        });
    }
    
    
    /**
     * Sends a raw JSON query to the RPC server, and then returns an object in the specified class containing the
     * deserialized response data.
     *
     * @param jsonRequest   the JSON query to send to the node
     * @param timeout       the connection timeout in milliseconds, or null to disable timeouts
     * @param responseClass the class to deserialize the response data into
     * @return the response received from the node, contained in an object of the specified class
     *
     * @throws IOException  if an error occurs with the connection to the node
     * @throws RpcException if the node returns a non-successful response
     */
    public <R extends RpcResponse> R processRequestRaw(String jsonRequest, Integer timeout, Class<R> responseClass)
            throws IOException, RpcException {
        if (responseClass == null)
            throw new IllegalArgumentException("Response class argument cannot be null.");
        
        String responseJson = this.processRequestRaw(jsonRequest, timeout); // Send the request to the node
        return this.deserializeResponseFromJSON(responseJson, responseClass);
    }
    
    /**
     * <p>Sends a raw JSON query to the RPC server, and then returns the raw JSON response.</p>
     * <p>Note that this method will not deserialize the resulting JSON, or parse it for errors reported by the node.
     * You will need to implement this functionality yourself, or use the alternate {@link #processRequestRaw(String,
     * Integer, Class)} method.</p>
     *
     * @param jsonRequest the JSON query to send to the node
     * @param timeout     the connection timeout in milliseconds, or null to disable timeouts
     * @return the JSON response received from the node
     *
     * @throws IOException if an error occurs with the connection to the node
     */
    public String processRequestRaw(String jsonRequest, Integer timeout) throws IOException {
        if (jsonRequest == null)
            throw new IllegalArgumentException("JSON request string cannot be null.");
        
        // Open connection
        HttpURLConnection con = (HttpURLConnection)this.address.openConnection();
        
        // Configure connection
        if (timeout != null) { // Set timeouts
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);
        }
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestMethod("POST");
        
        // Set authorization token header (if set)
        if (this.authToken != null) {
            con.setRequestProperty("Authorization", "Bearer " + this.authToken);
        }
        
        // Write request data
        OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
        writer.write(jsonRequest);
        writer.close();
        
        // Read response data
        InputStreamReader input = new InputStreamReader(con.getInputStream());
        BufferedReader inputReader = new BufferedReader(input);
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = inputReader.readLine()) != null) {
            response.append(line);
        }
        inputReader.close();
        
        return response.toString();
    }
    
    
    /**
     * Converts a pure JSON string into a response instance.
     *
     * @param responseJson  the JSON to deserialize
     * @param responseClass the response class to deserialize into
     * @return the deserialized response instance
     */
    protected <R extends RpcResponse> R deserializeResponseFromJSON(String responseJson, Class<R> responseClass)
            throws RpcException {
        JsonObject response;
        try {
            response = JsonParser.parseString(responseJson).getAsJsonObject(); // Parse response
        } catch (JsonSyntaxException ex) {
            throw new RpcInvalidResponseException(responseJson, ex); // If unable to parse
        }
        
        // Check for returned RPC error
        JsonElement responseError = response.get("error");
        if (responseError != null)
            throw this.parseException(responseError.getAsString());
        
        // Deserialize response
        R responseObj = JNanoHelper.GSON.fromJson(responseJson, responseClass); // Deserialize from JSON
        responseObj.initResponseObject(response); // Initialise raw parameters
        return responseObj;
    }
    
    
    /**
     * Parses a returned error string into the appropriate RpcException type.
     *
     * @param message the returned error message from the node
     * @return the matching exception to be thrown
     */
    protected RpcException parseException(String message) {
        String msgLc = message.toLowerCase();
        
        switch (msgLc) {
            case "wallet is locked":
            case "wallet locked":
                return new RpcWalletLockedException();                  // Wallet locked
            case "insufficient balance":
                return new RpcInvalidArgumentException(message + ".");  // Invalid/bad argument
            case "invalid authorization header":
                return new RpcInvalidAuthTokenException();              // Invalid auth token
            case "rpc control is disabled":
                return new RpcControlDisabledException();               // RPC control disabled
            case "unable to parse json":
                return new RpcInvalidRequestJsonException();            // Invalid request body
            case "unknown command":
                return new RpcUnknownCommandException();                // Unknown command
        }
        
        if (msgLc.startsWith("bad") || msgLc.startsWith("invalid") || msgLc.endsWith("invalid")
                || msgLc.endsWith("required")) {
            return new RpcInvalidArgumentException(message + ".");    // Invalid/bad argument
        } else if (msgLc.contains("not found")) {
            return new RpcEntityNotFoundException(message + ".");     // Unknown referenced entity
        } else if (msgLc.endsWith("is disabled")) {
            return new RpcFeatureDisabledException(message + ".");    // Feature is disabled
        } else if (msgLc.startsWith("internal")) {
            return new RpcInternalException(message + ".");           // Internal server error
        }
        
        return new RpcException(message.isEmpty() ? null : (message + ".")); // Default to base exception
    }
    
    
    /**
     * Converts a request instance into a pure JSON string.
     *
     * @param req the request to serialize
     * @return the serialized JSON command
     */
    public String serializeRequestToJSON(RpcRequest<?> req) {
        if (req == null)
            throw new IllegalArgumentException("Query request argument cannot be null.");
        
        return JNanoHelper.GSON.toJson(req);
    }
    
    
    /**
     * @return the Gson utility class used by this instance
     *
     * @deprecated moved to use internal static utility
     */
    @Deprecated(forRemoval = true)
    public final Gson getGsonInstance() {
        return JNanoHelper.GSON;
    }
    
}
