package org.silnith.example.renderer;

import java.util.Locale;

import org.silnith.example.grpc.tessellation.VertexOrBuilder;

public class Renderer {
	
	public void render(final VertexOrBuilder a, final VertexOrBuilder b, final VertexOrBuilder c) {
		String.format(Locale.US,
				"(%+,.2f, %+,.2f, %+,.2f) -> (%+,.2f, %+,.2f, %+,.2f) -> (%+,.2f, %+,.2f, %+,.2f)",
				a.getX(), a.getY(), a.getZ(),
				b.getX(), b.getY(), b.getZ(),
				c.getX(), c.getY(), c.getZ());
	}
	
}
