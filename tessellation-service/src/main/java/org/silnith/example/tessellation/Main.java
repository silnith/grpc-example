package org.silnith.example.tessellation;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		final Server server = ServerBuilder.forPort(5678)
				.addService(new TessellationServiceImpl())
				.directExecutor()
				.build();
		server.start();
		server.shutdown();
		server.awaitTermination();
	}

}
