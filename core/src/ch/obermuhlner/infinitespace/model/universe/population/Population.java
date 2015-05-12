package ch.obermuhlner.infinitespace.model.universe.population;

import java.util.List;
import java.util.Map;

import ch.obermuhlner.infinitespace.CommodityItem;
import ch.obermuhlner.infinitespace.model.generator.TechLevel;
import ch.obermuhlner.infinitespace.util.MathUtil;


public class Population {
	
	public double population;
	
	public TechLevel techLevel;
	
	public Map<Industry, Double> industry;
	
	public List<CommodityItem> commodities;
	
	public boolean hasCommodity (Commodity commodity) {
		return commodity.techLevel.compareTo(techLevel) <= 0;
	}

	public double getSupply(Commodity commodity) {
		if (!hasCommodity(commodity)) {
			return 0;
		}
		
		Double production = industry.get(commodity.supply);
		if (production == null) {
			production = 0.0;
		}
		Double trade = industry.get(Industry.TRADE);
		if (trade != null) {
			production += trade / Industry.values().length;
		}
		return production;
	}
	
	public double getDemand(Commodity commodity) {
		double result = 1.0;
		
		double supply = MathUtil.clamp(getSupply(commodity), 0, 1);
		result *= 1.0 - supply;
		
		Double demand = industry.get(commodity.demand);
		if (demand != null) {
			result *= 1.0 + demand;
		}
		
		return result;
	}
	
	@Override
	public String toString () {
		return "Population{" + population + "," + industry + "}";
	}

}
