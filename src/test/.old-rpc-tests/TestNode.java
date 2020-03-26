package uk.oczadly.karl.jnano.rpc;

import uk.oczadly.karl.jnano.rpc.exception.RpcException;
import uk.oczadly.karl.jnano.rpc.request.RpcRequest;
import uk.oczadly.karl.jnano.rpc.response.RpcResponse;
import uk.oczadly.karl.jnano.tests.Configuration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

public class TestNode extends RpcQueryNode {
    
    public TestNode() throws MalformedURLException {
        super(Configuration.NODE_IP, Configuration.NODE_PORT);
    }
    
    @Override
    public String processRawRequest(String jsonRequest, HttpURLConnection con) throws IOException {
        System.out.println("Processing query: " + jsonRequest);
        String response = super.processRequestRaw(jsonRequest, con);
        System.out.println("Response: " + response);
        return response;
    }
    
    @Override
    public <Q extends RpcRequest<R>, R extends RpcResponse> R processRequest(Q request) throws IOException, RpcException {
        return super.processRequest(request);
    }
    
}
