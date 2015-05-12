package ch.obermuhlner.infinitespace;

import ch.obermuhlner.infinitespace.model.Node;

public class UserData {
	
	public String shaderName;

	public Node node;
	
	@Override
	public String toString () {
		return "UserData{" + shaderName + ", " + node + "}";
	}
}
