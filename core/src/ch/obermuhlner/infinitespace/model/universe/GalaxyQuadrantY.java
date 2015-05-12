package ch.obermuhlner.infinitespace.model.universe;

import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.generator.Generator;

public class GalaxyQuadrantY extends Node {

	public int quadrantY; // quadrant
	
	public GalaxyQuadrantY(Node parent, long index) {
		super(parent, index);
	}

	@Override
	public GalaxyQuadrantZ getChild(Generator generator, long index) {
		return generator.generateGalaxyQuadrantZ(this, index);
	}

}
