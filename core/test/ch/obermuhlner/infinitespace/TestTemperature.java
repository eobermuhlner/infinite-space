package ch.obermuhlner.infinitespace;

import ch.obermuhlner.infinitespace.model.generator.Generator;
import ch.obermuhlner.infinitespace.util.Units;

public class TestTemperature {

	public static void main(String[] args) {
		double temperature = Generator.theoreticalTemperature(Units.SUN_LUMINOSITY, 0.306, Units.ASTRONOMICAL_UNIT);
		System.out.println("Temperature: " + Units.kelvinToString(temperature));
	}
}
