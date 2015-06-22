package ch.obermuhlner.infinitespace.model;

import ch.obermuhlner.infinitespace.model.generator.Generator;
import ch.obermuhlner.infinitespace.model.random.Seed;

public class Node {

	public Node parent; // TODO writable, so that Generator can update parent of stored nodes - find a better solution (e.g. copy stored nodes)
	public final Seed seed;

	public String name;
	
	public int childCount;

	public Node(long index) {
		this(null, new Seed(index));
	}
	
	public Node(Node parent, long index) {
		this(parent, new Seed(parent.seed, index));
	}
	
	public Node(Node parent, Seed seed) {
		this.parent = parent;
		this.seed = seed;
	}
	
	public int getChildCount(Generator generator) {
		return childCount;
	}
	
	public Node getChild(Generator generator, long index) {
		return null;
	}
	
	@Override
	public int hashCode() {
		return seed == null ? 42 : seed.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (seed == null) {
			if (other.seed != null)
				return false;
		} else if (!seed.equals(other.seed))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + getFullName() + " " + seed;
	}

	public String getName() {
		return name == null ? "?" : name;
	}
	
	public String getFullName () {
		StringBuilder fullName = new StringBuilder();
		
		boolean first = true;
		
		Node current = this;
		while (current != null) {
			if (current.name != null) {
				if (! first) {
					fullName.append(" - ");
				}
				fullName.append(current.name);
				first = false;
			}
			current = current.parent;
		}
		
		return fullName.toString();
	}

}
