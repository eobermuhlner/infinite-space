package ch.obermuhlner.infinitespace;


import ch.obermuhlner.infinitespace.model.random.Random;
import ch.obermuhlner.infinitespace.model.random.Seed;
import ch.obermuhlner.infinitespace.model.universe.Planet;
import ch.obermuhlner.infinitespace.model.universe.SpaceStation;
import ch.obermuhlner.infinitespace.model.universe.Star;
import ch.obermuhlner.infinitespace.model.universe.StarSystem;

public class TestNameGenerator {

	public static void main (String[] args) {
		NameGenerator nameGenerator = new NameGenerator();
		
		//Seed seed = new Seed(0);
		Seed seed = new Seed(System.currentTimeMillis());
		Random random = seed.getRandom();
		
		for (int i = 0; i < 100; i++) {
			System.out.println("GENERIC " + nameGenerator.generateGenericName(random));
		}
		System.out.println();
		
		for (int i = 0; i < 100; i++) {
			System.out.println("HUMAN " + nameGenerator.generateFirstName(random) + " " + nameGenerator.generateLastName(random));
		}
		System.out.println();
		
		StarSystem system = new StarSystem(new Seed(0));
		Star star = new Star(system, 0);
		Planet planet = new Planet(star, 0);
		SpaceStation station = new SpaceStation(star, 0);
		SpaceStation stationRing = new SpaceStation(star, 1);
		stationRing.type = SpaceStation.Type.RING;
		
		for (int i = 0; i < 20; i++) {
			System.out.println("STAR " + nameGenerator.generateNodeName(random, star));
		}
		System.out.println();

		for (int i = 0; i < 20; i++) {
			System.out.println("PLANET " + nameGenerator.generateNodeName(random, planet));
		}
		System.out.println();

		for (int i = 0; i < 20; i++) {
			System.out.println("STATION " + nameGenerator.generateNodeName(random, station));
		}
		System.out.println();

		for (int i = 0; i < 20; i++) {
			System.out.println("STATION RING " + nameGenerator.generateNodeName(random, stationRing));
		}
		System.out.println();
	}
}
