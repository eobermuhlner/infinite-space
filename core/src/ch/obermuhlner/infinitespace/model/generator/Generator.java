package ch.obermuhlner.infinitespace.model.generator;

import static ch.obermuhlner.infinitespace.model.random.Random.p;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ch.obermuhlner.infinitespace.CommodityItem;
import ch.obermuhlner.infinitespace.Config;
import ch.obermuhlner.infinitespace.NameGenerator;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.OrbitingNode;
import ch.obermuhlner.infinitespace.model.OrbitingSpheroidNode;
import ch.obermuhlner.infinitespace.model.random.Random;
import ch.obermuhlner.infinitespace.model.random.Seed;
import ch.obermuhlner.infinitespace.model.universe.AsteroidBelt;
import ch.obermuhlner.infinitespace.model.universe.Galaxy;
import ch.obermuhlner.infinitespace.model.universe.GalaxyQuadrantX;
import ch.obermuhlner.infinitespace.model.universe.GalaxyQuadrantY;
import ch.obermuhlner.infinitespace.model.universe.GalaxyQuadrantZ;
import ch.obermuhlner.infinitespace.model.universe.Planet;
import ch.obermuhlner.infinitespace.model.universe.SpaceStation;
import ch.obermuhlner.infinitespace.model.universe.Star;
import ch.obermuhlner.infinitespace.model.universe.StarSystem;
import ch.obermuhlner.infinitespace.model.universe.Universe;
import ch.obermuhlner.infinitespace.model.universe.population.Commodity;
import ch.obermuhlner.infinitespace.model.universe.population.Industry;
import ch.obermuhlner.infinitespace.model.universe.population.Population;
import ch.obermuhlner.infinitespace.util.MathUtil;
import ch.obermuhlner.infinitespace.util.Molecule;
import ch.obermuhlner.infinitespace.util.Tuple2;
import ch.obermuhlner.infinitespace.util.Units;

import com.badlogic.gdx.utils.Array;

public class Generator {

	private static final double MAX_ANGLE = 2 * Math.PI;
	
	private static final int GALAXY_QUADRANTS = 3;
	private static final int GALAXY_QUADRANTS_OFFSET = 1;

	private static final double GALAXY_QUADRANT_DIAMETER = 10 * Units.LIGHT_YEAR;

	private static final long SEED_SUPPLY = 1;
	private static final long SEED_DEMAND = 2;
	private static final long SEED_PRICE = 3;

	private final Map<Seed, Node> storedNodes = new HashMap<Seed, Node>();
	
	private NameGenerator nameGenerator;
	
	public Generator () {
		//storeSolarSystem();
	}
	
	private NameGenerator getNameGenerator() {
		if (nameGenerator == null) {
			nameGenerator = new NameGenerator();
		}
		return nameGenerator;
	}
	
	public int getChildCount(Node node) {
		return node.getChildCount(this);
	}
	
	public Node getChild(Node node, int index) {
		Seed childSeed = new Seed(node.seed, index);
		
		if (storedNodes.containsKey(childSeed)) {
			Node childNode = storedNodes.get(childSeed);
			childNode.parent = node;
			return childNode;
		}

		Node childNode = node.getChild(this, index);
		return childNode;
	}
	
	private void store(Node node) {
		storedNodes.put(node.seed, node);
	}
	
	public Universe generateUniverse(long index) {
		if (storedNodes.size() == 0) {
			storeSolarSystem();
		}

		Universe universe = new Universe(index);
		universe.childCount = 1;
		return universe;
	}
	
	public Galaxy generateGalaxy(Node parent, long index) {
		Galaxy galaxy = new Galaxy(parent, index);
		galaxy.childCount = GALAXY_QUADRANTS;
		return galaxy;
	}

	public GalaxyQuadrantX generateGalaxyQuadrantX(Node parent, long index) {
		GalaxyQuadrantX galaxyQuadrantX = new GalaxyQuadrantX(parent, index);
		galaxyQuadrantX.childCount = GALAXY_QUADRANTS;
		galaxyQuadrantX.quadrantX = (int) index - GALAXY_QUADRANTS_OFFSET;
		return galaxyQuadrantX;
	}

	public GalaxyQuadrantY generateGalaxyQuadrantY(Node parent, long index) {
		GalaxyQuadrantY galaxyQuadrantY = new GalaxyQuadrantY(parent, index);
		galaxyQuadrantY.childCount = GALAXY_QUADRANTS;
		galaxyQuadrantY.quadrantY = (int) index - GALAXY_QUADRANTS_OFFSET;
		return galaxyQuadrantY;
	}

	public GalaxyQuadrantZ generateGalaxyQuadrantZ(Node parent, long index) {
		GalaxyQuadrantZ galaxyQuadrantZ = new GalaxyQuadrantZ(parent, index);
		Random random = galaxyQuadrantZ.seed.getRandom();
		galaxyQuadrantZ.childCount = random.nextInt(10) + 3;
		galaxyQuadrantZ.quadrantZ = (int) index - GALAXY_QUADRANTS_OFFSET;
		return galaxyQuadrantZ;
	}

	public StarSystem generateStarSystem(Node parent, long index) {
		StarSystem starSystem = new StarSystem(parent, index);
		Random random = starSystem.seed.getRandom();
		
		@SuppressWarnings("unchecked")
		int childCount = random.<Integer> nextProbability(p(89, 1), p(10, 2), p(1, 3));
		starSystem.childCount = childCount;
		
		starSystem.x = random.nextDouble(GALAXY_QUADRANT_DIAMETER);
		starSystem.y = random.nextDouble(GALAXY_QUADRANT_DIAMETER);
		starSystem.z = random.nextDouble(GALAXY_QUADRANT_DIAMETER);
		starSystem.angleX = random.nextDouble(MAX_ANGLE);
		starSystem.angleY = random.nextDouble(MAX_ANGLE);
		starSystem.angleZ = random.nextDouble(MAX_ANGLE);
		
		return starSystem;
	}

