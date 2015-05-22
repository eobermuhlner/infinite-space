package ch.obermuhlner.infinitespace.game.ship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.utils.Array;

public class ShipFactory {

	public static final String MAIN_THRUSTER = "Main Thruster";
	public static final String ROLL_THRUSTER = "Roll Thruster";
	public static final String YAW_THRUSTER = "Yaw Thruster";
	public static final String PITCH_THRUSTER = "Pitch Thruster";
	public static final String UP_DOWN_THRUSTER = "Up/Down Thruster";
	public static final String LEFT_RIGHT_THRUSTER = "Left/Right Thruster";

	private static final Array<String> INTERNAL_COMPONENT_TYPES = Array.with(CargoBay.class.getSimpleName(), PassengerBay.class.getSimpleName(), PowerPlant.class.getSimpleName(), ShieldGenerator.class.getSimpleName(), Weapon.class.getSimpleName());

	public static Ship getStandardShip() {
		Ship ship = new Ship();
		
		ship.parts.add(new ShipPart(
				MAIN_THRUSTER,
				Array.with(Thruster.class.getSimpleName()), 
				1, 1, 6.0f, 
				new Thruster(4.0f)));
		ship.parts.add(new ShipPart(
				ROLL_THRUSTER,
				Array.with(Thruster.class.getSimpleName()), 
				1, 1, 2.0f, 
				new Thruster(2.0f)));
		ship.parts.add(new ShipPart(
				PITCH_THRUSTER,
				Array.with(Thruster.class.getSimpleName()), 
				1, 1, 2.0f, 
				new Thruster(2.0f)));
		ship.parts.add(new ShipPart(
				YAW_THRUSTER,
				Array.with(Thruster.class.getSimpleName()), 
				1, 1, 1.0f, 
				new Thruster(1.0f)));
		ship.parts.add(new ShipPart(
				UP_DOWN_THRUSTER,
				Array.with(Thruster.class.getSimpleName()), 
				1, 1, 0.5f, 
				new Thruster(0.4f)));
		ship.parts.add(new ShipPart(
				LEFT_RIGHT_THRUSTER,
				Array.with(Thruster.class.getSimpleName()), 
				1, 1, 0.5f, 
				new Thruster(0.4f)));
		ship.parts.add(new ShipPart(
			"Hull",
			Array.with(Hull.class.getSimpleName()), 
			1, 1, 1, 
			DEFAULT_HULL));
		ship.parts.add(new ShipPart(
			"Powerplant",
			Array.with(PowerPlant.class.getSimpleName()), 
			1, 1, 3, 
			DEFAULT_POWER_PLANT));
		ship.parts.add(new ShipPart(
			"Weapons",
			Array.with(Weapon.class.getSimpleName()),
			0, 4, 3));
		ship.parts.add(new ShipPart(
			"Main Compartment",
			INTERNAL_COMPONENT_TYPES,
			0, 5, 20f,
			DEFAULT_CARGO_BAY));
		
		ship.update();
		
		return ship;
	}

	public static List<Thruster> getThrusters() {
		return Arrays.asList(
			// thrust
			new Thruster(0.1f),
			new Thruster(0.2f),
			new Thruster(0.3f),
			new Thruster(0.4f),
			new Thruster(0.5f),
			new Thruster(0.6f),
			new Thruster(0.7f),
			new Thruster(0.8f),
			new Thruster(0.9f),
			new Thruster(1.0f),
			new Thruster(1.1f),
			new Thruster(1.2f),
			new Thruster(1.3f),
			new Thruster(1.4f),
			new Thruster(1.5f),
			new Thruster(2.0f),
			new Thruster(2.5f),
			new Thruster(3.0f),
			new Thruster(4.0f),
			new Thruster(5.0f),
			new Thruster(6.0f),
			new Thruster(7.0f),
			new Thruster(8.0f),
			new Thruster(9.0f)
		);
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
		if (Thruster.class.getSimpleName().equals(type)) {
			return getThrusters();
		}
		
		return Collections.emptyList();
	}
}