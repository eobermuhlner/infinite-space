package ch.obermuhlner.infinitespace.util;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

public class Units {
	
	public static final double SECONDS_PER_DAY = 24*60*60;
	public static final double SECONDS_PER_YEAR = SECONDS_PER_DAY * 365.25;
	public static final double LIGHT_SECOND = 299792458;
	public static final double LIGHT_YEAR = LIGHT_SECOND * SECONDS_PER_YEAR;
	public static final double ASTRONOMICAL_UNIT = 149597871E3;
	
	/**
	 * See: http://en.wikipedia.org/wiki/Stefan%E2%80%93Boltzmann_constant
	 * W*m^-2*K^-4
	 */
	public static final double STEFAN_BOLTZMAN_CONSTANT = 5.670373E-8;
	
	public static final double EARTH_ORBIT_RADIUS = 149597890E3;
	public static final double EARTH_ORBIT_PERIOD = 1 * SECONDS_PER_YEAR;
	public static final double EARTH_MASS = 5.9742E24;
	public static final double EARTH_RADIUS = 6378.1E3;
	public static final double EARTH_PERIOD = 1 * SECONDS_PER_DAY;
	
	public static final double JUPITER_MASS = 1.8987E27;
	public static final double JUPITER_RADIUS = 71492.68E3;
	
	public static final double SUN_MASS = 2E30;
	public static final double SUN_RADIUS = 700000E3;
	public static final double SUN_LUMINOSITY = 3.827E26; // W

	private static NumberFormat numberFormat;
	static {
		numberFormat = NumberFormat.getNumberInstance();
	}
	
	private static Unit meterUnits[] = {
		new Unit(LIGHT_YEAR, "lightyears"),
		new Unit(1000, "km"),
		new Unit(1, "m"),
	};

	private static Unit alternateSizeUnits[] = {
		new Unit(SUN_RADIUS, "sun radius"),
		new Unit(JUPITER_RADIUS, "jupiter radius"),
		new Unit(EARTH_RADIUS, "earth radius"),
	};

	private static Unit alternate1OrbitUnits[] = {
		new Unit(LIGHT_YEAR, "lightyears"),
		new Unit(LIGHT_SECOND, "lightseconds"),
	};

	private static Unit alternate2OrbitUnits[] = {
		new Unit(ASTRONOMICAL_UNIT, "earth orbits"),
	};

	private static Unit secondUnits[] = {
		new Unit(365.25*24*60*60, "years"),
		new Unit(24*60*60, "days"),
		new Unit(60*60, "hours"),
		new Unit(60, "minutes"),
		new Unit(1, "seconds"),
	};

	private static Unit kilogramUnits[] = {
		new Unit(1, "kg"),
		new Unit(0.001, "g"),
	};
	
	private static Unit alternateKilogramUnits[] = {
		new Unit(SUN_MASS, "sun mass"),
		new Unit(JUPITER_MASS, "jupiter mass"),
		new Unit(EARTH_MASS, "earth mass"),
	};

	public static String toString(double value) {
		return numberFormat.format(value);
	}
	
	public static String meterSizeToString(double value) {
		return unitToString(value, meterUnits, alternateSizeUnits);
	}
	
	public static String meterOrbitToString(double value) {
		return unitToString(value, meterUnits, alternate1OrbitUnits, alternate2OrbitUnits);
	}
	
	public static String secondsToString (double value) {
		return unitToString(value, secondUnits);
	}

	public static String kilogramsToString (double value) {
		return unitToString(value, kilogramUnits, alternateKilogramUnits);
	}

	public static String kelvinToString(double value) {
		return toString(value) + " K (" + toString(value - 273.16) + " C)";
	}

	public static String volumeToString(double value) {
		return toString(value) + " m^3";
	}

	public static String percentToString(double value) {
		return toString(value * 100) + "%";
	}

	public static String moneyToString(double value) {
		return toString(value) + " $";
	}


	public static String unitToString(double value, Unit units[], Unit[]... alternateUnits) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(unitToString(value, true, units));

		boolean shown = false;
		for (Unit[] alternate : alternateUnits) {
			String earthString = unitToString(value, false, alternate);
			if (earthString != null) {
				if (shown) {
					stringBuilder.append(", ");
				} else {
					stringBuilder.append(" (");
				}
				stringBuilder.append(earthString);
				shown = true;
			}
		}
		if (shown) {
			stringBuilder.append(")");
		}
		
		return stringBuilder.toString();
	}

	private static String unitToString(double value, boolean showLast, Unit units[]) {
		for (int i = 0; i < units.length; i++) {
			Unit unit = units[i];
			if ((showLast && i == units.length-1) || Math.abs(value) > 0.9 * unit.value) {
				return toString(value / unit.value) + " " + unit.name;
			}
		}
		return null;
	}
	
	private static class Unit {
		double value;
		String name;
		
		public Unit(double value, String unit) {
			this.value = value;
			this.name = unit;
		}
	}

	public static String atmosphereToString(List<Tuple2<Molecule, Double>> atmosphere) {
		StringBuilder stringBuilder = new StringBuilder();
		
		for (Tuple2<Molecule, Double> tuple : atmosphere) {
			stringBuilder.append(percentToString(tuple.getValue2()));
			stringBuilder.append(tuple.getValue1().name());
			stringBuilder.append("  ");
		}
		
		return stringBuilder.toString();
	}

}