	/* Temperature
	 * O	33,000 K or more	Zeta Ophiuchi
	 * B	10,500-30,000 K	Rigel
	 * A	7,500-10,000 K	Altair
	 * F	6,000-7,200 K	Procyon A
	 * G	5,500-6,000 K	Sun
	 * K	4,000-5,250 K	Epsilon Indi
	 * M	2,600-3,850 K	
	 */
	@SuppressWarnings("unchecked")
	public Star generateStar(StarSystem parent, long index) {
		Star star = new Star(parent, index);
		Random random = star.seed.getRandom();
		
		star.name = getNameGenerator().generateNodeName(random, star);
		
		if (parent.childCount == 1) {
			star.orbitRadius = 0;
		} else {
			star.orbitRadius = random.nextDouble(0.01*Units.LIGHT_YEAR, 0.1*Units.LIGHT_YEAR);			
		}
		star.rotation = random.nextGaussian(25);

		star.type = random.nextProbability(
				p(10, Star.Type.WHITE_DWARF),
				p(20, Star.Type.MAIN_SEQUENCE),
				p(6, Star.Type.SUB_GIANT),
				p(2, Star.Type.GIANT),
				p(1, Star.Type.SUPER_GIANT));
		
		if (Config.DEBUG_FORCE_STAR_TYPE != null) {
			star.type = Config.DEBUG_FORCE_STAR_TYPE;
		}
		
		switch (star.type) {
		case BROWN_DWARF:
			star.mass = random.nextGaussian(0.2*Units.SUN_MASS); 
			star.radius = random.nextDouble(0.01*Units.SUN_RADIUS);
			star.temperature = random.nextDouble(1000, 2600);
			star.childCount = random.nextInt (1, 3);
			break;
		case WHITE_DWARF:
			star.mass = random.nextGaussian(0.2*Units.SUN_MASS); 
			star.radius = random.nextDouble(0.01*Units.SUN_RADIUS);
			star.temperature = random.nextDouble(2600, 5000);
			star.childCount = random.nextInt (2, 5);
			break;
		case MAIN_SEQUENCE:
			star.mass = random.nextDouble(0.1*Units.SUN_MASS, 10*Units.SUN_MASS); 
			star.radius = random.nextDouble(0.1*Units.SUN_RADIUS, 10*Units.SUN_RADIUS);
			star.temperature = random.nextDouble(2600, 33000);
			break;
		case SOL_LIKE:
			star.mass = random.nextDouble(0.8*Units.SUN_MASS, 1.2*Units.SUN_MASS); 
			star.radius = random.nextDouble(0.8*Units.SUN_RADIUS, 1.2*Units.SUN_RADIUS);
			star.temperature = random.nextDouble(4000, 6000);
			break;
		case SUB_GIANT:
			star.mass = random.nextGaussian(3*Units.SUN_MASS); 
			star.radius = random.nextGaussian(10*Units.SUN_RADIUS);
			star.temperature = random.nextDouble(2600, 6000);
			break;
		case GIANT:
			star.mass = random.nextGaussian(5*Units.SUN_MASS); 
			star.radius = random.nextGaussian(30*Units.SUN_RADIUS);
			star.temperature = random.nextDouble(2600, 6000);
			break;
		case SUPER_GIANT:
			star.mass = random.nextGaussian(10*Units.SUN_MASS); 
			star.radius = random.nextGaussian(60*Units.SUN_RADIUS);
			star.temperature = random.nextDouble(2600, 6000);
			break;
		}

		// TODO planet count depends on radius of star and number of stars in system (+1 starport)
		if (parent.childCount > 1) {
			star.childCount = random.nextInt(1, 4);
		} else {
			star.childCount = random.nextInt(6, 13);		
		}

		return star;
	}

	public Node generateStarChild(Star parent, long index) {
		Seed seed = new Seed(parent.seed, index);
		Random random = seed.getRandom();
		
		int childCount = parent.getChildCount(this);
		if (index == childCount - 1) {
			return generateSpaceStation(parent, index);
		}
		
		if (random.nextBoolean(0.1)) {
			return generateAsteroidBelt(parent, seed, index, random);
		} else {
			return generatePlanet(parent, seed, index, random);
		}
	}
	
	public Node generatePlanetChild(Planet parent, long index) {
		Seed seed = new Seed(parent.seed, index);
		Random random = seed.getRandom();

		int childCount = parent.getChildCount(this);
		if (index == childCount - 1) {
			return generateSpaceStation(parent, index);
		}

		if (index == 0) {
			if (parent instanceof Planet) {
				Planet parentPlanet = (Planet)parent;
				float probability = parentPlanet.type == Planet.Type.GAS ? 0.3f : 0.05f;
				if (random.nextBoolean(probability)) {
					return generateAsteroidBelt(parent, index);
				}
			}
		}
		return generatePlanet(parent, index);
	}

	public AsteroidBelt generateAsteroidBelt(Node parent, long index) {
		Seed seed = new Seed(parent.seed, index);
		Random random = seed.getRandom();
		
		return generateAsteroidBelt(parent, seed, index, random);
	}

	public AsteroidBelt generateAsteroidBelt(Node parent, Seed seed, long index, Random random) {
		AsteroidBelt belt = new AsteroidBelt(parent, seed);
		Random parentRandom = seed.getRandom();
		
		boolean parentIsStar = parent instanceof Star;
		belt.orbitRadius = calculateOrbitRadius(parent, parentRandom, index);
		belt.averageRadius = parentIsStar ? random.nextGaussian(10) : random.nextGaussian(150);
		belt.width = random.nextGaussian(belt.orbitRadius * 0.001);
		belt.height = random.nextGaussian(belt.orbitRadius * 0.00001);
		belt.density = parentIsStar ? random.nextFloat(0.1f, 0.3f) : random.nextFloat(0.5f, 0.95f); 
		//TODO belt.density = random.nextGaussian(1/(Math.pow(belt.averageRadius*10,3)));
		
		return belt;
	}
	
	public Planet generatePlanet(OrbitingSpheroidNode parent, long index) {
		Seed seed = new Seed(parent.seed, index);
		Random random = seed.getRandom();
		
		return generatePlanet(parent, seed, index, random);
	}

