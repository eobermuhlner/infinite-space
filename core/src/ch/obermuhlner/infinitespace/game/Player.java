package ch.obermuhlner.infinitespace.game;

import ch.obermuhlner.infinitespace.NodeToRenderConverter;
import ch.obermuhlner.infinitespace.Throttle;
import ch.obermuhlner.infinitespace.game.ship.Ship;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.universe.Planet;
import ch.obermuhlner.infinitespace.model.universe.Star;
import ch.obermuhlner.infinitespace.util.MathUtil;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Player {

	public Ship ship;
	public Camera camera;
	
	public float velocity = 1.0f;

	public Throttle thrustForwardThrottle = new Throttle();
	//public float thrustForward = 0;
	public float thrustUp = 0;
	public float thrustRight = 0;

	public float pitch = 0;
	public float roll = 0;
	public float yaw = 0;

	// optimization: temporary variable
	private final Vector3 vec3 = new Vector3();

	
	public Player (Ship ship, Camera camera) {
		this.ship = ship;
		this.camera = camera;
	}

	public void addThrustForward(float value) {
		//thrustForward = MathUtil.clamp(thrustForward + value, -1.0f, 1.0f);
	}

	public void addThrustUp(float value) {
		thrustUp = MathUtil.clamp(thrustUp + value, -1.0f, 1.0f);
	}

	public void addThrustRight(float value) {
		thrustRight = MathUtil.clamp(thrustRight + value, -1.0f, 1.0f);
	}

	public void addPitch(float value) {
		pitch = MathUtil.clamp(pitch + value, -1.0f, 1.0f);
	}

	public void addRoll(float value) {
		roll = MathUtil.clamp(roll + value, -1.0f, 1.0f);
	}

	public void addYaw(float value) {
		yaw = MathUtil.clamp(yaw + value, -1.0f, 1.0f);
	}

	public void update (float deltaTime) {
		float mass = ship.mass;
		float maxThrustForward = ship.forwardThruster.thrust / mass;
		float maxThrustRight = ship.rightThruster.thrust / mass;
		float maxThrustUp = ship.upThruster.thrust / mass;
		float maxThrustRoll = ship.rollThruster.thrust / mass;
		float maxThrustPitch = ship.pitchThruster.thrust / mass;
		float maxThrustYaw = ship.yawThruster.thrust / mass;
		
		vec3.set(camera.direction).crs(camera.up).scl(deltaTime * thrustRight * velocity * maxThrustRight);
		camera.position.add(vec3);

		vec3.set(camera.direction).scl(deltaTime * thrustForwardThrottle.value * velocity * maxThrustForward);
		camera.position.add(vec3);
		
		vec3.set(camera.up).scl(deltaTime * thrustUp * velocity * maxThrustUp);
		camera.position.add(vec3);
		
		float rotateAngle = 90;
		camera.rotate(camera.up, deltaTime * -yaw * maxThrustYaw * rotateAngle);
		vec3.set(camera.direction).crs(camera.up).nor();
		camera.rotate(vec3, deltaTime * pitch * maxThrustPitch * rotateAngle);
		vec3.set(camera.direction);
		camera.rotate(vec3, deltaTime * roll * maxThrustRoll * rotateAngle);

		camera.update(true);
	}

	public void calculateHyperVelocity (Array<Node> massiveNodes) {
		velocity = 500.0f;

		for (int i = 0; i < massiveNodes.size; i++) {
			Node node = massiveNodes.get(i);
			Vector3 position = NodeToRenderConverter.calculatePosition(node);
			position.sub(camera.position);
			float radius;
			if (node instanceof Planet) {
				Planet planet = (Planet)node;
				radius = NodeToRenderConverter.calculatePlanetRadius(planet);
			} else if (node instanceof Star) {
				Star star = (Star)node;
				radius = NodeToRenderConverter.calculateStarRadius(star);
			} else {
				radius = 0.1f;
			}
			float distance = position.len();
			float dot = position.nor().dot(camera.direction);
			float directionFactor = Math.min(1f, MathUtil.transform(-1f, 1f, 0.8f, 1.01f, dot));
			float correctedDistance = Math.max(distance - radius * 2, 0.0001f);

			float maxDelta = 1.0f + Math.min(dot, 0);
			float delta = 1.001f / (correctedDistance + 1) * directionFactor;
			delta = 1f - MathUtil.clamp(delta, 0, maxDelta);
			//System.out.printf("dist=%10.6f corr=%10.6f dirFactor=%10.6f delta=%10.6f => velocity=%10.6f dot=%10.6f %s radius=%10.2f\n", distance, correctedDistance, directionFactor, delta, velocity, dot, node.toString(), radius);
			velocity *= delta;
		}
		
		//System.out.println("velocity " + velocity);
		//System.out.println();
	}
}
