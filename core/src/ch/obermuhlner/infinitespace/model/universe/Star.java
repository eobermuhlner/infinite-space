package ch.obermuhlner.infinitespace.model.universe;

import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.OrbitingSpheroidNode;
import ch.obermuhlner.infinitespace.model.generator.Generator;

public class Star extends OrbitingSpheroidNode {

	public static enum Type { 
		BROWN_DWARF,
		WHITE_DWARF,
		MAIN_SEQUENCE,
		SUB_GIANT,
		GIANT,
		SUPER_GIANT,
		};
	
	public Type type;
	public double temperature; // K
	
	public Star(Node parent, long index) {
		super(parent, index);
	}

	@Override
	public Node getChild(Generator generator, long index) {
		return generator.generateStarChild(this, index);
	}

}
