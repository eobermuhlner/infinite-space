package ch.obermuhlner.infinitespace;

import java.util.Locale;

import ch.obermuhlner.infinitespace.model.universe.SpaceStation;

import com.badlogic.gdx.Gdx;

public class Config {

	public static final boolean DEBUG_PROFILING = false;
	public static final boolean DEBUG_LINEAR_ORBITS = false;
	public static final boolean DEBUG_ORBIT_LINEUP = false;

	public static final boolean DEBUG_SHOW_NODE_BOUNDING_BOX = false;
	public static final boolean DEBUG_SHOW_LINES = false;

	public static final boolean DEBUG_TEST_GENERATOR = true;

	public static final SpaceStation.Type DEBUG_FORCE_SPACE_STATION_TYPE = null;
	//public static final SpaceStation.Type DEBUG_FORCE_SPACE_STATION_TYPE = SpaceStation.Type.RING;

	public static final Locale LOCALE = Locale.US;
	
	public static boolean useScreenControls = true;

	public static boolean useTouchpadControls = true;

	public static boolean useKeyControls = true;
	
	public static float screenDensityFactor = 1.0f;

	public static int getFontSize(int theFontSize) {
		float density = Gdx.graphics.getDensity();
		return Math.round(theFontSize * density * screenDensityFactor * 0.8f);
	}
	
	/**
	 * To be used with {@link com.badlogic.gdx.utils.viewport.ScreenViewport#setUnitsPerPixel(float)}.
	 * 
	 * @return the units per pixel
	 */
	public static float getUnitsPerPixel() {
//		float density = Gdx.graphics.getDensity();
//		return 1.0f / density / screenDensityFactor;
		return 1.0f;
	}

}
