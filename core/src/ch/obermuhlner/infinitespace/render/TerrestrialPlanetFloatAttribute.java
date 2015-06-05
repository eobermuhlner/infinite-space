package ch.obermuhlner.infinitespace.render;

import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;

public class TerrestrialPlanetFloatAttribute extends FloatAttribute {

	public static final String HeightMinAlias = "heightMin";
	public static final long HeightMin = register(HeightMinAlias);

	public static final String HeightMaxAlias = "heightMax";
	public static final long HeightMax = register(HeightMaxAlias);

	public static final String HeightFrequencyAlias = "heightFrequency";
	public static final long HeightFrequency = register(HeightFrequencyAlias);

	public static final String HeightWaterAlias = "heightWater";
	public static final long HeightWater = register(HeightWaterAlias);

	public static final String IcelLevelAlias = "icelLevel";
	public static final long IcelLevel = register(IcelLevelAlias);

	public static TerrestrialPlanetFloatAttribute createHeightMin (float value) {
		return new TerrestrialPlanetFloatAttribute(HeightMin, value);
	}

	public static TerrestrialPlanetFloatAttribute createHeightMax (float value) {
		return new TerrestrialPlanetFloatAttribute(HeightMax, value);
	}

	public static TerrestrialPlanetFloatAttribute createHeightFrequency (float value) {
		return new TerrestrialPlanetFloatAttribute(HeightFrequency, value);
	}

	public static TerrestrialPlanetFloatAttribute createHeightWater (float value) {
		return new TerrestrialPlanetFloatAttribute(HeightWater, value);
	}

	public static TerrestrialPlanetFloatAttribute createIcelLevel (float value) {
		return new TerrestrialPlanetFloatAttribute(IcelLevel, value);
	}

	private TerrestrialPlanetFloatAttribute(long type, float value) {
		super(type, value);
	}

}
