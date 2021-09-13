# gRPC Example

<https://www.linkedin.com/pulse/grpc-leaky-abstractions-kent-rosenkoetter/>

Over time I have found that most APIs, libraries, and frameworks fall broadly into two general groups.

1. Those that initially seem intimidating, but the more you learn about it and the problem domain it addresses the more you realize that the people who designed it had an in-depth understanding that far surpasses your own. The longer you work with it, the more your respect grows.
1. Those that initially seem promising, but the more you try to use it the more you find discrepancies and problematic use-cases that make you question whether the designers truly understood the problem domain they set out to tackle. The longer you work with it, the more your discontent grows.

The Platonic ideal of the first group is IRIS GL / OpenGL. That API so cleanly and thoroughly described and modeled the 3D graphics pipeline that one single API, essentially unchanged, managed to provide a consistent programming interface for **decades** of generations of new graphics hardware. It started with the custom high-end hardware created by SGI but also worked cleanly on the lowest software renderers, and it accurately modeled a torrent of new generations of graphics hardware in a time when video card manufacturers were producing new video cards every six months. To be able to create an API that continued to work seamlessly for twenty years is nothing short of astounding. The only thing that really changed the foundation upon which it was built was the advent of fully-programmable graphics pipelines in the 2000s, prompting the creation of OpenGL 2.0 and GLSL.

I will never forget the moment when I asked my graduate school professor why texturing is done before depth testing. He reminded me of depth textures. At that moment the thought of early depth testing was overshadowed by the myriad possibilities of using textures to manipulate depth values, and I was awed by just how deeply these people understood the domain and all its intricacies.

Did you know that every API function in OpenGL specifies whether it is client or server, and precisely when it binds data? That is because the original SGI graphics supercomputer were often centralized, with one machine serving up many remote display stations. Basically, remote rendering is built right into OpenGL itself, long before the advent of current screen casting and video streaming. In college I would run my little chess demo on a machine in the computer lab across campus, while displaying it right on the monitor in my dorm room in its full 3D glory. This was in the last century, folks. Before Y2K.

Java is another technology that falls into the first category. Initially, we all mocked it. "You have to compile your code so that the JVM can interpret it? How stupid!" "Why is the byte-code stack-based when all modern machines are register-based?" The list goes on and on. However, after working with it for many years I've come to understand just how brilliant some of those decisions truly were. Byte-code was never intended to be executed directly. The plan was always to JIT compile it to native code. Knowing this, stack-based byte-code is drastically easier to analyze and optimize than register-based byte-code, meaning the JIT compiler can be fast enough to be run constantly and repeatedly while the program itself is executing. Things like [liveness analysis](<https://en.wikipedia.org/wiki/Live_variable_analysis>) are expensive to compute for registers, but in a stack machine it is trivial. (And this really makes one wonder why Google chose a register-based byte-code for its Android system. I suspect it was so they could advertise that Android ran Java while also refusing to pay Sun for Java ME licensing.) And a simple stack-based VM can be trivially implemented on embedded systems while still allowing for obscenely complicated optimization opportunities on more powerful hardware.

As for things in the second category, I've already written a couple articles about such examples.

