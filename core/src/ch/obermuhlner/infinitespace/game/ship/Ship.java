package ch.obermuhlner.infinitespace.game.ship;

import java.util.ArrayList;
import java.util.List;

public class Ship {
	public float forwardThrust;
	public float upThrust;
	public float rightThrust;
	public float rollThrust;
	public float pitchThrust;
	public float yawThrust;
	
	public List<ShipPart<?>> parts = new ArrayList<ShipPart<?>>();
	
	public float allowedMass;
	public float mass;
	public float power;
	public float shield;
	public int cargoSpace;
	public int passengerSpace;
	
	public void update() {
		allowedMass = 0;
		mass = 0;
		power = 0;
		shield = 0;
		cargoSpace = 0;
		passengerSpace = 0;
		
		forwardThrust = 0;
		upThrust = 0;
		rightThrust = 0;
		rollThrust = 0;
		pitchThrust = 0;
		yawThrust = 0;
		
		for (ShipPart<?> part : parts) {
			for (ShipComponent component : part.components) {
				mass += component.mass;
				power += component.power;
				if (component instanceof Hull) {
					allowedMass += ((Hull) component).allowedMass;
				}
				if (component instanceof ShieldGenerator) {
					shield += ((ShieldGenerator) component).shield;
				}
				if (component instanceof CargoBay) {
					cargoSpace += ((CargoBay) component).space;
				}
				if (component instanceof PassengerBay) {
					passengerSpace += ((PassengerBay) component).space;
				}
				if (component instanceof Thruster) {
					Thruster thruster = (Thruster) component;
					if (part.name.equals(ShipFactory.MAIN_THRUSTER)) {
						forwardThrust += thruster.thrust;
					} else if (part.name.equals(ShipFactory.UP_DOWN_THRUSTER)) {
						upThrust += thruster.thrust;
					} else if (part.name.equals(ShipFactory.LEFT_RIGHT_THRUSTER)) {
						rightThrust += thruster.thrust;
					} else if (part.name.equals(ShipFactory.PITCH_THRUSTER)) {
						pitchThrust += thruster.thrust;
					} else if (part.name.equals(ShipFactory.ROLL_THRUSTER)) {
						rollThrust += thruster.thrust;
					} else if (part.name.equals(ShipFactory.YAW_THRUSTER)) {
						yawThrust += thruster.thrust;
					}
				}
			}
		}
	}

	@Override
	public String toString () {
		StringBuilder builder = new StringBuilder();
		builder.append("Ship [mass=");
		builder.append(mass);
		builder.append(", power=");
		builder.append(power);
		builder.append(", shield=");
		builder.append(shield);
		builder.append(", cargoSpace=");
		builder.append(cargoSpace);
		builder.append(", passengerSpace=");
		builder.append(passengerSpace);
		builder.append("]");
		return builder.toString();
	}
	
	
}
