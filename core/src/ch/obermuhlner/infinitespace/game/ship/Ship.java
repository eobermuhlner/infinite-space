package ch.obermuhlner.infinitespace.game.ship;

import java.util.ArrayList;
import java.util.List;

public class Ship {
	public Thruster forwardThruster;
	public Thruster upThruster;
	public Thruster rightThruster;
	public Thruster rollThruster;
	public Thruster pitchThruster;
	public Thruster yawThruster;

	public List<ShipPart<?>> parts = new ArrayList<ShipPart<?>>();
	
	public float allowedMass;
	public float mass;
	public float power;
	public float shield;
	public int cargoSpace;
	public int passengerSpace;
	
	public void update() {
		allowedMass = 0;
		mass = forwardThruster.mass + upThruster.mass + rightThruster.mass + rollThruster.mass + pitchThruster.mass + yawThruster.mass;
		power = forwardThruster.power + upThruster.power + rightThruster.power + rollThruster.power + pitchThruster.power + yawThruster.power;
		shield = 0;
		cargoSpace = 0;
		passengerSpace = 0;
		
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
