package org.silnith.example.tessellation;

import java.util.Iterator;
import java.util.List;

import org.silnith.example.grpc.tessellation.Polygon;
import org.silnith.example.grpc.tessellation.TessellationServiceGrpc;
import org.silnith.example.grpc.tessellation.Vertex;

import io.grpc.stub.StreamObserver;

public class TessellationServiceImpl extends TessellationServiceGrpc.TessellationServiceImplBase {

    @Override
    public StreamObserver<Polygon> tessellate(final StreamObserver<Polygon> responseObserver) {
        return new StreamObserver<Polygon>() {

            @Override
            public void onNext(final Polygon value) {
                final List<Vertex> vertexList = value.getVertexList();
                final Iterator<Vertex> iterator = vertexList.iterator();
                if (!iterator.hasNext()) {
                    responseObserver.onError(new IllegalArgumentException("Polygon has no vertices."));
                }
                final Vertex firstVertex = iterator.next();
                if (!iterator.hasNext()) {
                    responseObserver.onError(new IllegalArgumentException("Polygon only has one vertex."));
                }
                Vertex secondVertex = iterator.next();
                if (!iterator.hasNext()) {
                    responseObserver.onError(new IllegalArgumentException("Polygon only has two vertices."));
                }
                do {
                    final Vertex thirdVertext = iterator.next();
                    final Polygon triangle = Polygon.newBuilder()
                            .addVertex(firstVertex)
                            .addVertex(secondVertex)
                            .addVertex(thirdVertext)
                            .build();
                    responseObserver.onNext(triangle);
                    secondVertex = thirdVertext;
                } while (iterator.hasNext());
            }

            @Override
            public void onError(final Throwable t) {
                // log error or do something else equally useless
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

}
