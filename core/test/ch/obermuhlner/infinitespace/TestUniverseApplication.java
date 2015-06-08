package ch.obermuhlner.infinitespace;

import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.OrbitingNode;
import ch.obermuhlner.infinitespace.model.OrbitingSpheroidNode;
import ch.obermuhlner.infinitespace.model.UniverseModel;
import ch.obermuhlner.infinitespace.model.universe.AsteroidBelt;
import ch.obermuhlner.infinitespace.model.universe.Planet;
import ch.obermuhlner.infinitespace.model.universe.SpaceStation;
import ch.obermuhlner.infinitespace.model.universe.Star;
import ch.obermuhlner.infinitespace.util.Units;

public class TestUniverseApplication {

	private UniverseModel universeModel = new UniverseModel();

	private void printUniverse () {
		for (int i = 1; i < 2; i++) {
			System.out.println("##########################################################################");
			System.out.println("### Star System " + i);
			universeModel.setStarSystemIndex(i);
			Iterable<Node> nodes = universeModel.getUniverse();
			for (Node node : nodes) {
				//System.out.println(node.getFullName());
				printNode(node);
			}
			System.out.println();
		}
	}
	
	private void printNode(Node node) {
//		if (node instanceof Planet) {
//			Planet planet = (Planet)node;
//			if (planet.supportsLife) {
//				printInfo(node);
//			}
//		}
		printInfo(node);
	}

	private void printInfo(Node node) {
		println(node.getClass().getSimpleName(), node.getFullName() + " " + node.seed);
		
		if (node instanceof Star) {
			Star star = (Star)node;
			println("Type", star.type);
			println("Temperature", Units.kelvinToString(star.temperature));
		}
		
		if (node instanceof Planet) {
			Planet planet = (Planet)node;
			println("Type", planet.type);
			println("Breathable Atmosphere", planet.breathableAtmosphere);
			println("Water", Units.percentToString(planet.water));
			println("Supports Life", planet.supportsLife);
			println("Has Life", planet.hasLife);
		}

		if (node instanceof AsteroidBelt) {
			AsteroidBelt asteroidBelt = (AsteroidBelt)node;
			println("Width", Units.meterSizeToString(asteroidBelt.width));
			println("Height", Units.meterSizeToString(asteroidBelt.height));
			println("Density", Units.toString(asteroidBelt.density) + "1/m^3");
			println("Average Radius", Units.meterSizeToString(asteroidBelt.averageRadius));
		}

		if (node instanceof SpaceStation) {
			SpaceStation spaceStation = (SpaceStation)node;
			
			println("Type", spaceStation.type);
			println("Width", Units.meterSizeToString(spaceStation.width));
			println("Height", Units.meterSizeToString(spaceStation.height));
			println("Length", Units.meterSizeToString(spaceStation.length));
			println("Starport", spaceStation.starport);
		}
		
		if (node instanceof OrbitingSpheroidNode) {
			OrbitingSpheroidNode orbitingSpheroidNode = (OrbitingSpheroidNode)node;
			println("Radius", Units.meterSizeToString(orbitingSpheroidNode.radius));
		}

		if (node instanceof OrbitingNode) {
			OrbitingNode orbitingNode = (OrbitingNode)node;
			println("Mass", Units.kilogramsToString(orbitingNode.mass));
			println("Orbit Radius", Units.meterOrbitToString(orbitingNode.orbitRadius));
			println("Orbit Period", Units.secondsToString(orbitingNode.orbitPeriod));
			println("Rotation Period", Units.secondsToString(orbitingNode.rotation));
			if (orbitingNode.population != null) {
				println("Population", Units.toString(orbitingNode.population.population));
				println("Industry", orbitingNode.population.industry);
				println("Commodities", orbitingNode.population.commodities);
			}
		}
		System.out.println();
	}
	
	private void println (String string, Object object) {
		System.out.printf("%-25s %s\n", string, String.valueOf(object));
	}

	public static void main (String[] args) {
		TestUniverseApplication testApplication = new TestUniverseApplication();
		
		testApplication.printUniverse();
	}

}
