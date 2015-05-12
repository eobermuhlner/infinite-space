package ch.obermuhlner.infinitespace;

import ch.obermuhlner.infinitespace.model.universe.population.Commodity;

public class CommodityItem {
	public CommodityItem () {
	}
	
	public CommodityItem (CommodityItem item) {
		name = item.name;
		commodity = item.commodity;
		amount = item.amount;
		priceSell = item.priceSell;
		priceBuy = item.priceBuy;
		luxury = item.luxury;
	}
	
	public String name;
	public Commodity commodity;
	public int amount;
	public int priceSell;
	public int priceBuy;
	public boolean luxury;
	
	@Override
	public String toString () {
		return "CommodityItem [name=" + name + ", commodity=" + commodity + ", amount=" + amount + ", priceSell=" + priceSell
			+ ", priceBuy=" + priceBuy + ", luxury=" + luxury + "]";
	}
}
