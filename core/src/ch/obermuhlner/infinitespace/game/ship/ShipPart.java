package ch.obermuhlner.infinitespace.game.ship;

import com.badlogic.gdx.utils.Array;

public class ShipPart {
	public String name;
	public Array<String> types;
	public int minCount;
	public int maxCount;
	public float maxComponentVolume;
	public float maxTotalVolume;
	public Array<ShipComponent> components;

	protected ShipPart() {
	}
	
	public ShipPart (String name, Array<String> types, int minCount, int maxCount, float maxComponentVolume) {
		this(name, types, minCount, maxCount, maxComponentVolume, new Array<ShipComponent>());
	}
	
	public ShipPart (String name, Array<String> types, int minCount, int maxCount, float maxComponentVolume, ShipComponent component) {
		this(name, types, minCount, maxCount, maxComponentVolume, Array.with(component));
	}
	
	public ShipPart (String name, Array<String> types, int minCount, int maxCount, float maxComponentVolume, Array<ShipComponent> components) {
		this.name = name;
		this.types = types;
		this.minCount = minCount;
		this.maxCount = maxCount;
		this.maxComponentVolume = maxComponentVolume;
		this.maxTotalVolume = maxComponentVolume * maxCount;
		this.components = components;
	}
}