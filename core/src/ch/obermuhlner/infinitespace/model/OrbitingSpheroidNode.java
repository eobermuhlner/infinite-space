package ch.obermuhlner.infinitespace.model;

import ch.obermuhlner.infinitespace.model.random.Seed;


public class OrbitingSpheroidNode extends OrbitingNode {

	public double radius; // m

	public OrbitingSpheroidNode(Node parent, long index) {
		super(parent, index);
	}

	public OrbitingSpheroidNode(Node parent, Seed seed) {
		super(parent, seed);
	}
}
