package ch.obermuhlner.infinitespace.game.ship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShipPart<T extends ShipComponent> {
	public String type;
	public int minCount;
	public int maxCount;
	public float maxVolume;
	public List<T> components = new ArrayList<T>();

	protected ShipPart() {
	}
	
	public ShipPart (String type, int minCount, int maxCount, float maxSize) {
		this(type, minCount, maxCount, maxSize, Collections.<T> emptyList());
	}
	
	@SuppressWarnings("unchecked")
	public ShipPart (String type, int minCount, int maxCount, float maxSize, T component) {
		this(type, minCount, maxCount, maxSize, Arrays.asList(component));
	}
	
	public ShipPart (String type, int minCount, int maxCount, float maxSize, List<T> components) {
		this.type = type;
		this.minCount = minCount;
		this.maxCount = maxCount;
		this.maxVolume = maxSize;
		this.components.addAll(components);
	}
}