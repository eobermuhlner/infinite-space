package ch.obermuhlner.infinitespace.game;

import ch.obermuhlner.infinitespace.Config;
import ch.obermuhlner.infinitespace.NodeToRenderConverter;
import ch.obermuhlner.infinitespace.Throttle;
import ch.obermuhlner.infinitespace.game.ship.Ship;
import ch.obermuhlner.infinitespace.graphics.CenterPerspectiveCamera;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.universe.Planet;
import ch.obermuhlner.infinitespace.model.universe.SpaceStation;
import ch.obermuhlner.infinitespace.model.universe.Star;
import ch.obermuhlner.infinitespace.util.MathUtil;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Player {

	private static final float HYPER_DRAG_RADII = 1000;
	public Ship ship;
	public CenterPerspectiveCamera camera;
	
	public float velocity = 1.0f;
	public float warpDrag = 0.0f;
	public Node warpDragNode;
	
	public Throttle thrustForwardThrottle = new Throttle();
	//public float thrustForward = 0;
	public float thrustUp = 0;
	public float thrustRight = 0;

	public float pitch = 0;
	public float roll = 0;
	public float yaw = 0;

	// optimization: temporary variable
	private final Vector3 vec3 = new Vector3();

	public Player (Ship ship, CenterPerspectiveCamera camera) {
		this.ship = ship;
		this.camera = camera;
	}

	public void addThrustForward(float value) {
		//thrustForward = MathUtil.maybeZero(MathUtil.clamp(thrustForward + value, -1.0f, 1.0f));
	}

	public void addThrustUp(float value) {
		thrustUp = MathUtil.maybeZero(MathUtil.clamp(thrustUp + value, -1.0f, 1.0f));
	}

	public void addThrustRight(float value) {
		thrustRight = MathUtil.maybeZero(MathUtil.clamp(thrustRight + value, -1.0f, 1.0f));
	}

	public void addPitch(float value) {
		pitch = MathUtil.maybeZero(MathUtil.clamp(pitch + value, -1.0f, 1.0f));
	}

	public void addRoll(float value) {
		roll = MathUtil.maybeZero(MathUtil.clamp(roll + value, -1.0f, 1.0f));
	}

	public void addYaw(float value) {
		yaw = MathUtil.maybeZero(MathUtil.clamp(yaw + value, -1.0f, 1.0f));
	}

	public void update (float deltaTime) {
		float mass = ship.mass;
		float maxThrustForward = ship.forwardThrust / mass;
		float maxThrustRight = ship.rightThrust / mass;
		float maxThrustUp = ship.upThrust / mass;
		float maxThrustRoll = ship.rollThrust / mass;
		float maxThrustPitch = ship.pitchThrust / mass;
		float maxThrustYaw = ship.yawThrust / mass;
		
		boolean updateNeeded = false;
		
		if (thrustRight != 0) {
			vec3.set(camera.direction).crs(camera.up).scl(deltaTime * thrustRight * velocity * maxThrustRight);
			camera.position.add(vec3);
			updateNeeded = true;
		}
		if (thrustForwardThrottle.value != 0) {
			vec3.set(camera.direction).scl(deltaTime * thrustForwardThrottle.value * velocity * maxThrustForward);
			camera.position.add(vec3);
			updateNeeded = true;
		}
		if (thrustUp != 0) {
			vec3.set(camera.up).scl(deltaTime * thrustUp * velocity * maxThrustUp);
			camera.position.add(vec3);
			updateNeeded = true;
		}
		
		float rotateAngle = 90;
		if (yaw != 0) {
			camera.rotate(camera.up, deltaTime * -yaw * maxThrustYaw * rotateAngle);
			updateNeeded = true;
		}
		if (pitch != 0) {
			vec3.set(camera.direction).crs(camera.up).nor();
			camera.rotate(vec3, deltaTime * pitch * maxThrustPitch * rotateAngle);
			updateNeeded = true;
		}
		if (roll != 0) {
			vec3.set(camera.direction);
			camera.rotate(vec3, deltaTime * roll * maxThrustRoll * rotateAngle);
			updateNeeded = true;
		}

		if (updateNeeded) {
			camera.update(true);
		}
	}

	public void calculateHyperVelocity (Array<Node> massiveNodes) {
		velocity = (float) (10000000E5 * Config.SIZE_FACTOR);

		float strongestDragFactor = 1f;
		Node strongestDragNode = null;
		for (int i = 0; i < massiveNodes.size; i++) {
			Node node = massiveNodes.get(i);
			Vector3 position = NodeToRenderConverter.calculatePosition(node);
			position.sub(camera.position);
			position.sub(camera.positionOffset);
			float radius;
			if (node instanceof Planet) {
				Planet planet = (Planet)node;
				radius = NodeToRenderConverter.calculatePlanetRadius(planet);
			} else if (node instanceof Star) {
				Star star = (Star)node;
				radius = NodeToRenderConverter.calculateStarRadius(star);
			} else if (node instanceof SpaceStation) {
				SpaceStation spaceStation = (SpaceStation)node;
				radius = NodeToRenderConverter.calculateSpaceStationRadius(spaceStation);
			} else {
				continue;
			}
			float distance = position.len();
			float dot = position.nor().dot(camera.direction);
			float directionFactor = MathUtil.smoothstep(0f, 1f, Math.min(1f, MathUtil.transform(0f, 1f, 0.2f, 1.1f, dot)));
			//directionFactor = directionFactor * directionFactor;
			float correctedDistance = Math.max(0, distance - 3 * radius) / radius;

			float drag = directionFactor / (1 + correctedDistance / HYPER_DRAG_RADII);
			float dragFactor = 1f - MathUtil.clamp(drag, 0, 1.0f);
			if (dragFactor < strongestDragFactor) {
				strongestDragNode = node;
				System.out.printf("dist=%10.6f radii=%10.6f corr=%15.6f dot=%10.6f dirFactor=%10.6f drag=%10.6f dragFactor=%10.6f %s\n", 
						distance, (distance/radius), correctedDistance, dot, directionFactor, drag, dragFactor, node.toString());
			}
			strongestDragFactor = Math.min(strongestDragFactor, dragFactor);
		}
		warpDrag = strongestDragFactor;
		warpDragNode = strongestDragNode;
		velocity *= strongestDragFactor;
		
		System.out.println();		
	}
}
