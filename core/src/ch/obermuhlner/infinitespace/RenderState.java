package ch.obermuhlner.infinitespace;

import java.util.HashMap;
import java.util.Map;

import ch.obermuhlner.infinitespace.model.Node;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Array;

public class RenderState {

	public final Environment environment = new Environment();
	public final Array<ModelInstance> instancesAlways = new Array<ModelInstance>();
	public final Array<ModelInstance> instancesFar = new Array<ModelInstance>();
	
	public final Map<Node, Array<ModelInstance>> nodeToInstances = new HashMap<Node, Array<ModelInstance>>();
}
