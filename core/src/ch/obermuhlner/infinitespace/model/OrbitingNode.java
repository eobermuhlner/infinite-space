package ch.obermuhlner.infinitespace.model;

import ch.obermuhlner.infinitespace.model.random.Seed;
import ch.obermuhlner.infinitespace.model.universe.population.Population;


public class OrbitingNode extends Node {

	public double orbitRadius; // m
	public double orbitPeriod; // seconds
	public double orbitStartAngle; // radian
	public double rotation; // seconds
	public double mass; // kg
	public Population population;
	
	public OrbitingNode(Node parent, long index) {
		super(parent, index);
	}
	
	public OrbitingNode(Node parent, Seed seed) {
		super(parent, seed);
	}

}
