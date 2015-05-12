package ch.obermuhlner.infinitespace.game.ship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShipFactory {

	public static Ship getStandardShip() {
		Ship ship = new Ship();
		
		ship.forwardThruster = new Thruster(4.0f);
		ship.upThruster = new Thruster(0.4f);
		ship.rightThruster = new Thruster(0.4f);
		ship.rollThruster = new Thruster(2.0f);
		ship.pitchThruster = new Thruster(2.0f);
		ship.yawThruster = new Thruster(1f);

		ship.parts.add(new ShipPart<Hull>(
			Hull.class.getSimpleName(), 
			1, 1, 1, 
			DEFAULT_HULL));
		ship.parts.add(new ShipPart<PowerPlant>(
			PowerPlant.class.getSimpleName(), 
			1, 1, 3, 
			DEFAULT_POWER_PLANT));
		ship.parts.add(new ShipPart<Weapon>(
			Weapon.class.getSimpleName(),
			0, 4, 3));
		ship.parts.add(new ShipPart<InternalComponent>(
			InternalComponent.class.getSimpleName(),
			0, 5, 20f,
			Arrays.<InternalComponent> asList(
				DEFAULT_CARGO_BAY)));
		
		ship.update();
		
		return ship;
	}
	
	private static final Hull DEFAULT_HULL = new Hull(Hull.Type.STEEL, 1f, 20f);
	public static List<Hull> getHulls() {
		return Arrays.asList(
			// strength, allowedMass
			new Hull(Hull.Type.ALUMINIUM, 1.0f, 10f),
			new Hull(Hull.Type.ALUMINIUM, 1.1f, 20f),
			new Hull(Hull.Type.ALUMINIUM, 1.2f, 30f),
			DEFAULT_HULL,
			new Hull(Hull.Type.STEEL, 2f, 30f),
			new Hull(Hull.Type.STEEL, 3f, 40f),
			new Hull(Hull.Type.STEEL, 3f, 60f),
			new Hull(Hull.Type.TITANIUM, 1.0f, 20f),
			new Hull(Hull.Type.TITANIUM, 1.5f, 30f),
			new Hull(Hull.Type.TITANIUM, 2.0f, 40f),
			new Hull(Hull.Type.TITANIUM, 2.5f, 60f),
			new Hull(Hull.Type.TITANIUM, 3.0f, 80f),
			new Hull(Hull.Type.PLASTEEL, 2.0f, 30f),
			new Hull(Hull.Type.PLASTEEL, 2.5f, 40f),
			new Hull(Hull.Type.PLASTEEL, 3.0f, 60f),
			new Hull(Hull.Type.PLASTEEL, 3.5f, 80f)
			);
	}
	
	private static final PowerPlant DEFAULT_POWER_PLANT = new PowerPlant(10);
	public static List<PowerPlant> getPowerPlants() {
		return Arrays.asList(
			// power
			DEFAULT_POWER_PLANT,
			new PowerPlant(20),
			new PowerPlant(30),
			new PowerPlant(40),
			new PowerPlant(50),
			new PowerPlant(60)
			);
	}
	
	public static List<ShieldGenerator> getShieldGenerators() {
		return Arrays.asList(
			// shield
			new ShieldGenerator(1f),
			new ShieldGenerator(2f),
			new ShieldGenerator(4f),
			new ShieldGenerator(8f)
			);
	}

	private static final CargoBay DEFAULT_CARGO_BAY = new CargoBay(2);
	public static List<CargoBay> getCargoBays() {
		return Arrays.asList(
			// space
			new CargoBay(1),
			DEFAULT_CARGO_BAY,
			new CargoBay(4),
			new CargoBay(8),
			new CargoBay(10),
			new CargoBay(15),
			new CargoBay(30),
			new CargoBay(50)
			);
	}
	
	public static List<PassengerBay> getPassengerBays() {
		return Arrays.asList(
			// type, space
			new PassengerBay(PassengerBay.Type.ECONOMY, 1),
			new PassengerBay(PassengerBay.Type.ECONOMY, 2),
			new PassengerBay(PassengerBay.Type.ECONOMY, 4),
			new PassengerBay(PassengerBay.Type.BUSINESS, 1),
			new PassengerBay(PassengerBay.Type.BUSINESS, 2),
			new PassengerBay(PassengerBay.Type.BUSINESS, 4),
			new PassengerBay(PassengerBay.Type.FIRST, 1),
			new PassengerBay(PassengerBay.Type.FIRST, 2),
			new PassengerBay(PassengerBay.Type.FIRST, 4),
			new PassengerBay(PassengerBay.Type.VIP, 1),
			new PassengerBay(PassengerBay.Type.VIP, 2),
			new PassengerBay(PassengerBay.Type.VIP, 4)
			);
	}
	
	public static List<Weapon> getWeapons() {
		return Arrays.asList(
			new Weapon(Weapon.Type.LASER, 1.0f, 0.2f),
			new Weapon(Weapon.Type.LASER, 1.5f, 0.1f),
			new Weapon(Weapon.Type.PLASMA, 3.0f, 0.5f),
			new Weapon(Weapon.Type.PLASMA, 2.0f, 0.2f),
			new Weapon(Weapon.Type.BULLET, 0.5f, 0.05f),
			new Weapon(Weapon.Type.BULLET, 0.5f, 0.02f)
			);
	}

	public static List<? extends ShipComponent> getShipComponents (String type) {
		if (PowerPlant.class.getSimpleName().equals(type)) {
			return getPowerPlants();
		}
		if (CargoBay.class.getSimpleName().equals(type)) {
			return getCargoBays();
		}
		if (PassengerBay.class.getSimpleName().equals(type)) {
			return getPassengerBays();
		}
		if (ShieldGenerator.class.getSimpleName().equals(type)) {
			return getShieldGenerators();
		}
		if (InternalComponent.class.getSimpleName().equals(type)) {
			List<ShipComponent> result = new ArrayList<ShipComponent>();
			result.addAll(getPowerPlants());
			result.addAll(getCargoBays());
			result.addAll(getPassengerBays());
			result.addAll(getShieldGenerators());
			return result;
		}
		if (Hull.class.getSimpleName().equals(type)) {
			return getHulls();
		}
		if (Weapon.class.getSimpleName().equals(type)) {
			return getWeapons();
		}
		
		return Collections.emptyList();
	}
}