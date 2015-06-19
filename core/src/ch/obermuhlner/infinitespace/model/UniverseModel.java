package ch.obermuhlner.infinitespace.model;

import ch.obermuhlner.infinitespace.UniverseCoordinates;
import ch.obermuhlner.infinitespace.model.generator.Generator;
import ch.obermuhlner.infinitespace.model.universe.Galaxy;
import ch.obermuhlner.infinitespace.model.universe.GalaxyQuadrantX;
import ch.obermuhlner.infinitespace.model.universe.GalaxyQuadrantY;
import ch.obermuhlner.infinitespace.model.universe.GalaxyQuadrantZ;
import ch.obermuhlner.infinitespace.model.universe.StarSystem;
import ch.obermuhlner.infinitespace.model.universe.Universe;

import com.badlogic.gdx.utils.Array;

public class UniverseModel {

	public final Generator generator = new Generator();
	
	private int universeIndex = 0;

	private int starSystemIndex = 0;

	Array<Node> nearNodes = new Array<Node>();
	Array<Node> farNodes = new Array<Node>();
	
	public void setStarSystemIndex (int starSystemIndex) {
		this.starSystemIndex = starSystemIndex;
	}

	public Iterable<Node> getUniverse() {
		Universe universe = generator.generateUniverse(universeIndex);

		Node node = universe;
		while (!(node instanceof GalaxyQuadrantZ)) {
			node = generator.getChild(node, 0);
		}

		// get StarSystem
		node = generator.getChild(node, starSystemIndex);

		Array<Node> result = new Array<Node>();
		generateDeep(node, result);
		return result;
	}
	
	private void generateDeep (Node node, Array<Node> result) {
		result.add(node);
		int childCount = generator.getChildCount(node);
		for (int i = 0; i < childCount; i++) {
			Node child = generator.getChild(node, i);
			generateDeep(child, result);
		}
	}

	public void setPosition (UniverseCoordinates coordinates) {
		if (nearNodes.size == 0) {
			nearNodes.add(generator.generateUniverse(universeIndex));
		}
		
		for (int i=nearNodes.size-1; i>0; i--) {
			Node node = nearNodes.get(i);
			if (!isNear(coordinates, node)) {
				farNodes.add(node);
				nearNodes.removeIndex(i);
			}
		}
		
		for (int i=farNodes.size-1; i>0; i--) {
			Node node = farNodes.get(i);
			if (isNear(coordinates, node)) {
				nearNodes.add(node);
				farNodes.removeIndex(i);
				
				int childCount = generator.getChildCount(node);
				for (int j = 0; j < childCount; j++) {
					Node child = generator.getChild(node, j);
					farNodes.add(child);
				}
			}
		}
	}

	private boolean isNear (UniverseCoordinates coordinates, Node node) {
		return false;
	}

}
