package ch.obermuhlner.infinitespace.desktop;

import ch.obermuhlner.infinitespace.Config;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Config.useTouchpadControls = false;
		Config.useKeyControls = true;
		Config.screenDensityFactor = 1.4f;
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1200;
		config.height = 1000;
		config.samples = 4;
		new LwjglApplication(new InfiniteSpaceGame(), config);
	}
}
