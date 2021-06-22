package com.testorg.testplugin;

import coprocess.DispatcherGrpc;
import coprocess.CoprocessMiniRequestObject;
import coprocess.CoprocessObject;

import io.grpc.StatusRuntimeException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class PluginClient {
    private static final Logger logger = Logger.getLogger(PluginClient.class.getName());

    private final ManagedChannel channel;
    private final DispatcherGrpc.DispatcherBlockingStub blockingStub;

    public PluginClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build());
    }

    PluginClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = DispatcherGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public CoprocessObject.Object dispatchRequest(CoprocessObject.Object req) {
        CoprocessObject.Object modifiedReq = null;
        try {
            modifiedReq = blockingStub.dispatch(req);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return modifiedReq;
        }
        return modifiedReq;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Initializing gRPC client");
        PluginClient client = new PluginClient("localhost", 5555);

        // The mini request object definition is found here: https://github.com/TykTechnologies/tyk-protobuf/blob/master/proto/coprocess_mini_request_object.proto
        CoprocessMiniRequestObject.MiniRequestObject miniRequestObj = CoprocessMiniRequestObject.MiniRequestObject.newBuilder()
                .putHeaders("Authorization", "token")
                .build();

        // Initialize a CP object using the appropriate hook name and the mini request object built earlier:
        CoprocessObject.Object req = CoprocessObject.Object.newBuilder()
                .setHookName("MyPreMiddleware")
                .setRequest(miniRequestObj)
                .build();

        logger.log(Level.INFO, "Dispatching request:" + req.toString());

        try {
            CoprocessObject.Object modifiedReq = client.dispatchRequest(req);
            logger.log(Level.INFO, "Got request:" + modifiedReq.toString());
        } finally {
            client.shutdown();
        }
    }
}
