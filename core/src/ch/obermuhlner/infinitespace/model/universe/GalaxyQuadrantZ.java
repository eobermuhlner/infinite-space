package ch.obermuhlner.infinitespace.model.universe;

import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.generator.Generator;

public class GalaxyQuadrantZ extends Node {

	public int quadrantZ; // quadrant
	
	public GalaxyQuadrantZ(Node parent, long index) {
		super(parent, index);
	}

	@Override
	public StarSystem getChild(Generator generator, long index) {
		return generator.generateStarSystem(this, index);
	}

}
