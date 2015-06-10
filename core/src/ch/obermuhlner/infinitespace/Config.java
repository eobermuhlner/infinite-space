package ch.obermuhlner.infinitespace;

import com.badlogic.gdx.Gdx;

public class Config {

	public static final boolean DEBUG_PROFILING = true;
	public static final boolean DEBUG_LINEAR_ORBITS = true;
	public static final boolean DEBUG_RENDER_CLOUDS = true;
	public static final boolean DEBUG_ORBIT_LINEUP = false;

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