	@SuppressWarnings("unchecked")
	public Planet generatePlanet(OrbitingSpheroidNode parent, Seed seed, long index, Random random) {
		Planet planet = new Planet(parent, seed);
		Random parentRandom = seed.getRandom();

		planet.name = getNameGenerator().generateNodeName(random, planet);
		
		planet.orbitRadius = calculateOrbitRadius(parent, parentRandom, index);
		planet.orbitStartAngle = random.nextDouble(2 * Math.PI);
		planet.rotation = random.nextGaussian(25);
		
		Star star = findParentStar(planet);
		double starDistance = accumulateOrbitRadius(planet, star);
		double starLumininosity = star.getLuminosity();
		double theoreticalTemperature = theoreticalTemperature(starLumininosity, 0.1, starDistance);
		boolean lifeSupportingZone = theoreticalTemperature > Units.celsiusToKelvin(-50) && theoreticalTemperature < Units.celsiusToKelvin(60);
		
		// set Planet: type, mass, radius, childCount
		if (parent.mass > Units.SUN_MASS * 0.05) {
			int firstGasPlanet = parentRandom.nextInt(1, 7);
			int lastGasPlanet = parentRandom.nextInt(8, 11);
			if ((index > firstGasPlanet && index < lastGasPlanet) || random.nextBoolean(0.01)) {
				planet.type = Planet.Type.GAS;
				planet.mass = random.nextGaussian (Units.JUPITER_MASS);
				planet.radius = random.nextGaussian (Units.JUPITER_RADIUS);
				planet.childCount = random.nextInt (3, 6);
			} else {
				planet.type = Planet.Type.STONE;
				planet.mass = random.nextGaussian (Units.EARTH_MASS);
				planet.radius = random.nextGaussian (Units.EARTH_RADIUS);
				planet.childCount = random.nextInt (0, 2);
			}
		} else {
			planet.type = Planet.Type.STONE;
			planet.mass = random.nextGaussian (parent.mass / 10); // TODO not linear!
			planet.radius = random.nextGaussian (parent.radius / 10);
			planet.childCount = 0;
		}

		// set Planet orbit
		planet.orbitPeriod = Math.pow(planet.orbitRadius/Units.ASTRONOMICAL_UNIT, 3.0/2.0) * Units.SECONDS_PER_YEAR;
		planet.rotation = random.nextGaussian (1 * Units.SECONDS_PER_DAY); // TODO rotation of tide locked moons

		// set Planet atmosphere
		switch(planet.type) {
		case GAS: {
			planet.core = new Array<Planet.PartInfo>();
			double coreTemperature = random.nextDouble(30000, 50000);
			// TODO core temperature depends on size and age of system
			planet.core.add(new Planet.PartInfo(
					"Rocky Core",
					"Liquid inner core.",
					random.nextDouble(0.02, 0.05) * planet.radius,
					coreTemperature,
					null));
			planet.core.add(new Planet.PartInfo(
					"Metallic Hydrogen",
					"...",
					random.nextDouble(0.75, 0.80) * planet.radius,
					coreTemperature * 0.25,
					null));
			planet.core.add(new Planet.PartInfo(
					"Liquid Hydrogen",
					"...",
					random.nextDouble(0.85, 0.95) * planet.radius,
					random.nextDouble(50, 200), // unknown values
					null));
			planet.core.add(new Planet.PartInfo(
					"Gassy Hydrogen",
					"...",
					random.nextDouble(0.96, 0.98) * planet.radius,
					random.nextDouble(20, 50), // unknown values
					null));
			planet.core.add(new Planet.PartInfo(
					"Cloud Layers",
					"...",
					planet.radius,
					random.nextDouble(20, 50), // unknown values
					null));
			
			// jupiter-like atmosphere
			planet.atmosphere = random.nextProbabilityMap(
					p(random.nextGaussian(90), Molecule.H2),
					p(random.nextGaussian(10), Molecule.He),
					p(random.nextGaussian(0.3), Molecule.CH4),
					p(random.nextGaussian(0.003), Molecule.NH3),
					p(random.nextGaussian(0.0006), Molecule.C2H6)
					);
			planet.albedo = random.nextDouble(0.27, 0.34);
			break;
			}
		default :
		case ICE: //FIXME ??
		case STONE: {
			planet.core = new Array<Planet.PartInfo>();
			double coreTemperature = random.nextDouble(4800, 5400);
			// TODO core temperature depends on size and age of system
			Map<Molecule, Double> coreComposition = random.nextProbabilityMap(
					p(random.nextGaussian(60), Molecule.Fe),
					p(random.nextGaussian(40), Molecule.Ni)
					);
			planet.core.add(new Planet.PartInfo(
					"Inner Core",
					"Liquid inner core.",
					random.nextDouble(0.1, 0.3) * planet.radius,
					coreTemperature,
					coreComposition));
			planet.core.add(new Planet.PartInfo(
					"Outer Core",
					"Liquid outer core.",
					random.nextDouble(0.4, 0.6) * planet.radius,
					coreTemperature * 0.8,
					coreComposition));
			planet.core.add(new Planet.PartInfo(
					"Mantle",
					"The mantle.",
					planet.radius - random.nextDouble(10, 50),
					coreTemperature * 0.3,
					random.nextProbabilityMap(
							p(random.nextGaussian(45), Molecule.SiO2),
							p(random.nextGaussian(40), Molecule.MgO),
							p(random.nextGaussian(8), Molecule.FeO),
							p(random.nextGaussian(4), Molecule.Al2O3),
							p(random.nextGaussian(3), Molecule.CaO),
							p(random.nextGaussian(0.5), Molecule.Na2O),
							p(random.nextGaussian(0.05), Molecule.K2O)
							)));
			planet.core.add(new Planet.PartInfo(
					"Crust",
					"The crust.",
					planet.radius,
					300.0,
					random.nextProbabilityMap(
							p(random.nextGaussian(60), Molecule.SiO2),
							p(random.nextGaussian(16), Molecule.Al2O3),
							p(random.nextGaussian(6), Molecule.CaO),
							p(random.nextGaussian(5), Molecule.MgO),
							p(random.nextGaussian(3), Molecule.FeO),
							p(random.nextGaussian(2), Molecule.K2O),
							p(random.nextGaussian(0.7), Molecule.TiO2),
							p(random.nextGaussian(0.1), Molecule.P2O5)
							)));
			
			planet.atmospherePressure = MathUtil.smoothstep(Units.EARTH_MASS / 10, Units.EARTH_MASS * 10, planet.mass) * Units.EARTH_ATMOSPHERE_PRESSURE; // FIXME real function for atmosphere density
			planet.breathableAtmosphere = planet.atmospherePressure > Units.EARTH_ATMOSPHERE_PRESSURE * 0.1 && random.nextBoolean(0.1);
			if (lifeSupportingZone) {
				planet.breathableAtmosphere = random.nextBoolean(0.8);
				if (planet.breathableAtmosphere) {
					planet.water = random.nextDouble();
				} else {
					if (random.nextBoolean(0.8)) {
						planet.water = random.nextDouble();
					}
				}
			}
			if (planet.breathableAtmosphere) {
				// terra like atmosphere
				planet.atmosphere = random.nextProbabilityMap(
						p(random.nextGaussian(75), Molecule.N2),
						p(random.nextGaussian(20), Molecule.O2),
						p(random.nextGaussian(0.01), Molecule.Ar),
						p(random.nextGaussian(0.005), Molecule.CO2)
						);
			} else {
				if (planet.atmospherePressure > Units.EARTH_ATMOSPHERE_PRESSURE * 0.1 ) {
					if (random.nextBoolean(0.5)) {
						// mars/venus-like atmosphere
						planet.atmosphere = random.nextProbabilityMap(
								p(random.nextGaussian(95), Molecule.CO2),
								p(random.nextGaussian(3), Molecule.N2),
								p(random.nextGaussian(0.001), Molecule.Ar),
								p(random.nextGaussian(0.001), Molecule.O2),
								p(random.nextGaussian(0.0001), Molecule.SO2)
								);
						
					} else {
						// titan-line atmosphere
						planet.atmosphere = random.nextProbabilityMap(
								p(random.nextGaussian(98), Molecule.N2),
								p(random.nextGaussian(1), Molecule.CH4),
								p(random.nextGaussian(0.5), Molecule.H2)
								);
					}
				}
			}
			planet.albedo = planet.water > 0 ? random.nextDouble(0.3, 0.95) : random.nextDouble(0.05, 0.2);
			break;
			}
		}
		
		// set Planet population
		if (planet.breathableAtmosphere && lifeSupportingZone) {
			planet.supportsLife = true;
			planet.hasLife = random.nextBoolean(0.9);
			if (planet.hasLife && random.nextBoolean(0.9)) {
				generatePopulationNicePlanet(planet, random);
			}
		} else {
			if (random.nextBoolean(planet.breathableAtmosphere || lifeSupportingZone ? 0.5 : 0.2)) {
				generatePopulationAcceptablePlanet(planet, random);
			}
		}

		// set Planet temperature
		// FIXME albedo + temperature
		planet.temperature = surfaceTemperature(planet); 
		
		return planet;
	}

	/**
	 * See: http://www.astronomynotes.com/solarsys/s3c.htm
	 * 
	 * @param lumininosity the luminosity of the sun
	 * @param albedo the albedo of the planet (1.0 = 100% reflection)
	 * @param distance distance between sun and planet
	 * @return the theoretical temperature of the planet in K
	 */
	public static double theoreticalTemperature(double lumininosity, double albedo, double distance) {
		double value1 = (lumininosity * (1 - albedo) / (16 * Units.STEFAN_BOLTZMAN_CONSTANT * Math.PI));
		return Math.pow(value1, 0.25) / Math.sqrt(distance);
	}
	
	public static double surfaceTemperature(Planet planet) {
		Star star = findParentStar(planet);
		
		double albedo = Math.min(1.0, planet.albedo);
		double distance = accumulateOrbitRadius(planet, star) - star.radius;
		double theoreticalTemperature = theoreticalTemperature(star.getLuminosity(), albedo, distance);
		
		// FIXME apply correct model of greenhouse effect
		if (planet.breathableAtmosphere) {
			theoreticalTemperature *= 1.1;
		}
		
		return theoreticalTemperature;
	}
	
	private static Star findParentStar (Planet planet) {
		Node parent = planet.parent;
		while (parent != null) {
			if (parent instanceof Star) {
				return (Star) parent;
			}
			parent = parent.parent;
		}
		
		return null;
	}
	
	private static double accumulateOrbitRadius(Planet planet, Node center) {
		double orbitRadius = planet.orbitRadius;
		Node parent = planet.parent;
		while (parent != null && parent != center) {
			if (parent instanceof OrbitingNode) {
				OrbitingNode orbitingNode = (OrbitingNode)parent;
				orbitRadius += orbitingNode.orbitRadius;
			}
			parent = parent.parent;
		}
		return orbitRadius;
	}

