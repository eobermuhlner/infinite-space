package ch.obermuhlner.infinitespace;

import java.util.Map;

import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.util.Molecule;

public class UserData {
	
	public String shaderName;

	public Node node;
	
	public String modelName;
	
	public String description;
	public Map<Molecule, Double> composition;
	
	@Override
	public String toString () {
		return "UserData{" + shaderName + ", " + node + "}";
	}
}
