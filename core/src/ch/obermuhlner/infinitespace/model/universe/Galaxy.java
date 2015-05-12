package ch.obermuhlner.infinitespace.model.universe;

import ch.obermuhlner.infinitespace.model.CartesianNode;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.generator.Generator;

public class Galaxy extends CartesianNode {

	public int radiusX; // quadrant
	public int radiusY; // quadrant
	public int radiusZ; // quadrant
	
	public Galaxy(Node parent, long index) {
		super(parent, index);
	}

	@Override
	public GalaxyQuadrantX getChild(Generator generator, long index) {
		return generator.generateGalaxyQuadrantX(this, index);
	}
}