	private double calculateOrbitRadius (Node parent, Random parentRandom, long index) {
		double baseDist = parent instanceof Planet ? ((Planet)parent).radius * parentRandom.nextInt (5, 20) : Units.ASTRONOMICAL_UNIT ;
		if (Config.DEBUG_LINEAR_ORBITS) {
			return baseDist + index * baseDist * 0.25;
		}
		double distConst = parentRandom.nextGaussian (0.4, 0.01) * baseDist;
		double distFactor = parentRandom.nextGaussian (0.3, 0.01) * baseDist;
		double orbitRadius = distConst + distFactor * (index == 0 ? 0 : Math.pow(2, index));
		
		return orbitRadius;
	}
	
	public SpaceStation generateSpaceStation(OrbitingSpheroidNode parent, long index) {
		Seed seed = new Seed(parent.seed, index);
		Random random = seed.getRandom();
		
		return generateSpaceStation(parent, seed, index, random);
	}

	@SuppressWarnings("unchecked")
	public SpaceStation generateSpaceStation(OrbitingSpheroidNode parent, Seed seed, long index, Random random) {
		SpaceStation station = new SpaceStation(parent, seed);
		Random parentRandom = seed.getRandom();

		station.name = getNameGenerator().generateNodeName(random, station);
		station.orbitRadius = calculateOrbitRadius(parent, parentRandom, 3); // always at third orbit (similar to earth orbit)
		
		if (parent instanceof Star) {
			station.starport = true;
		}
		
		station.type = random.nextProbability(
			p(3, SpaceStation.Type.SPHERE),
			p(3, SpaceStation.Type.VARIABLE_CYLINDER),
			p(5, SpaceStation.Type.CYLINDER),
			p(30, SpaceStation.Type.RING),
			p(10, SpaceStation.Type.BALANCED),
			p(1, SpaceStation.Type.CUBE),
			p(20, SpaceStation.Type.BLOCKY),
			p(3, SpaceStation.Type.CONGLOMERATE)
			);

		if (Config.DEBUG_FORCE_SPACE_STATION_TYPE != null) {
			station.type = Config.DEBUG_FORCE_SPACE_STATION_TYPE;
		}
		
		station.width = random.nextDouble(100, 500);
		station.height = random.nextDouble(100, 500);
		station.length = random.nextDouble(100, 500);
		
		switch(station.type) {
		case SPHERE:
		case CYLINDER:
		case VARIABLE_CYLINDER:
			station.width = station.length;
			break;
		case RING:
			station.length *= random.nextDouble(2, 3);
			station.width = station.length;
			break;
		case BALANCED:
			station.height = station.width;
			station.length *= random.nextDouble(4, 8);
			break;
		case BLOCKY:
		case CONGLOMERATE:
		case CUBE:
			// no constraints on size
			break;
		}
		
		switch(station.type) {
		case CYLINDER:
		case RING:
		case BALANCED:
			station.rotation = 60 * 60; // TODO rotation of space station
			break;
		default:
			// TODO rotation ? 
		}
		
		double volume = station.type.volume(station.width, station.height, station.length);
		
		station.mass = volume * random.nextInt(900, 1500);
		
		generatePopulation(station, random, volume);
		
		return station;
	}

	@SuppressWarnings("unchecked")
	private void generatePopulationNicePlanet (Planet planet, Random random) {
		planet.population = new Population();
		planet.population.techLevel = random.nextProbability(
			p(5, TechLevel.STONE),
			p(10, TechLevel.IRON),
			p(10, TechLevel.BRONZE),
			p(20, TechLevel.STEEL),
			p(20, TechLevel.MACHINES),
			p(30, TechLevel.ELECTRONICS),
			p(5, TechLevel.NANO)
			);
		planet.population.population = random.nextInt(2000, (int)(10000000 * planet.water + 1000000));
		planet.population.industry = random.nextProbabilityMap(
			p(planet.water * 60, Industry.AGRICULTURE),
			p(planet.water * 60, Industry.FISHING),
			p(planet.water * random.nextDouble(30), Industry.TOURISM),
			p(random.nextDouble(1, 20), Industry.INDUSTRY),
			p(random.nextDouble(1, 10), Industry.MINING),
			p(random.nextDouble(1, 8), Industry.REFINERY)
			);
		generateCommodities(planet, planet.population, random);
	}

	@SuppressWarnings("unchecked")
	private void generatePopulationAcceptablePlanet (Planet planet, Random random) {
		planet.population = new Population();
		planet.population.techLevel = random.nextProbability(
			p(10, TechLevel.MACHINES),
			p(20, TechLevel.ELECTRONICS),
			p(5, TechLevel.NANO)
			);
		planet.population.population = random.nextInt(500, (int)(300000 * planet.water + (planet.breathableAtmosphere ? 100000 : 20000)));
		planet.population.industry = random.nextProbabilityMap(
			p(10, Industry.MINING),
			p(20, Industry.REFINERY),
			p(random.nextDouble(20), Industry.INDUSTRY),
			p(random.nextDouble(2), Industry.PIRACY)
			);
		generateCommodities(planet, planet.population, random);
	}

	@SuppressWarnings("unchecked")
	private void generatePopulationHorriblePlanet (Planet planet, Random random) {
		planet.population = new Population();
		planet.population.techLevel = random.nextProbability(
			p(10, TechLevel.MACHINES),
			p(20, TechLevel.ELECTRONICS),
			p(5, TechLevel.NANO)
			);
		planet.population.population = random.nextInt(500, (int)(100000 * planet.water + (planet.breathableAtmosphere ? 10000 : 5000)));
		planet.population.industry = random.nextProbabilityMap(
			p(50, Industry.MINING),
			p(10, Industry.REFINERY),
			p(random.nextDouble(1), Industry.INDUSTRY),
			p(random.nextDouble(20), Industry.PIRACY)
			);
		generateCommodities(planet, planet.population, random);
	}

	@SuppressWarnings("unchecked")
	private void generatePopulation (SpaceStation station, Random random, double volume) {
		double maxPopulation = volume * 0.1;
		station.population = new Population();
		station.population.techLevel = random.nextProbability(
			p(10, TechLevel.MACHINES),
			p(20, TechLevel.ELECTRONICS),
			p(5, TechLevel.NANO)
			);
		station.population.population = random.nextInt((int)(maxPopulation * 0.5), (int)maxPopulation);
		station.population.industry = random.nextProbabilityMap(
			p(station.type.allowsAgriculture() ? random.nextDouble(60) : 0, Industry.AGRICULTURE),
			p(random.nextDouble(80), Industry.TRADE),
			p(random.nextDouble(1, 20), Industry.INDUSTRY),
			p(random.nextDouble(1, 2), Industry.REFINERY)
			);
		generateCommodities(station, station.population, random);
	}
	
	private void generateCommodities (Node node, Population population, Random random) {
		population.commodities = new ArrayList<CommodityItem>();
		for (Commodity commodity : Commodity.values()) {
			if (population.hasCommodity(commodity)) {
				double production = population.getSupply(commodity);
				if (production > 0) {
					CommodityItem item = new CommodityItem();
					item.name = generateCommodityName(node, commodity, random);
					item.commodity = commodity;
					double amount = population.population * 0.01 * production * random.nextDouble(0.5, 3);
					item.amount = (int) amount;
					item.luxury = random.nextBoolean(0.1);
					double price = calculatePrice(item, node, population);
					item.priceBuy = calculatePriceBuy(price, population);
					item.priceSell = calculatePriceSell(price, population);
					if (item.priceBuy == item.priceSell) {
						item.priceSell += 1;
					}
					population.commodities.add(item);
				}
			}
		}
	}

