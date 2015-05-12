package ch.obermuhlner.infinitespace;

import java.util.HashMap;
import java.util.Map;

import ch.obermuhlner.infinitespace.GamePreferences.GraphicsQuality;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

public class OptionsScreen extends AbstractStageScreen {

	private static Map<Enum<?>, Map<String, ?>> GRAPHICS_QUALITY_SETTINGS;
	static {
		Map<String, Object> highQuality = new HashMap<String, Object>();
		highQuality.put(GamePreferences.BOOL_PROCEDURAL_TEXTURES, Boolean.TRUE);
		highQuality.put(GamePreferences.INT_GENERATED_TEXTURES_SIZE, 2048);
		
		Map<String, Object> mediumQuality = new HashMap<String, Object>();
		mediumQuality.put(GamePreferences.BOOL_PROCEDURAL_TEXTURES, Boolean.FALSE);
		mediumQuality.put(GamePreferences.INT_GENERATED_TEXTURES_SIZE, 1024);
		
		Map<String, Object> lowQuality = new HashMap<String, Object>();
		lowQuality.put(GamePreferences.BOOL_PROCEDURAL_TEXTURES, Boolean.FALSE);
		lowQuality.put(GamePreferences.INT_GENERATED_TEXTURES_SIZE, 512);
		
		GRAPHICS_QUALITY_SETTINGS = new HashMap<Enum<?>, Map<String, ?>>();
		GRAPHICS_QUALITY_SETTINGS.put(GamePreferences.GraphicsQuality.HIGH, highQuality);
		GRAPHICS_QUALITY_SETTINGS.put(GamePreferences.GraphicsQuality.MEDIUM, mediumQuality);
		GRAPHICS_QUALITY_SETTINGS.put(GamePreferences.GraphicsQuality.LOW, lowQuality);
	}
	
	public OptionsScreen (InfiniteSpaceGame game) {
		super(game);
	}
	
	@Override
	protected void prepareStage (final Stage stage) {
		Table table = table();
		
		table.row();
		table.add(new Label("Controls", skin, TITLE)).colspan(2);
		
		table.row();
		table.add(new Label("Invert Roll", skin)).align(Align.left);
		table.add(bindBoolean(new CheckBox(null, skin), GamePreferences.BOOL_ROLL_INVERT));
		
		table.row();
		table.add(new Label("Invert Pitch", skin));
		table.add(bindBoolean(new CheckBox(null, skin), GamePreferences.BOOL_PITCH_INVERT));
		
		table.row();
		table.add(new Label("Audio", skin, TITLE)).colspan(2);
		
		table.row();
		table.add(new Label("Music Volume", skin));
		table.add(bindFloat(new Slider(0.0f, 1.0f, 0.01f, false, skin), GamePreferences.FLOAT_MUSIC_VOLUME));
		
		table.row();
		table.add(new Label("Effect Volume", skin));
		table.add(bindFloat(new Slider(0.0f, 1.0f, 0.01f, false, skin), GamePreferences.FLOAT_EFFECT_VOLUME));
		
		table.row();
		table.add(new Label("Graphics", skin, TITLE)).colspan(2);

		table.row();
		table.add(new Label("Quality", skin));
		SelectBox<Enum<?>> selectGraphicsQuality = new SelectBox<Enum<?>>(skin);
		table.add(bindEnum(selectGraphicsQuality, GamePreferences.STRING_GRAPHICS_QUALITY, new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				prepareStageRoot(stage);
				
			}
		}, GraphicsQuality.HIGH, GraphicsQuality.MEDIUM, GraphicsQuality.LOW, GraphicsQuality.CUSTOM));
		
		CheckBox checkboxProcedureTextures = new CheckBox(null, skin);
		SelectBox<Integer> selectboxGeneratedTexturesSize = new SelectBox<Integer>(skin);
		GraphicsQuality graphicsQuality = GraphicsQuality.valueOf(GamePreferences.INSTANCE.preferences.getString(GamePreferences.STRING_GRAPHICS_QUALITY, GraphicsQuality.HIGH.toString()));
		if (graphicsQuality != GraphicsQuality.CUSTOM) {
			GamePreferences.INSTANCE.preferences.put(GRAPHICS_QUALITY_SETTINGS.get(graphicsQuality));
			checkboxProcedureTextures.setDisabled(true);
			selectboxGeneratedTexturesSize.setDisabled(true);
		}
		if (graphicsQuality == GraphicsQuality.CUSTOM || true) {
			String labelStyle = graphicsQuality == GraphicsQuality.CUSTOM ? "default" : "disabled";
			
			table.row();
			table.add(new Label("Procedural Textures", skin, labelStyle));
			table.add(bindBoolean(checkboxProcedureTextures, GamePreferences.BOOL_PROCEDURAL_TEXTURES));
			
			table.row();
			table.add(new Label("Generated Textures", skin, labelStyle));
			table.add(bindInt(selectboxGeneratedTexturesSize, GamePreferences.INT_GENERATED_TEXTURES_SIZE, 2048, 1024, 512, 256));
		}

		// rootTable
		Table rootTable = rootTable();
		ScrollPane scrollPane = new ScrollPane(table, skin);
		rootTable.add(scrollPane).colspan(3);
		
		rootTable.row().padTop(20);
		rootTable.add(button(I18N.OK, new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				GamePreferences.INSTANCE.preferences.flush();
				showWelcomeScreen();
			}
		}));
		rootTable.add(button(I18N.CANCEL, new WelcomeScreen(infiniteSpaceGame)));
		rootTable.add(button(I18N.RESET, new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				GamePreferences.INSTANCE.preferences.clear();
				GamePreferences.INSTANCE.preferences.flush();
				prepareStageRoot(stage);
			}
		}));

		stage.addActor(rootTable);
	}
}
