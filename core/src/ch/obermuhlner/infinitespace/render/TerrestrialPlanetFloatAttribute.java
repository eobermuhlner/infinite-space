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

	public static final String IceLevelAlias = "iceLevel";
	public static final long IceLevel = register(IceLevelAlias);

	public static final String ColorNoiseAlias = "colorNoise";
	public static final long ColorNoise = register(ColorNoiseAlias);

	public static final String ColorFrequencyAlias = "colorFrequency";
	public static final long ColorFrequency = register(ColorFrequencyAlias);

	public static final String HeightMountainsAlias = "heightMountains";
	public static final long HeightMountains = register(HeightMountainsAlias);

	public static final String CreateSpecularAlias = "createSpecular";
	public static final long CreateSpecular = register(CreateSpecularAlias);

	public static final String CreateNormalAlias = "createNormal";
	public static final long CreateNormal = register(CreateNormalAlias);

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

	public static TerrestrialPlanetFloatAttribute createIceLevel (float value) {
		return new TerrestrialPlanetFloatAttribute(IceLevel, value);
	}

	public static TerrestrialPlanetFloatAttribute createColorNoise (float value) {
		return new TerrestrialPlanetFloatAttribute(ColorNoise, value);
	}

	public static TerrestrialPlanetFloatAttribute createColorFrequency (float value) {
		return new TerrestrialPlanetFloatAttribute(ColorFrequency, value);
	}

	public static TerrestrialPlanetFloatAttribute createHeightMountains (float value) {
		return new TerrestrialPlanetFloatAttribute(HeightMountains, value);
	}

	public static TerrestrialPlanetFloatAttribute createCreateSpecular () {
		return new TerrestrialPlanetFloatAttribute(CreateSpecular, 1f);
	}

	public static TerrestrialPlanetFloatAttribute createCreateNormal () {
		return new TerrestrialPlanetFloatAttribute(CreateNormal, 1f);
	}

	private TerrestrialPlanetFloatAttribute(long type, float value) {
		super(type, value);
	}

}
