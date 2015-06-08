package ch.obermuhlner.infinitespace.model.universe;

import java.util.Map;

import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.OrbitingSpheroidNode;
import ch.obermuhlner.infinitespace.model.generator.Generator;
import ch.obermuhlner.infinitespace.model.random.Seed;
import ch.obermuhlner.infinitespace.util.Molecule;

public class Planet extends OrbitingSpheroidNode {

	public static enum Type { STONE, GAS, ICE };
	
	public Type type;
	public String textureName;
	
	// magnetic field
	public double albedo;
	public double atmospherePressure; // Pa at surface
	public Map<Molecule, Double> atmosphere;
	public double temperature;
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