Which brings me to gRPC. My [previous article](<https://www.linkedin.com/pulse/does-protobuf-actually-support-streaming-kent-rosenkoetter/>) points out how gRPC's much-advertised "streaming capability" is false advertising. Here I am going to analyze the actual implementation of an example gRPC service. Let's start with a trivial service definition.

```grpc
syntax = "proto3";

package tessellation;
option java_package = "org.silnith.example.grpc.tessellation";
option java_multiple_files = true;

/*
 * A single coordinate in 3-space.
 */
message Vertex {
  double x = 1;
  double y = 2;
  double z = 3;
}

/*
 * A polygon with an arbitrary number of sides.
 */
message Polygon {
    repeated Vertex vertex = 1;
}

/*
 * A service that provides tessellation for 3D models.
 */
service TessellationService {
    /*
     * Tessellates a series of polygons into a series of triangles suitable for rendering.
     */
    rpc Tessellate(stream Polygon) returns (stream Polygon);
}
```

This part is simple enough. The input polygons can have any number of sides (greater than two), and the response polygons will all have exactly three sides. The service does nothing to correct for non-planar polygons.

Next we generate the interface code and implement a client and a server. I open a new class in my IDE and start with the generated `Polygon` class. The constructor does not auto-complete. What is the interface of this class?

```java
package org.silnith.example.grpc.tessellation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Descriptors;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import com.google.protobuf.RepeatedFieldBuilderV3;
import com.google.protobuf.UnknownFieldSet;

/**
 * <pre>
 *
 * A polygon with an arbitrary number of sides.
 * </pre>
 *
 * Protobuf type {@code tessellation.Polygon}
 */
public final class Polygon extends GeneratedMessageV3 implements PolygonOrBuilder {
    public static final int VERTEX_FIELD_NUMBER = 1;

    /**
     * <pre>
     *
     * A polygon with an arbitrary number of sides.
     * </pre>
     *
     * Protobuf type {@code tessellation.Polygon}
     */
    public static final class Builder extends GeneratedMessageV3.Builder<Builder> implements PolygonOrBuilder {
        public static Descriptors.Descriptor getDescriptor();

        public Builder clear();

        public Descriptors.Descriptor getDescriptorForType();

        public Polygon getDefaultInstanceForType();

        public Polygon build();

        public Polygon buildPartial();

        public Builder clone();

        public Builder setField(final Descriptors.FieldDescriptor field, final Object value);

        public Builder clearField(final Descriptors.FieldDescriptor field);

        public Builder clearOneof(final Descriptors.OneofDescriptor oneof);

        public Builder setRepeatedField(final Descriptors.FieldDescriptor field, final int index, final Object value);

        public Builder addRepeatedField(final Descriptors.FieldDescriptor field, final Object value);

        public Builder mergeFrom(final Message other);

        public Builder mergeFrom(final Polygon other);

        public boolean isInitialized();

        public Builder mergeFrom(final CodedInputStream input, final ExtensionRegistryLite extensionRegistry) throws IOException;

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public List<Vertex> getVertexList();

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public int getVertexCount();

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public Vertex getVertex(final int index);

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public Builder setVertex(final int index, final Vertex value);

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public Builder setVertex(final int index, final Vertex.Builder builderForValue);

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public Builder addVertex(final Vertex value);

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public Builder addVertex(final int index, final Vertex value);

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public Builder addVertex(final Vertex.Builder builderForValue);

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public Builder addVertex(final int index, final Vertex.Builder builderForValue);

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public Builder addAllVertex(final java.lang.Iterable<? extends Vertex> values);

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public Builder clearVertex();

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public Builder removeVertex(final int index);

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public Vertex.Builder getVertexBuilder(final int index);

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public VertexOrBuilder getVertexOrBuilder(final int index);

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public List<? extends VertexOrBuilder> getVertexOrBuilderList();

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public Vertex.Builder addVertexBuilder();

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public Vertex.Builder addVertexBuilder(final int index);

        /**
         * <code>repeated .tessellation.Vertex vertex = 1;</code>
         */
        public List<Vertex.Builder> getVertexBuilderList();

        public Builder setUnknownFields(final UnknownFieldSet unknownFields);

        public Builder mergeUnknownFields(final UnknownFieldSet unknownFields);

        protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable();

        public Map<Descriptors.FieldDescriptor, Object> getAllFields();

        public Message.Builder newBuilderForField(Descriptors.FieldDescriptor field);

        public Message.Builder getFieldBuilder(Descriptors.FieldDescriptor field);

        public Message.Builder getRepeatedFieldBuilder(Descriptors.FieldDescriptor field, int index);

        public boolean hasOneof(Descriptors.OneofDescriptor oneof);

        public Descriptors.FieldDescriptor getOneofFieldDescriptor(Descriptors.OneofDescriptor oneof);

        public boolean hasField(Descriptors.FieldDescriptor field);

        public Object getField(Descriptors.FieldDescriptor field);

        public int getRepeatedFieldCount(Descriptors.FieldDescriptor field);

        public Object getRepeatedField(Descriptors.FieldDescriptor field, int index);

        public List<String> findInitializationErrors();

        public String getInitializationErrorString();

        public Builder mergeFrom(CodedInputStream input) throws IOException;

        public Builder mergeFrom(ByteString data) throws InvalidProtocolBufferException;

        public Builder mergeFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException;

        public Builder mergeFrom(byte[] data) throws InvalidProtocolBufferException;

        public Builder mergeFrom(byte[] data, int off, int len) throws InvalidProtocolBufferException;

        public Builder mergeFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException;

        public Builder mergeFrom(byte[] data, int off, int len, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException;

        public Builder mergeFrom(InputStream input) throws IOException;

        public Builder mergeFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException;

        public boolean mergeDelimitedFrom(InputStream input) throws IOException;

        public boolean mergeDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry)
                throws IOException;

        public AbstractMessageLite.Builder mergeFrom(MessageLite other);

    }

    public static Descriptors.Descriptor getDescriptor();

    public static Polygon parseFrom(final ByteBuffer data) throws InvalidProtocolBufferException;

    public static Polygon parseFrom(final ByteBuffer data, final ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException;

    public static Polygon parseFrom(final ByteString data) throws InvalidProtocolBufferException;

    public static Polygon parseFrom(final ByteString data, final ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException;

    public static Polygon parseFrom(final byte[] data) throws InvalidProtocolBufferException;

    public static Polygon parseFrom(final byte[] data, final ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException;

    public static Polygon parseFrom(final InputStream input) throws IOException;

    public static Polygon parseFrom(final InputStream input, final ExtensionRegistryLite extensionRegistry) throws IOException;

    public static Polygon parseDelimitedFrom(final InputStream input) throws IOException;

    public static Polygon parseDelimitedFrom(final InputStream input, final ExtensionRegistryLite extensionRegistry) throws IOException;

    public static Polygon parseFrom(final CodedInputStream input) throws IOException;

    public static Polygon parseFrom(final CodedInputStream input, final ExtensionRegistryLite extensionRegistry) throws IOException;

    public static Builder newBuilder();

    public static Builder newBuilder(final Polygon prototype);

    public static Polygon getDefaultInstance();

    public static Parser<Polygon> parser();

    public UnknownFieldSet getUnknownFields();

    /**
     * <code>repeated .tessellation.Vertex vertex = 1;</code>
     */
    public List<Vertex> getVertexList();

    /**
     * <code>repeated .tessellation.Vertex vertex = 1;</code>
     */
    public List<? extends VertexOrBuilder> getVertexOrBuilderList();

    /**
     * <code>repeated .tessellation.Vertex vertex = 1;</code>
     */
    public int getVertexCount();

    /**
     * <code>repeated .tessellation.Vertex vertex = 1;</code>
     */
    public Vertex getVertex(final int index);

    /**
     * <code>repeated .tessellation.Vertex vertex = 1;</code>
     */
    public VertexOrBuilder getVertexOrBuilder(final int index);

    public boolean isInitialized();

    public void writeTo(final CodedOutputStream output) throws IOException;

    public int getSerializedSize();

    public Builder newBuilderForType();

    public Builder toBuilder();

    public Parser<Polygon> getParserForType();

    public Polygon getDefaultInstanceForType();

    public Descriptors.Descriptor getDescriptorForType();

    public Map<Descriptors.FieldDescriptor, Object> getAllFields();

    public boolean hasOneof(Descriptors.OneofDescriptor oneof);

    public Descriptors.FieldDescriptor getOneofFieldDescriptor(Descriptors.OneofDescriptor oneof);

    public boolean hasField(Descriptors.FieldDescriptor field);

    public Object getField(Descriptors.FieldDescriptor field);

    public int getRepeatedFieldCount(Descriptors.FieldDescriptor field);

    public Object getRepeatedField(Descriptors.FieldDescriptor field, int index);

    public List<String> findInitializationErrors();

    public String getInitializationErrorString();

    public ByteString toByteString();

    public byte[] toByteArray();

    public void writeTo(OutputStream output) throws IOException;

    public void writeDelimitedTo(OutputStream output) throws IOException;

}
```

Holy Toledo, Batman! That's 27 public methods (not including the ones overridden from `java.lang.Object`) plus 17 public static methods. In addition, there is an inner class that exposes exactly the same declared properties from the `.proto` file, stacking up an additional 58 public methods plus another public static method (not including `clone`).

A "leaky abstraction" is a logical abstraction where details of the underlying implementation are unnecessarily exposed. For contrast, compare the above to the equivalent interface in a standard REST service using generic bindings (not tied to any particular vendor).

```java
package org.silnith.example.rest.model;

import java.util.List;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Polygon {

    public Polygon();

    @JsonbCreator
    public Polygon(@JsonbProperty("vertices") @NotNull @Size(min = 3) final List<@NotNull @Valid Vertex> vertices);

    @NotNull
    @Size(min = 3)
    public List<@NotNull @Valid Vertex> getVertices();

    public void setVertices(@NotNull @Size(min = 3) final List<@NotNull @Valid Vertex> vertices);

}
```

Is there any confusion at all as to how to use this class? Here's the associated `Vertex` implementation.

```java
package org.silnith.example.rest.model;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

public class Vertex {

    public Vertex();

    @JsonbCreator
    public Vertex(@JsonbProperty("x") final double x, @JsonbProperty("y") final double y, @JsonbProperty("z") final double z);

    public double getX();

    public void setX(final double x);

    public double getY();

    public void setY(final double y);

    public double getZ();

    public void setZ(final double z);

}
```

Any questions? I only included the non-default constructors to make them look a little longer and more complicated. Did it work? Are you confused? Maybe I should have included the method bodies, too. That nearly doubles the size of each class. However, the gRPC samples only include method signatures so I tried to stay consistent.

Perhaps I am using the API incorrectly. Maybe I should be using the `PolygonOrBuilder` interface. Here it is.

```java
package org.silnith.example.grpc.tessellation;

import java.util.List;
import java.util.Map;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.UnknownFieldSet;

public interface PolygonOrBuilder extends MessageOrBuilder {

    /**
     * <code>repeated .tessellation.Vertex vertex = 1;</code>
     */
    List<Vertex> getVertexList();

    /**
     * <code>repeated .tessellation.Vertex vertex = 1;</code>
     */
    Vertex getVertex(int index);

    /**
     * <code>repeated .tessellation.Vertex vertex = 1;</code>
     */
    int getVertexCount();

    /**
     * <code>repeated .tessellation.Vertex vertex = 1;</code>
     */
    List<? extends VertexOrBuilder> getVertexOrBuilderList();

    /**
     * <code>repeated .tessellation.Vertex vertex = 1;</code>
     */
    VertexOrBuilder getVertexOrBuilder(int index);

    boolean isInitialized();

    Message getDefaultInstanceForType();

    List<String> findInitializationErrors();

    String getInitializationErrorString();

    Descriptors.Descriptor getDescriptorForType();

    Map<Descriptors.FieldDescriptor, Object> getAllFields();

    boolean hasOneof(Descriptors.OneofDescriptor oneof);

    Descriptors.FieldDescriptor getOneofFieldDescriptor(Descriptors.OneofDescriptor oneof);

    boolean hasField(Descriptors.FieldDescriptor field);

    Object getField(Descriptors.FieldDescriptor field);

    int getRepeatedFieldCount(Descriptors.FieldDescriptor field);

    Object getRepeatedField(Descriptors.FieldDescriptor field, int index);

    UnknownFieldSet getUnknownFields();

}
```

Not much better. Still 13 public methods with no relevance to my declared structure whatsoever. Even with this, how am I supposed to use these types? Should I be declaring all of my variables to be of type `PolygonOrBuilder` rather than `Polygon`? Why is that logical for my code? I do not care about the details of the builder pattern used in the library, I only want to interact with polygons. How they are serialized is orthogonal to how I intend to use them in my code. Even the name, "`PolygonOrBuilder`", betrays a fundamental misunderstanding of object-oriented design. An interface should describe the intrinsic properties of an object. It should not expose implementation details of that object. Furthermore, using "or" in an interface name is a backwards way of viewing the concept of inheritance. It is like the library architects tried to use multiple inheritance, but then realized that many programming languages do not support that feature and so they shoved the idea into the name and called it a day. Why not have two interfaces instead? Why does the builder part even need to be there? Why not have the builder implement or extend an interface named `Polygon`? What value does the builder pattern even bring to this in the first place? Here is a simple square created using the builder pattern.

```java
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
```

Here is the same square created with ordinary constructors.

```java
final Polygon polygon = new Polygon(Arrays.asList(
        new Vertex(0, 0, 0),
        new Vertex(1, 0, 0),
        new Vertex(1, 1, 0),
        new Vertex(0, 1, 0)));
```

I know which one I prefer.

Then there is the server side of the system. Here is a trivial implementation of the service.

```java
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
            public void onError(Throwable t) {
                // log error or do something else equally useless
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

}
```

One glaring problem that shows up when you try to write this in an IDE is that the class you create immediately shows up as blank, with no methods to implement. As it turns out, the generated `tessellate` method has a default implementation that simply returns an error saying that the method is not implemented. Except that the base class `TessellationServiceGrpc.TessellationServiceImplBase` is generated, and it is declared abstract. So why wasn't the `tessellate` method also declared abstract? That would force any subclass to implement it, completely eliminating the need for the default error-producing implementation and preventing programmers from making a silly and easily-prevented mistake.

```java
package org.silnith.example.grpc.tessellation;

import javax.annotation.Generated;

import io.grpc.BindableService;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;
import io.grpc.stub.AbstractAsyncStub;
import io.grpc.stub.AbstractBlockingStub;
import io.grpc.stub.AbstractFutureStub;
import io.grpc.stub.StreamObserver;
import io.grpc.stub.annotations.GrpcGenerated;
import io.grpc.stub.annotations.RpcMethod;

/**
 * <pre>
 *
 * A service that provides tessellation for 3D models.
 * </pre>
 */
@Generated(value = "by gRPC proto compiler (version 1.40.1)", comments = "Source: tessellation-service.proto")
@GrpcGenerated
public final class TessellationServiceGrpc {

    public static final String SERVICE_NAME = "tessellation.TessellationService";

    /**
     * <pre>
     *
     * A service that provides tessellation for 3D models.
     * </pre>
     */
    public static abstract class TessellationServiceImplBase implements BindableService {

        /**
         * <pre>
         *
         * Tessellates a series of polygons into a series of triangles suitable for rendering.
         * </pre>
         */
        public StreamObserver<Polygon> tessellate(final StreamObserver<Polygon> responseObserver);

        @Override
        public final ServerServiceDefinition bindService();
    }

    /**
     * <pre>
     *
     * A service that provides tessellation for 3D models.
     * </pre>
     */
    public static final class TessellationServiceStub extends AbstractAsyncStub<TessellationServiceStub> {
        /**
         * <pre>
         *
         * Tessellates a series of polygons into a series of triangles suitable for rendering.
         * </pre>
         */
        public StreamObserver<Polygon> tessellate(final StreamObserver<Polygon> responseObserver);
    }

    /**
     * <pre>
     *
     * A service that provides tessellation for 3D models.
     * </pre>
     */
    public static final class TessellationServiceBlockingStub extends AbstractBlockingStub<TessellationServiceBlockingStub> {
    }

    /**
     * <pre>
     *
     * A service that provides tessellation for 3D models.
     * </pre>
     */
    public static final class TessellationServiceFutureStub extends AbstractFutureStub<TessellationServiceFutureStub> {
    }

    @RpcMethod(fullMethodName = SERVICE_NAME + '/' + "Tessellate", requestType = Polygon.class, responseType = Polygon.class, methodType = MethodDescriptor.MethodType.BIDI_STREAMING)
    public static MethodDescriptor<Polygon, Polygon> getTessellateMethod();

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static TessellationServiceStub newStub(final Channel channel);

    /**
     * Creates a new blocking-style stub that supports unary and streaming output
     * calls on the service
     */
    public static TessellationServiceBlockingStub newBlockingStub(final Channel channel);

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the
     * service
     */
    public static TessellationServiceFutureStub newFutureStub(final Channel channel);

    public static ServiceDescriptor getServiceDescriptor();
}
```

On that same note, the `TessellationServiceGrpc` actually defines three public static inner classes. However, two of them are not compatible with the streaming request and response types declared for the service. So why were those types generated at all? They serve no purpose other than to confuse the programmer. The methods `newBlockingStub` and `newFutureStub` could have simply been omitted because types `TessellationServiceBlockingStub` and `TessellationServiceFutureStub` expose no functionality. The entire point of code generation is so that the code can be customized for a specific use-case. Why then are these empty boilerplates left intact?

In an unrelated note, is anybody else irritated by the malformed and useless JavaDoc generated based on the supposed inline documentation of the `.proto` file?

Overall, I am forced to conclude that the designers of gRPC simply do not know how to design a good API. It makes me question their general competence, and wonder what else they may not know about. In particular, why was an entirely new RPC system needed in the first place? Was there some specific reason that an existing platform-independent and widely supported RPC system could not be used? Were they even aware that CORBA exists?

