package ch.obermuhlner.infinitespace.graphics;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

/**
 * The {@link CenterPerspectiveCamera} keeps track of a position offset so that the camera can be recentered to (0, 0, 0).
 * 
 * Together with every call to {@link #recenter()} all model instances in the scene must be repositioned by the {@link #position}.
 * 
 * This reduces the jitter problem that happens when the position is far away from point 0 and objects are close to the camera.
 */
public class CenterPerspectiveCamera extends PerspectiveCamera {

	public Vector3 positionOffset = new Vector3();
	
	public CenterPerspectiveCamera() {
		super();
	}

	public CenterPerspectiveCamera(float fieldOfViewY, float viewportWidth, float viewportHeight) {
		super(fieldOfViewY, viewportWidth, viewportHeight);
	}

	public void recenter() {
		positionOffset.add(position);
		position.setZero();
	}
	
}
