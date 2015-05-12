package ch.obermuhlner.infinitespace;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GamePreferences {

	public static GamePreferences INSTANCE = new GamePreferences();
	
	public static final String BOOL_ROLL_INVERT = "rollInvert";

	public static final String BOOL_PITCH_INVERT = "pitchInvert";

	public static final String FLOAT_MUSIC_VOLUME = "musicVolume";
	public static final String FLOAT_EFFECT_VOLUME = "fxVolume";

	public static final String STRING_GRAPHICS_QUALITY = "graphicsQuality";

	public static final String BOOL_PROCEDURAL_TEXTURES = "proceduralTextures";
	public static final String INT_GENERATED_TEXTURES_SIZE = "generatedTexturesSize";
	
	public enum GraphicsQuality {
		HIGH,
		MEDIUM,
		LOW,
		CUSTOM
	}

	public Preferences preferences;
	
	private GamePreferences() {
		preferences = Gdx.app.getPreferences(GamePreferences.class.getName());
	}
}
