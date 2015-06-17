package ch.obermuhlner.infinitespace.model.universe;

import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.OrbitingNode;
import ch.obermuhlner.infinitespace.model.random.Seed;

public class SpaceStation extends OrbitingNode {

	public enum Type {
		SPHERE {
			public double volume(double height, double width, double length) {
				double radius = (length + height + width) / 3;
				return radius * radius * radius * Math.PI;
			}
			public boolean allowsAgriculture() {
				return true;
			}
		},
		CYLINDER {
			public double volume(double height, double width, double length) {
				double radius = (height + width) / 2;
				return length *  radius * radius * Math.PI;
			}
			public boolean allowsAgriculture() {
				return true;
			}
		},
		VARIABLE_CYLINDER {
			public double volume(double height, double width, double length) {
				double radius = (height + width) / 2;
				return length *  radius * radius * Math.PI / 2;
			}
		},
		RING {
			public double volume(double height, double width, double length) {
				double diameter = height + width * 3; 
				return length * diameter * Math.PI;
			}
			public boolean allowsAgriculture() {
				return true;
			}
		},
		BALANCED {
			public double volume(double height, double width, double length) {
				return height * width * height * 2;
			}
		},
		CUBE {
			public double volume(double height, double width, double length) {
				return height * width * length;
			}
		},
		BLOCKY {
			public double volume(double height, double width, double length) {
				return height * width * length * 0.6;
			}
		},
		CONGLOMERATE {
			public double volume(double height, double width, double length) {
				return height * width * length * 0.01;
			}
		};
		
		abstract public double volume(double height, double width, double length);
		public boolean allowsAgriculture() {
			return false;
		}
	}
	
	public Type type;
	
	public double height; //m
	public double width; //m
	public double length; //m
	
	public boolean starport;
	
	public SpaceStation(Node parent, long index) {
		super(parent, index);
	}

	public SpaceStation(Node parent, Seed seed) {
		super(parent, seed);
	}

}