	private String generateCommodityName (Node node, Commodity commodity, Random random) {
		switch (commodity) {
		case ALCOHOL:
			return random.next("Beer", "Wine", "Liquor");
		case BLANK_WEAPONS:
			return random.next("Knifes", "Swords", "Sabers");
		case COMPUTER:
			break;
		case DRUGS:
			return random.next("Marijuana", "Opium", "Ecstasy", "Cocaine", "Heroin", "Tobacco");
		case ELECTRONICS:
			break;
		case FIRE_WEAPONS:
			return random.next("Pistols", "Rifles", "Machineguns");
		case FISH:
			break;
		case GEM_STONE:
			return random.next("Diamonds", "Rubies", "Sapphires", "Emeralds", "Topaz");
		case GRAIN:
			return random.next("Wheat", "Rye", "Rice");
		case JEWELLERY:
			break;
		case MACHINERY:
			break;
		case MEAT:
			return random.next("Pork", "Beef", "Chicken");
		case MEDICINAL_PLANTS:
			break;
		case PELTS:
			break;
		case POTTERY:
			break;
		case TOOLS:
			break;
		case WOOD:
			return random.next("Oak", "Pine", "Cypress", "Mahogany");
		default:
			// do nothing
		}
		return NameGenerator.firstToUppercase(commodity.toString());
	}

	public static double calculateSupply(Commodity commodity, Node node, Population population) {
		Seed seed = new Seed(node.seed, commodity.ordinal(), SEED_SUPPLY);
		Random random = seed.getRandom();
		return population.getSupply(commodity) * random.nextDouble(0.9, 1.1);
	}
	
	public static double calculateDemand(Commodity commodity, Node node, Population population) {
		Seed seed = new Seed(node.seed, commodity.ordinal(), SEED_DEMAND);
		Random random = seed.getRandom();
		return population.getDemand(commodity) * random.nextDouble(0.9, 1.1);
	}
	
	public static double calculatePrice(CommodityItem commodityItem, Node node, Population population) {
		return calculatePrice(commodityItem.commodity, node, population);// * (commodityItem.luxury ? random.nextDouble(1.1, 10) : 1);
	}

	public static double calculatePrice(Commodity commodity, Node node, Population population) {
		Seed seed = new Seed(node.seed, commodity.ordinal(), SEED_PRICE);
		Random random = seed.getRandom();
		return commodity.basePrice * population.getDemand(commodity) * random.nextDouble(0.9, 1.1);
	}

	public static int calculatePriceBuy(double price, Population population) {
		return (int) (price * 0.9);
	}

	public static int calculatePriceSell(double price, Population population) {
		return (int) (price * 1.1 + 0.5);
	}

