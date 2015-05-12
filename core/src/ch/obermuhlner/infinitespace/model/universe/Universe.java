package ch.obermuhlner.infinitespace.model.universe;

import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.generator.Generator;

public class Universe extends Node {

	public Universe(long index) {
		super(index);
	}

	@Override
	public Galaxy getChild(Generator generator, long index) {
		return generator.generateGalaxy(this, index);
	}
}
