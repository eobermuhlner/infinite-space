package ch.obermuhlner.infinitespace.model.universe.population;

import ch.obermuhlner.infinitespace.model.generator.TechLevel;

public enum Commodity {
	// agriculture
	WOOD(TechLevel.STONE, 10, Industry.AGRICULTURE, Industry.REFINERY),
	GRAIN(TechLevel.BRONZE, 20, Industry.AGRICULTURE, null),
	MEAT(TechLevel.BRONZE, 50, Industry.AGRICULTURE, null),
	LEATHER(TechLevel.STONE, 40, Industry.AGRICULTURE, null),
	PELTS(TechLevel.STONE, 100, Industry.AGRICULTURE, null),
	ALCOHOL(TechLevel.BRONZE, 40, Industry.AGRICULTURE, null),
	MEDICINAL_PLANTS(TechLevel.STONE, 200, Industry.AGRICULTURE, null),
	// fishing
	WATER(TechLevel.STONE, 5, Industry.FISHING, null),
	SALT(TechLevel.STONE, 6, Industry.FISHING, null),
	FISH(TechLevel.STONE, 40, Industry.FISHING, null),
	ALGAE(TechLevel.STONE, 10, Industry.FISHING, null),
	// mining
	COAL(TechLevel.STONE, 20, Industry.MINING, Industry.REFINERY),
	SAND(TechLevel.STONE, 20, Industry.MINING, Industry.REFINERY),
	IRON_ORE(TechLevel.IRON, 20, Industry.MINING, Industry.REFINERY),
	COPPER_ORE(TechLevel.IRON, 30, Industry.MINING, Industry.REFINERY),
	SILVER_ORE(TechLevel.IRON, 50, Industry.MINING, Industry.REFINERY),
	GOLD_ORE(TechLevel.IRON, 70, Industry.MINING, Industry.REFINERY),
	URANIUM_ORE(TechLevel.STEEL, 80, Industry.MINING, Industry.REFINERY),
	GEM_STONE(TechLevel.IRON, 500, Industry.MINING, Industry.REFINERY),
	// refined
	GLASS(TechLevel.STONE, 20, Industry.MINING, Industry.REFINERY),
	IRON(TechLevel.STEEL, 40, Industry.REFINERY, Industry.INDUSTRY),
	STEEL(TechLevel.MACHINES, 60, Industry.REFINERY, Industry.INDUSTRY),
	COPPER(TechLevel.IRON, 60, Industry.REFINERY, Industry.INDUSTRY),
	SILVER(TechLevel.STEEL, 100, Industry.REFINERY, Industry.INDUSTRY),
	GOLD(TechLevel.STEEL, 140, Industry.REFINERY, Industry.INDUSTRY),
	URANIUM(TechLevel.MACHINES, 160, Industry.REFINERY, Industry.INDUSTRY),
	// industry
	POTTERY(TechLevel.STONE, 50, Industry.INDUSTRY, null),
	TOOLS(TechLevel.STEEL, 100, Industry.INDUSTRY, null),
	MACHINERY(TechLevel.MACHINES, 200, Industry.INDUSTRY, null),
	BLANK_WEAPONS(TechLevel.IRON, 100, Industry.INDUSTRY, null),
	FIRE_WEAPONS(TechLevel.STEEL, 100, Industry.INDUSTRY, null),
	MEDICINE(TechLevel.IRON, 150, Industry.INDUSTRY, null),
	JEWELLERY(TechLevel.IRON, 1000, Industry.INDUSTRY, null),
	ELECTRONICS(TechLevel.ELECTRONICS, 300, Industry.INDUSTRY, null),
	COMPUTER(TechLevel.ELECTRONICS, 700, Industry.INDUSTRY, null),
	NANOTECH(TechLevel.NANO, 1200, Industry.INDUSTRY, null),
	BIOTECH(TechLevel.NANO, 900, Industry.INDUSTRY, null),
	// illegal
	DRUGS(TechLevel.STONE, 500, Industry.AGRICULTURE, Industry.PIRACY),
	SLAVES(TechLevel.STONE, 200, Industry.PIRACY, Industry.PIRACY);
	
	public TechLevel techLevel;
	public float basePrice;
	public Industry supply;
	public Industry demand;

	private Commodity (TechLevel techLevel, float basePrice, Industry supply, Industry demand) {
		this.techLevel = techLevel;
		this.basePrice = basePrice;
		this.supply = supply;
		this.demand = demand;
	}

	
}
