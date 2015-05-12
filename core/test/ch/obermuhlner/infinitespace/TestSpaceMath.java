package ch.obermuhlner.infinitespace;

import ch.obermuhlner.infinitespace.util.MathUtil;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class TestSpaceMath {

	public static void main (String[] args) {
		Vector3 position = new Vector3(0, 0, 10);
		Vector3 direction = new Vector3(0, 0, -1);
		
		Array<Vector3> masses = new Array<Vector3>();
		masses.add(new Vector3(0, 0, 0));
		masses.add(new Vector3(2, 2, 2));
		
		for (float z = 3f; z >=0f ; z -= 0.1f) {
			position.z = z;
			float speed = calculateWarpSpeed(position, direction, 1f, masses);
			System.out.println("Speed(pos=" + position + ") : " + speed);
		}
	}

	private static float calculateWarpSpeed (Vector3 position, Vector3 direction, float radius, Array<Vector3> masses) {
		float velocity = 1;
		
		for (Vector3 mass : masses) {
			Vector3 vectorPositionToMass = new Vector3(mass);
			vectorPositionToMass.sub(position);
			
			float distance = vectorPositionToMass.len();
			float dot = vectorPositionToMass.nor().dot(direction);
	
			float directionFactor = 1.0f - dot * 0.9f;
			float correctedDistance = distance - radius;
			
			float delta = 1.0f / correctedDistance * directionFactor;
			delta = 1f - MathUtil.clamp(delta, 0, 1);
			velocity *= delta;
		}
		
		return velocity;
	}
}