	private void storeSolarSystem() {
		StarSystem starSystem = new StarSystem(new Seed(0, 0, 0, 0, 0, 0));
		starSystem.childCount = 1;
		store(starSystem);

		int childIndex = 0;

		Star star = new Star (starSystem, childIndex++);
		star.name = "Sol";
		star.type = Star.Type.MAIN_SEQUENCE;
		star.radius = Units.SUN_RADIUS;
		star.mass = Units.SUN_MASS;
		star.temperature = 5778;
		star.rotation = 25.38 * Units.SECONDS_PER_DAY;
		star.childCount = 10;
		store(star);

		// planets
		childIndex = 0;

		Planet mercury;
		{
			mercury = new Planet (star, childIndex++);
			Random random = mercury.seed.getRandom();
			mercury.name = "Mercury";
			mercury.orbitRadius = 57909175E3;
			mercury.orbitPeriod = 0.2408467 * Units.SECONDS_PER_YEAR;
			mercury.orbitStartAngle = random.nextDouble(2 * Math.PI);
			mercury.type = Planet.Type.STONE;
			mercury.textureName = "mercury.jpg";
			mercury.radius = 2439.64E3;
			mercury.rotation = 58.646225 * Units.SECONDS_PER_DAY;
			mercury.mass = 3.302E23;
			mercury.albedo = 0.068; // bond
			mercury.temperature = surfaceTemperature(mercury);
			mercury.childCount = 0;
			generatePopulationHorriblePlanet(mercury, random);
			store(mercury);
		}
		
		Planet venus;
		{
			venus = new Planet (star, childIndex++);
			Random random = venus.seed.getRandom();
			venus.name = "Venus";
			venus.orbitRadius = 108208930E3;
			venus.orbitPeriod = 0.61519726 * Units.SECONDS_PER_YEAR;
			venus.orbitStartAngle = random.nextDouble(2 * Math.PI);
			venus.type = Planet.Type.STONE;
			venus.textureName = "venus.jpg";
			venus.radius = 6051.59E3;
			venus.rotation = -243.0187 * Units.SECONDS_PER_DAY;
			venus.mass = 4.8690E24;
			venus.atmosphere = asMap(tuple(Molecule.CO2, 0.965), tuple(Molecule.N2, 0.035), tuple(Molecule.SO2, 0.00015));
			venus.atmospherePressure = 9.2E6;
			venus.albedo = 0.90; // bond
			venus.temperature = surfaceTemperature(venus);
			venus.childCount = 0;
			store(venus);
		}
		
		Planet terra;
		{
			terra = new Planet (star, childIndex++);
			Random random = terra.seed.getRandom();
			terra.name = "Terra";
			terra.orbitRadius = 149597890E3;
			terra.orbitPeriod = 1 * Units.SECONDS_PER_YEAR;
			terra.orbitStartAngle = random.nextDouble(2 * Math.PI);
			terra.type = Planet.Type.STONE;
			terra.breathableAtmosphere = true;
			terra.water = 0.75;
			terra.supportsLife = true;
			terra.hasLife = true;
			terra.textureName = "earth.jpg";
			terra.textureNormalName = "earth_normals.jpg";
			terra.radius = 6378.1E3;
			terra.rotation = 1 * Units.SECONDS_PER_DAY;
			terra.mass = 5.9742E24;
			terra.atmosphere = asMap(tuple(Molecule.N2, 0.7808), tuple(Molecule.O2, 0.2095), tuple(Molecule.Ar, 0.00934), tuple(Molecule.CO2, 0.00038));
			terra.atmospherePressure = Units.EARTH_ATMOSPHERE_PRESSURE;
			terra.albedo = 0.306; // bond
			terra.temperature = surfaceTemperature(terra);
			terra.childCount = 2;
			generatePopulationNicePlanet(terra, random);
			store(terra);
		}

		Planet mars;
		{
			mars = new Planet (star, childIndex++);
			Random random = mars.seed.getRandom();
			mars.name = "Mars";
			mars.orbitRadius = 227936640E3;
			mars.orbitPeriod = 1.8808476 * Units.SECONDS_PER_YEAR;
			mars.orbitStartAngle = random.nextDouble(2 * Math.PI);
			mars.type = Planet.Type.STONE;
			mars.textureName = "mars.png";
			mars.textureNormalName = "mars_normals.png";
			mars.radius = 3397.00E3;
			mars.rotation = 1.02595675 * Units.SECONDS_PER_DAY;
			mars.mass = 6.4191E23;
			mars.albedo = 0.25; // bond
			mars.atmosphere = asMap(tuple(Molecule.CO2, 0.9523), tuple(Molecule.N2, 0.0027), tuple(Molecule.Ar, 0.0016), tuple(Molecule.O2, 0.0013));
			mars.atmospherePressure = 0.636E6;
			mars.temperature = surfaceTemperature(mars); // 130K - 280K => 210K
			mars.childCount = 3;
			generatePopulationAcceptablePlanet(mars, random);
			store(mars);
		}
		
//		Planet ceres;
//		{
//			ceres = new Planet (star, childIndex++);
//			ceres.name = "Ceres";
//			ceres.orbitRadius = 413700000E3;
//			ceres.orbitPeriod = 4.599 * Units.SECONDS_PER_YEAR;
//			ceres.type = Planet.Type.STONE;
//			ceres.textureName = "ceres.jpg";
//			ceres.radius = 471E3;
//			ceres.rotation = 0.3781 * Units.SECONDS_PER_DAY;
//			ceres.mass = 9.5E20;
//			ceres.childCount = 0;
//			ceres.albedo = 0.1; //??
//			ceres.temperature = surfaceTemperature(ceres);
//			store(ceres);
//		}
		
		AsteroidBelt asteroidBelt;
		{
			asteroidBelt = new AsteroidBelt(star, childIndex++);
			asteroidBelt.name = "Asteroid Belt";
			asteroidBelt.orbitRadius = 600000000E3;
			asteroidBelt.orbitPeriod = 0;
			asteroidBelt.averageRadius = 50;
			asteroidBelt.rotation = 0;
			asteroidBelt.mass = 0;
			asteroidBelt.width = asteroidBelt.orbitRadius * 0.1;
			asteroidBelt.height = asteroidBelt.orbitRadius * 0.00001;
			asteroidBelt.density = 0.2;
			store(asteroidBelt);
		}
		
		Planet jupiter;
		{
			jupiter = new Planet (star, childIndex++);
			Random random = jupiter.seed.getRandom();
			jupiter.name = "Jupiter";
			jupiter.orbitRadius = 778412010E3;
			jupiter.orbitPeriod = 11.862615 * Units.SECONDS_PER_YEAR;
			jupiter.orbitStartAngle = random.nextDouble(2 * Math.PI);
			jupiter.type = Planet.Type.GAS;
			jupiter.textureName = "jupiter.jpg";
			jupiter.textureNormalName = "jupiter_normals.jpg";
			jupiter.radius = 71492.68E3;
			jupiter.rotation = 0.41354 * Units.SECONDS_PER_DAY;
			jupiter.mass = 1.8987E27;
			jupiter.albedo = 0.343; // bond
			jupiter.temperature = surfaceTemperature(jupiter);
			jupiter.atmosphere = asMap(tuple(Molecule.H2, percent(89.8)), tuple(Molecule.He, percent(10.2)), tuple(Molecule.CH4, percent(0.3)), tuple(Molecule.NH3, percent(0.003)), tuple(Molecule.C2H6, percent(0.0006)));
			jupiter.childCount = 4;
			store(jupiter);
		}
		
		Planet saturn;
		{
			saturn = new Planet (star, childIndex++);
			Random random = saturn.seed.getRandom();
			saturn.name = "Saturn";
			saturn.orbitRadius = 1426725400E3;
			saturn.orbitPeriod = 29.447498 * Units.SECONDS_PER_YEAR;
			saturn.orbitStartAngle = random.nextDouble(2 * Math.PI);
			saturn.type = Planet.Type.GAS;
			saturn.textureName = "saturn.jpg";
			saturn.radius = 60267.14E3;
			saturn.rotation = 0.44401 * Units.SECONDS_PER_DAY;
			saturn.mass = 5.6851E26;
			saturn.albedo = 0.342; // bond
			saturn.temperature = surfaceTemperature(saturn);
			saturn.atmosphere = asMap(tuple(Molecule.H2, 0.932), tuple(Molecule.He, 0.067));
			saturn.childCount = 8;
			store(saturn);
		}
		
		Planet uranus;
		{
			uranus = new Planet (star, childIndex++);
			Random random = uranus.seed.getRandom();
			uranus.name = "Uranus";
			uranus.orbitRadius = 2870972200E3;
			uranus.orbitPeriod = 84.016846 * Units.SECONDS_PER_YEAR;
			uranus.orbitStartAngle = random.nextDouble(2 * Math.PI);
			uranus.type = Planet.Type.GAS;
			uranus.textureName = "uranus.jpg";
			uranus.radius = 25557.25E3;
			uranus.rotation = -0.7183 * Units.SECONDS_PER_DAY;
			uranus.mass = 8.6849E25;
			uranus.albedo = 0.300; // bond
			uranus.temperature = surfaceTemperature(uranus);
			uranus.atmosphere = asMap(tuple(Molecule.H2, 0.83), tuple(Molecule.He, 0.15), tuple(Molecule.CH4, 0.02));
			uranus.childCount = 5;
			store(uranus);
		}
		
		Planet neptune;
		{
			neptune = new Planet (star, childIndex++);
			Random random = neptune.seed.getRandom();
			neptune.name = "Neptune";
			neptune.orbitRadius = 4498252900E3;
			neptune.orbitPeriod = 164.79132 * Units.SECONDS_PER_YEAR;
			neptune.orbitStartAngle = random.nextDouble(2 * Math.PI);
			neptune.type = Planet.Type.GAS;
			neptune.textureName = "neptune.jpg";
			neptune.radius = 24766.36E3;
			neptune.rotation = 0.67125 * Units.SECONDS_PER_DAY;
			neptune.mass = 1.0244E26;
			neptune.albedo = 0.290; // bond
			neptune.temperature = surfaceTemperature(neptune);
			neptune.atmosphere = asMap(tuple(Molecule.H2, 0.80), tuple(Molecule.He, 0.19), tuple(Molecule.CH4, 0.015));
			neptune.childCount = 1;
			store(neptune);
		}
		
		store(generateSpaceStation(star, childIndex++));

		star.childCount = childIndex;

		// moons of terra
		childIndex = 0;

		{
			Planet luna = new Planet (terra, childIndex++);
			Random random = luna.seed.getRandom();
			luna.name = "Luna";
			luna.orbitRadius = 384399E3;
			luna.orbitPeriod = 27.32158 * Units.SECONDS_PER_DAY;
			luna.orbitStartAngle = random.nextDouble(2 * Math.PI);
			luna.childCount = 0;
			luna.type = Planet.Type.STONE;
			luna.textureName = "moon.jpg";
			luna.textureNormalName = "moon_normals.png";
			luna.radius = 1737.1E3;
			luna.rotation = 27.321582 * Units.SECONDS_PER_DAY;
			luna.mass = 7.3477E22;
			luna.albedo = 0.11; // bond
			luna.temperature = surfaceTemperature(luna);
			generatePopulationAcceptablePlanet(luna, random);
			store(luna);
		}
		
		store(generateSpaceStation(terra, childIndex++));
		
		terra.childCount = childIndex;

		// moons of mars
		childIndex = 0;

		{
			Planet phobos = new Planet (mars, childIndex++);
			Random random = neptune.seed.getRandom();
			phobos.name = "Phobos";
			phobos.orbitRadius = 9376E3;
			phobos.orbitPeriod = 0.31891023 * Units.SECONDS_PER_DAY;
			phobos.orbitStartAngle = random.nextDouble(2 * Math.PI);
			phobos.type = Planet.Type.STONE;
			phobos.textureName = "phobos.jpg";
			phobos.radius = 11.2667E3;
			phobos.rotation = phobos.orbitPeriod;
			phobos.mass = 1.0659E16;
			phobos.childCount = 0;
			phobos.albedo = 0.071;
			phobos.temperature = surfaceTemperature(phobos); // ~233K
			generatePopulationAcceptablePlanet(phobos, random);
			store(phobos);
		}
		
		{
			Planet deimos = new Planet (mars, childIndex++);
			Random random = deimos.seed.getRandom();
			deimos.name = "Deimos";
			deimos.orbitRadius = 23463.2E3;
			deimos.orbitPeriod = 1.263 * Units.SECONDS_PER_DAY;
			deimos.orbitStartAngle = random.nextDouble(2 * Math.PI);
			deimos.type = Planet.Type.STONE;
			deimos.textureName = "deimos.jpg";
			deimos.radius = 6.2E3;
			deimos.rotation = deimos.orbitPeriod;
			deimos.mass = 1.4762E15;
			deimos.albedo = 0.068;
			deimos.temperature = surfaceTemperature(deimos); // ~233K
			deimos.childCount = 0;
			generatePopulationAcceptablePlanet(deimos, random);
			store(deimos);
		}
		
		store(generateSpaceStation(mars, childIndex++));

		mars.childCount = childIndex;

		// moons of jupiter
		childIndex = 0;

		{
			Planet io = new Planet (jupiter, childIndex++);
			Random random = io.seed.getRandom();
			io.name = "Io";
			io.orbitRadius = 421600E3;
			io.orbitPeriod = 1.7691378 * Units.SECONDS_PER_DAY;
			io.orbitStartAngle = random.nextDouble(2 * Math.PI);
			io.type = Planet.Type.STONE;
			io.textureName = "io.jpg";
			io.radius = 1815E3;
			io.rotation = io.orbitPeriod;
			io.mass = 8.94E22;
			io.albedo = 0.63;
			io.temperature = surfaceTemperature(io); //90K - 130K => 110K
			io.childCount = 0;
			generatePopulationHorriblePlanet(io, random);
			store(io);
		}
		
		{
			Planet europa = new Planet (jupiter, childIndex++);
			Random random = europa.seed.getRandom();
			europa.name = "Europa";
			europa.orbitRadius = 670900E3;
			europa.orbitPeriod = 3.551181 * Units.SECONDS_PER_DAY;
			europa.orbitStartAngle = random.nextDouble(2 * Math.PI);
			europa.type = Planet.Type.STONE;
			europa.textureName = "europa.jpg";
			europa.radius = 1569E3;
			europa.rotation = europa.orbitPeriod;
			europa.mass = 4.80E22;
			europa.atmosphere = asMap(tuple(Molecule.O2, 0.98)); // percentage unknown
			europa.atmospherePressure = 0.1E-6; // 0.1 microP
			europa.albedo = 0.67;
			europa.temperature = surfaceTemperature(europa); // ~50K - 125K => 102K
			europa.childCount = 0;
			store(europa);
		}
		
		{
			Planet ganymede = new Planet (jupiter, childIndex++);
			Random random = ganymede.seed.getRandom();
			ganymede.name = "Ganymede";
			ganymede.orbitRadius = 1070400E3;
			ganymede.orbitPeriod = 7.154553 * Units.SECONDS_PER_DAY;
			ganymede.orbitStartAngle = random.nextDouble(2 * Math.PI);
			ganymede.type = Planet.Type.STONE;
			ganymede.textureName = "ganymede.jpg";
			ganymede.radius = 2634.19E3;
			ganymede.rotation = ganymede.orbitPeriod;
			ganymede.mass = 1.4819E23;
			ganymede.albedo = 0.43;
			ganymede.temperature = surfaceTemperature(ganymede); // 70K - 152K => 110K
			ganymede.childCount = 0;
			generatePopulationHorriblePlanet(ganymede, random);
			store(ganymede);
		}
		
		{
			Planet callisto = new Planet (jupiter, childIndex++);
			Random random = callisto.seed.getRandom();
			callisto.name = "Callisto";
			callisto.orbitRadius = 1882700E3;
			callisto.orbitPeriod = 16.68902 * Units.SECONDS_PER_DAY;
			callisto.orbitStartAngle = random.nextDouble(2 * Math.PI);
			callisto.type = Planet.Type.STONE;
			callisto.textureName = "callisto.jpg";
			callisto.radius = 2410.3E3;
			callisto.rotation = callisto.orbitPeriod;
			callisto.mass = 1.0758E23;
			//callisto.atmosphereDensity = 0; // 7.5pbar
			callisto.albedo = 0.22;
			callisto.temperature = surfaceTemperature(callisto); // 80K - 165K => 134K
			callisto.childCount = 0;
			generatePopulationHorriblePlanet(callisto, random);
			store(callisto);
		}
		
		store(generateSpaceStation(jupiter, childIndex++));

		jupiter.childCount = childIndex;

		// moons of saturn
		childIndex = 0;

		{
			 
			AsteroidBelt saturnRing = new AsteroidBelt(saturn, childIndex++);
			saturnRing.name = "Saturn Ring";
			saturnRing.orbitRadius = 140000E3; // 66900km - 140180km
			saturnRing.orbitPeriod = 0;
			saturnRing.averageRadius = 5;
			saturnRing.rotation = 0;
			saturnRing.mass = 0;
			saturnRing.width = 80000E3;
			saturnRing.height = 10000E3;
			saturnRing.density = 0.9;
			store(saturnRing);
		}
		
		{
			Planet mimas = new Planet (saturn, childIndex++);
			Random random = mimas.seed.getRandom();
			mimas.name = "Mimas";
			mimas.orbitRadius = 185520E3;
			mimas.orbitPeriod = 0.942422 * Units.SECONDS_PER_DAY;
			mimas.orbitStartAngle = random.nextDouble(2 * Math.PI);
			mimas.type = Planet.Type.STONE;
			mimas.textureName = "mimas.jpg";
			mimas.radius = 198.30E3;
			mimas.rotation = mimas.orbitPeriod;
			mimas.mass = 3.75E19;
			mimas.albedo = 0.962; // geometric
			mimas.temperature = surfaceTemperature(mimas); // ~64K
			mimas.childCount = 0;
			store(mimas);
		}
		
		{
			Planet enceladus = new Planet (saturn, childIndex++);
			Random random = enceladus.seed.getRandom();
			enceladus.name = "Enceladus";
			enceladus.orbitRadius = 237948E3;
			enceladus.orbitPeriod = 1.370218 * Units.SECONDS_PER_DAY;
			enceladus.orbitStartAngle = random.nextDouble(2 * Math.PI);
			enceladus.type = Planet.Type.STONE;
			enceladus.textureName = "enceladus.jpg";
			enceladus.radius = 252.1E3;
			enceladus.rotation = enceladus.orbitPeriod;
			enceladus.mass = 1.08E20;
			enceladus.albedo = 0.99; // bond
			enceladus.temperature = surfaceTemperature(enceladus);
			enceladus.childCount = 0;
			store(enceladus);
		}
		
		{
			Planet tethys = new Planet (saturn, childIndex++);
			Random random = tethys.seed.getRandom();
			tethys.name = "Tethys";
			tethys.orbitRadius = 294619E3;
			tethys.orbitPeriod = 1.887802 * Units.SECONDS_PER_DAY;
			tethys.orbitStartAngle = random.nextDouble(2 * Math.PI);
			tethys.type = Planet.Type.STONE;
			tethys.textureName = "tethys.jpg";
			tethys.radius = 533E3;
			tethys.rotation = tethys.orbitPeriod;
			tethys.mass = 6.174E20;
			tethys.albedo = 0.80; // bond
			tethys.temperature = surfaceTemperature(tethys);
			tethys.childCount = 0;
			store(tethys);
		}
		
		{
			Planet dione = new Planet (saturn, childIndex++);
			Random random = dione.seed.getRandom();
			dione.name = "Dione";
			dione.orbitRadius = 377396E3;
			dione.orbitPeriod = 2.736915 * Units.SECONDS_PER_DAY;
			dione.orbitStartAngle = random.nextDouble(2 * Math.PI);
			dione.type = Planet.Type.STONE;
			dione.textureName = "dione.jpg";
			dione.radius = 561.7E3;
			dione.rotation = dione.orbitPeriod;
			dione.mass = 1.095E21;
			dione.albedo = 0.998;
			dione.temperature = surfaceTemperature(dione);
			dione.childCount = 0;
			store(dione);
		}
		
		{
			Planet rhea = new Planet (saturn, childIndex++);
			Random random = rhea.seed.getRandom();
			rhea.name = "Rhea";
			rhea.orbitRadius = 527108E3;
			rhea.orbitPeriod = 4.518212 * Units.SECONDS_PER_DAY;
			rhea.orbitStartAngle = random.nextDouble(2 * Math.PI);
			rhea.type = Planet.Type.STONE;
			rhea.textureName = "rhea.jpg";
			rhea.radius = 764.3E3;
			rhea.rotation = rhea.orbitPeriod;
			rhea.mass = 2.306E21;
			rhea.albedo = 0.949;
			rhea.temperature = surfaceTemperature(rhea);
			rhea.childCount = 0;
			store(rhea);
		}
		
		{
			Planet titan = new Planet (saturn, childIndex++);
			Random random = titan.seed.getRandom();
			titan.name = "Titan";
			titan.orbitRadius = 1221870E3;
			titan.orbitPeriod = 15.945 * Units.SECONDS_PER_DAY;
			titan.orbitStartAngle = random.nextDouble(2 * Math.PI);
			titan.type = Planet.Type.STONE;
			titan.textureName = "titan.jpg";
			titan.radius = 2576E3;
			titan.rotation = titan.orbitPeriod;
			titan.mass = 1.3452E23;
			titan.atmosphere = asMap(tuple(Molecule.N2, 0.984), tuple(Molecule.CH4, 0.014), tuple(Molecule.H2, 0.002));
			titan.atmospherePressure = 146.7E3; // 146.7 kP
			titan.albedo = 0.22;
			titan.temperature = surfaceTemperature(titan);
			titan.childCount = 0;
			store(titan);
		}
		
		{
			Planet iapetus = new Planet (saturn, childIndex++);
			Random random = iapetus.seed.getRandom();
			iapetus.name = "Iapetus";
			iapetus.orbitRadius = 3560820E3;
			iapetus.orbitPeriod = 79.322 * Units.SECONDS_PER_DAY;
			iapetus.orbitStartAngle = random.nextDouble(2 * Math.PI);
			iapetus.type = Planet.Type.STONE;
			iapetus.textureName = "iapetus.jpg";
			iapetus.radius = 735.60E3;
			iapetus.rotation = iapetus.orbitPeriod;
			iapetus.mass = 1.8053E21;
			iapetus.albedo = 0.25; // 0.05 - 0.5
			iapetus.temperature = surfaceTemperature(iapetus);
			iapetus.childCount = 0;
			store(iapetus);
		}
		
		saturn.childCount = childIndex;

		// moons of uranus
		childIndex = 0;

		{
			Planet miranda = new Planet (uranus, childIndex++);
			Random random = miranda.seed.getRandom();
			miranda.name = "Miranda";
			miranda.orbitRadius = 129390E3;
			miranda.orbitPeriod = 1.4135 * Units.SECONDS_PER_DAY;
			miranda.orbitStartAngle = random.nextDouble(2 * Math.PI);
			miranda.type = Planet.Type.STONE;
			miranda.textureName = "miranda.jpg";
			miranda.radius = 235.8E3;
			miranda.rotation = miranda.orbitPeriod;
			miranda.mass = 6.59E19;
			miranda.albedo = 0.32;
			miranda.temperature = surfaceTemperature(miranda);
			miranda.childCount = 0;
			store(miranda);
		}
		
		{
			Planet ariel = new Planet (uranus, childIndex++);
			Random random = ariel.seed.getRandom();
			ariel.name = "Ariel";
			ariel.orbitRadius = 190900E3;
			ariel.orbitPeriod = 2.520 * Units.SECONDS_PER_DAY;
			ariel.orbitStartAngle = random.nextDouble(2 * Math.PI);
			ariel.type = Planet.Type.STONE;
			ariel.textureName = "ariel.jpg";
			ariel.radius = 578.9E3;
			ariel.rotation = ariel.orbitPeriod;
			ariel.mass = 1.35E21;
			ariel.albedo = 0.53;
			ariel.temperature = surfaceTemperature(ariel); // ?K - 84K => 60K
			ariel.childCount = 0;
			store(ariel);
		}
		
		{
			Planet umbriel = new Planet (uranus, childIndex++);
			Random random = umbriel.seed.getRandom();
			umbriel.name = "Umbriel";
			umbriel.orbitRadius = 266000E3;
			umbriel.orbitPeriod = 4.144 * Units.SECONDS_PER_DAY;
			umbriel.orbitStartAngle = random.nextDouble(2 * Math.PI);
			umbriel.type = Planet.Type.STONE;
			umbriel.textureName = "umbriel.jpg";
			umbriel.radius = 584.7E3;
			umbriel.rotation = umbriel.orbitPeriod;
			umbriel.mass = 1.2E21;
			umbriel.albedo = 0.26;
			umbriel.temperature = surfaceTemperature(umbriel); // ?K - 85K => 75K
			umbriel.childCount = 0;
			store(umbriel);
		}
	
		{
			Planet titania = new Planet (uranus, childIndex++);
			Random random = titania.seed.getRandom();
			titania.name = "Titania";
			titania.orbitRadius = 436300E3;
			titania.orbitPeriod = 8.706 * Units.SECONDS_PER_DAY;
			titania.orbitStartAngle = random.nextDouble(2 * Math.PI);
			titania.type = Planet.Type.STONE;
			titania.textureName = "titania.jpg";
			titania.radius = 788.9E3;
			titania.rotation = titania.orbitPeriod;
			titania.mass = 3.5E21;
			titania.albedo = 0.35;
			titania.temperature = surfaceTemperature(titania); // 60K - 89K => 70K
			titania.childCount = 0;
			store(titania);
		}
		
		{
			Planet oberon = new Planet (uranus, childIndex++);
			Random random = oberon.seed.getRandom();
			oberon.name = "Oberon";
			oberon.orbitRadius = 583519E3;
			oberon.orbitPeriod = 13.46 * Units.SECONDS_PER_DAY;
			oberon.orbitStartAngle = random.nextDouble(2 * Math.PI);
			oberon.type = Planet.Type.STONE;
			oberon.textureName = "oberon.jpg";
			oberon.radius = 761.4E3;
			oberon.rotation = oberon.orbitPeriod;
			oberon.mass = 3.014E21;
			oberon.albedo = 0.31;
			oberon.temperature = surfaceTemperature(oberon); // 70K-80K
			oberon.childCount = 0;
			store(oberon);
		}
		
		uranus.childCount = childIndex;

		// moons of neptune
		childIndex = 0;

		{
			Planet triton = new Planet (neptune, childIndex++);
			Random random = triton.seed.getRandom();
			triton.name = "Triton";
			triton.orbitRadius = 354759E3;
			triton.orbitPeriod = -5.877 * Units.SECONDS_PER_DAY;
			triton.orbitStartAngle = random.nextDouble(2 * Math.PI);
			triton.type = Planet.Type.STONE;
			triton.textureName = "triton.jpg";
			triton.radius = 1353.4E3;
			triton.rotation = - triton.orbitPeriod;
			triton.mass = 2.14E22;
			triton.atmosphere = asMap(tuple(Molecule.N2, 0.96), tuple(Molecule.CH4, 0.02), tuple(Molecule.CO, 0.01)); // exact values unknown
			triton.atmospherePressure = 1.5; // 1.4P-1.7P
			triton.albedo = 0.76;
			triton.temperature = surfaceTemperature(triton);
			triton.childCount = 0;
			store(triton);
		}		
		neptune.childCount = childIndex;
	}

	private <K, V> Map<K, V> asMap(Tuple2<K, V>... tuples) {
		Map<K, V> map = new HashMap<K, V>();
		for (Tuple2<K, V> tuple : tuples) {
			map.put(tuple.getValue1(), tuple.getValue2());
		}
		return map;
	}

	private <K, V> Tuple2<K, V> tuple(K key, V value) {
		return new Tuple2<K, V>(key, value);
	}
	
	private double percent(double x) {
		return x * 0.01;
	}
}
