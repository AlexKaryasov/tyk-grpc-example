package com.testorg.testplugin;

import coprocess.DispatcherGrpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginServer {

    private static final Logger logger = Logger.getLogger(PluginServer.class.getName());
    static Server server;
    static int port = 5555;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Initializing gRPC server.");

        // Our dispatcher is instantiated and attached to the server:
        server = ServerBuilder.forPort(port)
                .addService(new PluginDispatcher())
                .build()
                .start();

        blockUntilShutdown();

    }

    static void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}

