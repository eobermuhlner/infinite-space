package ch.obermuhlner.infinitespace.model.universe;

import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.OrbitingSpheroidNode;
import ch.obermuhlner.infinitespace.model.generator.Generator;
import ch.obermuhlner.infinitespace.model.random.Seed;

public class Planet extends OrbitingSpheroidNode {

	public static enum Type { STONE, GAS, ICE };
	
	public Type type;
	public String textureName;
	
	public boolean breathableAtmosphere;
	public double water; // 1.0 = 100% covered
	public boolean supportsLife;
	public boolean hasLife;
	
	public Planet(Node parent, long index) {
		super(parent, index);
	}
	
	public Planet(Node parent, Seed seed) {
		super(parent, seed);
	}
	
	@Override
	public Node getChild (Generator generator, long index) {
		return generator.generatePlanetChild(this, index);
	}
}
