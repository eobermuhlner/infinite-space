package ch.obermuhlner.infinitespace;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.utils.Array;


public interface NodeConverter<T> {

	public Array<ModelInstance> convertToModelInstances (T node, boolean realUniverse);

	public BaseLight convertToLight (T node, boolean realUniverse);

}
