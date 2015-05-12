package ch.obermuhlner.infinitespace.model.universe;

import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.generator.Generator;

public class GalaxyQuadrantX extends Node {

	public int quadrantX; // quadrant
	
	public GalaxyQuadrantX(Node parent, long index) {
		super(parent, index);
	}

	@Override
	public GalaxyQuadrantY getChild(Generator generator, long index) {
		return generator.generateGalaxyQuadrantY(this, index);
	}

}
