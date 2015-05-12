package ch.obermuhlner.infinitespace.model;

import ch.obermuhlner.infinitespace.model.random.Seed;


public class CartesianNode extends Node {

	public double x; // m
	public double y; // m
	public double z; // m
	
	public double angleX; // rad
	public double angleY; // rad
	public double angleZ; // rad
	
	public CartesianNode(Node parent, long index) {
		super(parent, index);
	}

	public CartesianNode(Node parent, Seed seed) {
		super(parent, seed);
	}
}
