package ch.obermuhlner.infinitespace;

import java.util.HashMap;
import java.util.Map;

import ch.obermuhlner.infinitespace.model.Node;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class RenderState {

	public final Environment environment = new Environment();
	public final Array<ModelInstance> instancesAlways = new Array<ModelInstance>();
	public final Array<ModelInstance> instancesFar = new Array<ModelInstance>();
	public final Array<BaseLight> lights = new Array<BaseLight>();
	
	public final Map<Node, Array<ModelInstance>> nodeToInstances = new HashMap<Node, Array<ModelInstance>>();

	public void addLight(BaseLight light) {
		lights.add(light);
		environment.add(light);		
	}

	public void recenter(Vector3 cameraPosition) {
		for (int i = 0; i < lights.size; i++) {
			BaseLight light = lights.get(i);
			if (light instanceof SpotLight) {
				SpotLight spotLight = (SpotLight) light;
				spotLight.position.add(cameraPosition);
			} else if (light instanceof PointLight) {
				PointLight spotLight = (PointLight) light;
				spotLight.position.add(cameraPosition);
			}
		}
		
		for (int i = 0; i < instancesAlways.size; i++) {
			ModelInstance modelInstance = instancesAlways.get(i);
			modelInstance.transform.translate(cameraPosition);
		}

		Vector3 nearest = null;
		Vector3 tmpPosition = new Vector3();
		for(Map.Entry<Node, Array<ModelInstance>> entry : nodeToInstances.entrySet()) {
			Array<ModelInstance> nodeInstances = entry.getValue();
			for (int i = 0; i < nodeInstances.size; i++) {
				ModelInstance modelInstance = nodeInstances.get(i);
				modelInstance.transform.translate(cameraPosition);
				if (nearest == null) {
					nearest = new Vector3();
					modelInstance.transform.getTranslation(nearest);
				} else {
					modelInstance.transform.getTranslation(tmpPosition);
					if (tmpPosition.len2() < nearest.len2()) {
						nearest.set(tmpPosition);
					}
				}
			}
		}
		
	}
}
