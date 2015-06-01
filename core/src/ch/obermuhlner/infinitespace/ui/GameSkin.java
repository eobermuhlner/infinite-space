package ch.obermuhlner.infinitespace.ui;

import ch.obermuhlner.infinitespace.Config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class GameSkin {

	static Skin skin;
	
	public static Skin getSkin() {
		if (skin == null) {
			
			skin = new Skin();
			skin.addRegions(new TextureAtlas(Gdx.files.internal("data/uiskin.atlas")));
			skin.add("default-font", generateFont("data/fonts/orbitron-medium.ttf", 20));
			skin.add("large-font", generateFont("data/fonts/orbitron-medium.ttf", 26));
			skin.load(Gdx.files.internal("data/uiskin.json"));			
		}
		return skin;
	}

	private static BitmapFont generateFont(String ttfFile, int size) {
		FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal(ttfFile));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = Config.getFontSize(size);
		BitmapFont font = gen.generateFont(parameter);
		gen.dispose();
		return font;
	}
}
