package org.silnith.example.renderer;

import org.silnith.example.grpc.tessellation.Polygon;
import org.silnith.example.grpc.tessellation.TessellationServiceGrpc;
import org.silnith.example.grpc.tessellation.Vertex;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class Main {

	private static final class RenderingResponseObserver implements StreamObserver<Polygon> {
		
		private final Renderer renderer;
		
		public RenderingResponseObserver(final Renderer renderer) {
			super();
			this.renderer = renderer;
		}
		
		@Override
		public void onNext(final Polygon value) {
			if (value.getVertexCount() != 3) {
				throw new IllegalArgumentException("Polygon is not a triangle.");
			}
			renderer.render(value.getVertex(0), value.getVertex(1), value.getVertex(2));
		}
		
		@Override
		public void onError(final Throwable t) {
			;
		}
		
		@Override
		public void onCompleted() {
			;
		}
		
	}

	public static void main(final String[] args) {
		final ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 5678)
				.directExecutor()
				.usePlaintext()
				.build();
		final TessellationServiceGrpc.TessellationServiceStub stub = TessellationServiceGrpc.newStub(managedChannel);
		
		final StreamObserver<Polygon> responseObserver = new RenderingResponseObserver(new Renderer());
		final StreamObserver<Polygon> requestObserver = stub.tessellate(responseObserver);
		
		final Polygon polygon = Polygon.newBuilder()
				.addVertex(Vertex.newBuilder()
						.setX(0)
						.setY(0)
						.setZ(0)
						.build())
				.addVertex(Vertex.newBuilder()
						.setX(1)
						.setY(0)
						.setZ(0)
						.build())
				.addVertex(Vertex.newBuilder()
						.setX(1)
						.setY(1)
						.setZ(0)
						.build())
				.addVertex(Vertex.newBuilder()
						.setX(0)
						.setY(1)
						.setZ(0)
						.build())
				.build();
		
		requestObserver.onNext(polygon);
		requestObserver.onCompleted();
	}

}
