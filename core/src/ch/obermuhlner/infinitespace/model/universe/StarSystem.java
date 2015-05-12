package ch.obermuhlner.infinitespace.model.universe;

import ch.obermuhlner.infinitespace.model.CartesianNode;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.generator.Generator;
import ch.obermuhlner.infinitespace.model.random.Seed;

public class StarSystem extends CartesianNode {

	public StarSystem(Seed seed) {
		super(null, seed);
	}
	
	public StarSystem(Node parent, long index) {
		super(parent, index);
	}

	@Override
	public Node getChild(Generator generator, long index) {
		return generator.generateStar(this, index);
	}

}
