package ch.obermuhlner.infinitespace.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.obermuhlner.infinitespace.Config;

public class Units {
	
	public static final double SECONDS_PER_DAY = 24*60*60;
	public static final double SECONDS_PER_YEAR = SECONDS_PER_DAY * 365.25;
	public static final double LIGHT_SECOND = 299792458;
	public static final double LIGHT_YEAR = LIGHT_SECOND * SECONDS_PER_YEAR;
	public static final double ASTRONOMICAL_UNIT = 149597871E3;
	private static final double CELSIUS_BASE = 273.16;
	
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
	public static final double EARTH_ATMOSPHERE_PRESSURE = 101.325E3; //Pa
	
	public static final double JUPITER_MASS = 1.8987E27;
	public static final double JUPITER_RADIUS = 71492.68E3;
	
	public static final double SUN_MASS = 2E30;
	public static final double SUN_RADIUS = 700000E3;
	public static final double SUN_LUMINOSITY = 3.827E26; // W

	private static final MathContext MC_SIGNIFICANT_DIGITS = new MathContext(3);
	
	private static NumberFormat numberFormat;
	static {
		if (Config.LOCALE == null) {
			numberFormat = NumberFormat.getNumberInstance();
		} else {
			numberFormat = NumberFormat.getNumberInstance(Config.LOCALE);
		}
	}
	
	private static Unit meterUnits[] = {
		new Unit(LIGHT_YEAR, "lightyears"),
		new Unit(1000, "km"),
		new Unit(1, "m"),
	};

	private static Unit meterDistanceUnits[] = {
		new Unit(LIGHT_YEAR, "lightyears"),
		new Unit(LIGHT_SECOND, "lightseconds"),
		new Unit(1000000, "Mm"),
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
		return numberFormat.format(BigDecimal.valueOf(value).round(MC_SIGNIFICANT_DIGITS));
	}
	
	public static String meterSizeToString(double value) {
		return unitToString(value, meterUnits, alternateSizeUnits);
	}
	
	public static String meterOrbitToString(double value) {
		return unitToString(value, meterUnits, alternate1OrbitUnits, alternate2OrbitUnits);
	}
	
	public static String meterDistanceToString(double value) {
		return unitToString(value, meterDistanceUnits);
	}
	
	public static String secondsToString (double value) {
		return unitToString(value, secondUnits);
	}

	public static String kilogramsToString (double value) {
		return unitToString(value, kilogramUnits, alternateKilogramUnits);
	}

	public static String kelvinToString(double value) {
		return toString(value) + " K  (" + toString(value - 273.16) + " C)";
	}

	public static String pascalToString(double value) {
		return toString(value) + " Pa  (" + toString(value / Units.EARTH_ATMOSPHERE_PRESSURE) + " bar)";
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
					stringBuilder.append("  (");
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

	public static String toString(Enum<?> value) {
		return toFirstUpperRestLowerCaseString(value.name().replace('_', ' '));
	}
	
	public static String toFirstUpperRestLowerCaseString(String name) {
		if (name.length() < 2) {
			return name.toUpperCase();
		}
		
		return (name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase());
	}

	private static String[] ROMAN_DIGITS_HUNDREDS = {
		"",
		"C",
		"CC",
		"CCC",
		"CD",
		"D",
		"DC",
		"DCC",
		"DCCC",
		"CM"
	};

	private static String[] ROMAN_DIGITS_TENS = {
		"",
		"X",
		"XX",
		"XXX",
		"XL",
		"L",
		"LX",
		"LXX",
		"LXXX",
		"XC"
	};

	private static String[] ROMAN_DIGITS_ONES = {
		"",
		"I",
		"II",
		"III",
		"IV",
		"V",
		"VI",
		"VII",
		"VIII",
		"IX"
	};
	
	public static String toRomanNumberString(int value) {
		StringBuilder roman = new StringBuilder();
		
		while (value >= 1000) {
			roman.append('M');
			value -= 1000;
		}

		int hundreds = value / 100;
		roman.append(ROMAN_DIGITS_HUNDREDS[hundreds]);
		value -= hundreds * 100;

		int tens = value / 10;
		roman.append(ROMAN_DIGITS_TENS[tens]);
		value -= tens * 10;

		roman.append(ROMAN_DIGITS_ONES[value]);

		return roman.toString();
	}

	public static String atmosphereToString(Map<Molecule, Double> atmosphere) {
		StringBuilder stringBuilder = new StringBuilder();
		
		if (atmosphere != null) {
			List<Map.Entry<Molecule, Double>> entries = new ArrayList<Map.Entry<Molecule,Double>>(atmosphere.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<Molecule,Double>>() {
				@Override
				public int compare(Entry<Molecule, Double> o1, Entry<Molecule, Double> o2) {
					return -Double.compare(o1.getValue(), o2.getValue());
				}
			});
			
			boolean first = true;
			for (Map.Entry<Molecule, Double> entry : entries) {
				if (first) {
					first = false;
				} else {
					stringBuilder.append(" | ");
				}
				stringBuilder.append(percentToString(entry.getValue()));
				stringBuilder.append(" ");
				stringBuilder.append(entry.getKey().name());
			}
		}
		
		return stringBuilder.toString();
	}

	public static double celsiusToKelvin(double celsius) {
		return celsius + CELSIUS_BASE;
	}
	
	public static double roundToSignificantDigits(double num, int n) {
	    if(num == 0) {
	        return 0;
	    }

	    final double d = Math.ceil(Math.log10(num < 0 ? -num: num));
	    final int power = n - (int) d;

	    final double magnitude = Math.pow(10, power);
	    final long shifted = Math.round(num*magnitude);
	    return shifted/magnitude;
	}
	
	private static class Unit {
		double value;
		String name;
		
		public Unit(double value, String unit) {
			this.value = value;
			this.name = unit;
		}
	}

}
