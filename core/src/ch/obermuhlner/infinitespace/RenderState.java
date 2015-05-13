package ch.obermuhlner.infinitespace;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Array;

public class RenderState {

	public final Environment environment = new Environment();
	public final Array<ModelInstance> instances = new Array<ModelInstance>();
	public final Array<ModelInstance> instancesAlways = new Array<ModelInstance>();
	public final Array<ModelInstance> instancesFar = new Array<ModelInstance>();
}
