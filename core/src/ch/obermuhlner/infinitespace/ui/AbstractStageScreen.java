package ch.obermuhlner.infinitespace.ui;

import ch.obermuhlner.infinitespace.Config;
import ch.obermuhlner.infinitespace.Effects;
import ch.obermuhlner.infinitespace.GamePreferences;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.RenderState;
import ch.obermuhlner.infinitespace.render.UberShaderProvider;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public abstract class AbstractStageScreen extends AbstractInfiniteSpaceGameScreen {

	protected static final String TITLE = "title";
	protected static final String HEADER = "header";

	private final RenderState renderState = new RenderState();

	protected Camera camera;

	private ModelBatch modelBatch;

	protected Stage stage;

	protected CameraInputController cameraInputController;

	public AbstractStageScreen (InfiniteSpaceGame game) {
		super(game);
	}

	@Override
	public void show () {
		super.show();
		
		modelBatch = new ModelBatch(UberShaderProvider.DEFAULT);
		
		ScreenViewport viewport = new ScreenViewport();
		viewport.setUnitsPerPixel(Config.getUnitsPerPixel());
		stage = new Stage(viewport);
		
		createCamera();

		cameraInputController = new CameraInputController(camera);
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, cameraInputController));

		prepareRenderState(renderState);
		prepareStageRoot(stage);
		
		stage.addAction(Effects.fadeIn());
	}
	
	protected void prepareRenderState (RenderState renderState) {
		// does nothing
	}

	@Override
	public void hide () {
		super.hide();
		stage.dispose();
	}

	@Override
	public void resize (int width, int height) {
		super.resize(width, height);
	   stage.getViewport().update(width, height, true);
	}
	
	@Override
	public void render (float delta) {
		super.render(delta);
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		cameraInputController.update();
		stage.act(delta);

		modelBatch.begin(camera);
		modelBatch.render(renderState.instances, renderState.environment);
		modelBatch.end();

		stage.draw();
	}

	private void createCamera () {
		camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(10, 10, 10);
		camera.lookAt(0, 0, 0);
		camera.near = 0.001f;
		camera.far = 300f;
		camera.update(true);
	}

	protected void prepareStageRoot (Stage stage) {
		stage.clear();
		
		prepareStage(stage);
	}

	protected void prepareStage (Stage stage) {
		Table rootTable = rootTable();
		
		prepareStage(stage, rootTable);
		
		stage.addActor(rootTable);
	}

	protected void prepareStage (Stage stage, Table rootTable) {
	}

	protected Table rootTable() {
		Table table = new Table(skin);
		
		table.top().left();
		table.defaults().left().space(10);

		table.setFillParent(true);

		//table.setDebug(true);

		return table;
	}
	
	protected Table table() {
		Table table = new Table(skin);
		table.setBackground("table");
		
		table.left();
		table.defaults().align(Align.left).spaceRight(50);

		//table.setDebug(true);
		
		return table;
	}
	
	protected TextButton button(String text, ChangeListener changeListener) {
		TextButton button = new TextButton(text, skin);
		button.addListener(changeListener);
		return button;
	}
	
	protected TextButton button(String text, final Screen screen) {
		return button(text, new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				setScreen(screen);
			}
		});
	}
	
	protected void setScreen(final Screen screen) {
		stage.addAction(Effects.fadeOut(game, screen));
	}

	protected  CheckBox bindBoolean (final CheckBox checkbox, final String key) {
		checkbox.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				GamePreferences.INSTANCE.preferences.putBoolean(key, checkbox.isChecked());
			}
		});
		checkbox.setChecked(GamePreferences.INSTANCE.preferences.getBoolean(key));
		return checkbox;
	}

	protected  Slider bindFloat (final Slider slider, final String key) {
		slider.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				GamePreferences.INSTANCE.preferences.putFloat(key, slider.getValue());
			}
		});
		slider.setValue(GamePreferences.INSTANCE.preferences.getFloat(key));
		return slider;
	}

	protected  SelectBox<String> bindString(final SelectBox<String> selectBox, final String key, final String... items) {
		return bindString(selectBox, key, null, items);
	}
	
	protected  SelectBox<String> bindString(final SelectBox<String> selectBox, final String key, final ChangeListener changeListener, final String... items) {
		selectBox.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				GamePreferences.INSTANCE.preferences.putString(key, selectBox.getSelected());
				if (changeListener != null) {
					changeListener.changed(event, actor);
				}
			}
		});
		selectBox.setItems(items);
		selectBox.setSelected(GamePreferences.INSTANCE.preferences.getString(key, items[0]));
		return selectBox;
	}

	protected SelectBox<Integer> bindInt(final SelectBox<Integer> selectBox, final String key, final Integer... items) {
		return bindInt(selectBox, key, null, items);
	}

	protected SelectBox<Integer> bindInt(final SelectBox<Integer> selectBox, final String key, final ChangeListener changeListener, final Integer... items) {
		selectBox.setItems(items);
		selectBox.setSelected(GamePreferences.INSTANCE.preferences.getInteger(key, items[0]));
		
		selectBox.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				GamePreferences.INSTANCE.preferences.putString(key, selectBox.getSelected().toString());
				if (changeListener != null) {
					changeListener.changed(event, actor);
				}
			}
		});

		return selectBox;
	}

	protected SelectBox<Enum<?>> bindEnum(final SelectBox<Enum<?>> selectBox, final String key, final Enum<?>... items) {
		return bindEnum(selectBox, key, null, items);
	}

	protected SelectBox<Enum<?>> bindEnum(final SelectBox<Enum<?>> selectBox, final String key, final ChangeListener changeListener, final Enum<?>... items) {
		selectBox.setItems(items);
		String currentString = GamePreferences.INSTANCE.preferences.getString(key, items[0].toString());
		Enum<?> currentEnum = getEnum(currentString, items);
		selectBox.setSelected(currentEnum);

		selectBox.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				GamePreferences.INSTANCE.preferences.putString(key, selectBox.getSelected().toString());
				if (changeListener != null) {
					changeListener.changed(event, actor);
				}
			}
		});

		return selectBox;
	}

	private Enum<?> getEnum (String string, Enum<?>[] items) {
		for (int i = 0; i < items.length; i++) {
			if (string.equals(items[i].toString())) {
				return items[i];
			}
		}
		return null;
	}

}
