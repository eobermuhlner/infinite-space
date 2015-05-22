package ch.obermuhlner.infinitespace.ui.game;

import ch.obermuhlner.infinitespace.GamePreferences;
import ch.obermuhlner.infinitespace.GameState;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.RenderState;
import ch.obermuhlner.infinitespace.ShipUserInterface;
import ch.obermuhlner.infinitespace.UniverseCoordinates;
import ch.obermuhlner.infinitespace.game.Player;
import ch.obermuhlner.infinitespace.game.ship.ShipFactory;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.render.UberShaderProvider;
import ch.obermuhlner.infinitespace.ui.AbstractInfiniteSpaceGameScreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;

public class GameScreen extends AbstractInfiniteSpaceGameScreen {

	private static final boolean USE_FRUSTUM_CULLING = false;

	private final RenderState renderState = new RenderState();

	private ModelBatch modelBatch;

	private Player player;
	private PerspectiveCamera camera;

	private final StringBuilder stringBuilder = new StringBuilder();
	private ShipUserInterface shipUserInterface;

	private final UniverseCoordinates coordinates = new UniverseCoordinates();
	private int starSystemIndex = Integer.MAX_VALUE;

	private boolean paused;

	private Music music;

	public GameScreen (InfiniteSpaceGame game) {
		super(game);
	}

	@Override
	public void show () {
		super.show();
		
		GameState.INSTANCE.load();
		
		music = createMusic();
		modelBatch = new ModelBatch(UberShaderProvider.DEFAULT);

		camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(GameState.INSTANCE.position);
		camera.direction.set(GameState.INSTANCE.direction);
		camera.up.set(GameState.INSTANCE.up);
		camera.near = 0.001f;
		camera.far = 400f;
		camera.update(true);

		player = new Player(ShipFactory.getStandardShip(), camera);
		shipUserInterface = new ShipUserInterface(infiniteSpaceGame, skin, this, player, camera);
		shipUserInterface.starSystemIndex = GameState.INSTANCE.starSystem;
		
		renderState.environment.set(
				new ColorAttribute(ColorAttribute.AmbientLight, 0.1f, 0.1f, 0.1f, 1f));
		
		updateUniverse();
		shipUserInterface.setZoomObject(renderState.instances);
	}
	
	@Override
	public void hide () {
		super.hide();
		
		GameState.INSTANCE.position.set(camera.position);
		GameState.INSTANCE.direction.set(camera.direction);
		GameState.INSTANCE.up.set(camera.up);
		GameState.INSTANCE.starSystem = starSystemIndex;
		
		GameState.INSTANCE.save();
	}
	
	private Music createMusic () {
		float volume = GamePreferences.INSTANCE.preferences.getFloat(GamePreferences.FLOAT_MUSIC_VOLUME);
		if (volume == 0) {
			return null;
		}
		
		Music music = Gdx.audio.newMusic(Gdx.files.internal("data/audio/Urban-Future.mp3"));
		music.setLooping(true);
		music.setVolume(volume);
		music.play();
		
		return music;
	}

	@Override
	public void render (float delta) {
		if (!paused) {
			updateUniverse();
		}
		
		shipUserInterface.update();
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(camera);

		int visibleCount = 0;
		int alwaysVisibleCount = 0;

		{
			camera.near = 100f;
			camera.far = 900f;
			camera.update(true);
	
			for (int i = 0; i < renderState.instancesFar.size; i++) {
				renderState.instancesFar.get(i).transform.setToTranslation(camera.position);
			}
			modelBatch.render(renderState.instancesFar);
			modelBatch.flush();
		}
		
		{
			camera.near = 0.01f;
			camera.far = 1000f;
			camera.update(true);
	
			// always render these instances
			modelBatch.render(renderState.instancesAlways, renderState.environment);
			alwaysVisibleCount += renderState.instancesAlways.size;
	
			modelBatch.flush();
		}

		{
			camera.near = 0.001f;
			camera.far = 300f;
			camera.update(true);
	
			// render these instances only if visible
			if (USE_FRUSTUM_CULLING) {
				for (final ModelInstance instance : renderState.instances) {
					if (isVisible(camera, instance)) {
						modelBatch.render(instance, renderState.environment);
						visibleCount++;
					}
				}
			} else {
				visibleCount += renderState.instances.size;
				modelBatch.render(renderState.instances, renderState.environment);
			}
			modelBatch.flush();
		}
		
		modelBatch.end();

		shipUserInterface.setDebugInfo(getDebugInfo(visibleCount, alwaysVisibleCount));
		shipUserInterface.render();
	}
	
	private boolean updateUniverse () {
		boolean changed = false;
		
		int starSystemIndexNew = shipUserInterface.starSystemIndex;
		
		if (starSystemIndexNew != starSystemIndex) {
			starSystemIndex = starSystemIndexNew;
			disposeInstances();
			renderState.environment.clear();

			infiniteSpaceGame.universeModel.setStarSystemIndex(starSystemIndex);
			Iterable<Node> universe = infiniteSpaceGame.universeModel.getUniverse();
			infiniteSpaceGame.genericNodeConverter.convertNodes(universe, renderState);
			shipUserInterface.setUniverse(universe);
			shipUserInterface.setZoomObject(renderState.instances);
			changed = true;
		}
		
		coordinates.starSystem = camera.position;
		infiniteSpaceGame.universeModel.setPosition(coordinates);
		
		return changed;
	}

	private void disposeInstances () {
		renderState.instances.clear();
		renderState.instancesAlways.clear();
		renderState.instancesFar.clear();
	}

	private final Vector3 positionForIsVisible = new Vector3();

	private boolean isVisible (final Camera cam, final ModelInstance instance) {
		instance.transform.getTranslation(positionForIsVisible);
		return cam.frustum.pointInFrustum(positionForIsVisible);
	}

	private String getDebugInfo (int visibleCount, int alwaysVisibleCount) {
		stringBuilder.setLength(0);
		stringBuilder.append("FPS=");
		stringBuilder.append(Gdx.graphics.getFramesPerSecond());
		stringBuilder.append(" visible=");
		stringBuilder.append(visibleCount);
		stringBuilder.append(" always visible=");
		stringBuilder.append(alwaysVisibleCount);

		return stringBuilder.toString();
	}

	@Override
	public void pause () {
		super.pause();
		
		paused = true;
	}

	@Override
	public void resume () {
		super.resume();
		
		paused = false;
	}

	@Override
	public void resize (int width, int height) {
		super.resize(width, height);
		
		shipUserInterface.resize(width, height);
	}
	
	@Override
	public void dispose () {
		super.dispose();
		
		shipUserInterface.dispose();
		if(music != null) {
			music.dispose();
		}
	}
}
