package ch.obermuhlner.infinitespace.model.universe;

import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.OrbitingNode;
import ch.obermuhlner.infinitespace.model.random.Seed;

public class AsteroidBelt extends OrbitingNode {

	public double width; // m
	public double height; // m
	public double density; // asteroid/m^3
	public double averageRadius; // m
	
	public AsteroidBelt(Node parent, long index) {
		super(parent, index);
	}

	public AsteroidBelt(Node parent, Seed seed) {
		super(parent, seed);
	}
}
