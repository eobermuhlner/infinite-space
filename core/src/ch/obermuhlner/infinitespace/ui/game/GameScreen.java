package ch.obermuhlner.infinitespace.ui.game;

import java.util.Map;

import ch.obermuhlner.infinitespace.GamePreferences;
import ch.obermuhlner.infinitespace.GameState;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.RenderState;
import ch.obermuhlner.infinitespace.ShipUserInterface;
import ch.obermuhlner.infinitespace.UniverseCoordinates;
import ch.obermuhlner.infinitespace.game.Player;
import ch.obermuhlner.infinitespace.game.ship.ShipFactory;
import ch.obermuhlner.infinitespace.graphics.CenterPerspectiveCamera;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.render.UberShaderProvider;
import ch.obermuhlner.infinitespace.ui.AbstractInfiniteSpaceGameScreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class GameScreen extends AbstractInfiniteSpaceGameScreen {

	private static final boolean USE_FRUSTUM_CULLING = false;

	private final RenderState renderState = new RenderState();

	private ModelBatch modelBatch;

	private Player player;
	private CenterPerspectiveCamera camera;

	private ShipUserInterface shipUserInterface;

	private final UniverseCoordinates coordinates = new UniverseCoordinates();
	private int starSystemIndex;

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

		camera = new CenterPerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(GameState.INSTANCE.position);
		camera.direction.set(GameState.INSTANCE.direction);
		camera.up.set(GameState.INSTANCE.up);
		camera.near = 0.001f;
		camera.far = 400f;
		camera.update(true);

		player = new Player(ShipFactory.getStandardShip(), camera);
		shipUserInterface = new ShipUserInterface(infiniteSpaceGame, skin, this, player, camera);
		shipUserInterface.starSystemIndex = GameState.INSTANCE.starSystem;
		
		renderState.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.1f, 0.1f, 0.1f, 1f));
		
		starSystemIndex = Integer.MAX_VALUE;
		updateUniverse();
		shipUserInterface.setZoomObject(renderState.nodeToInstances);
	}
	
	@Override
	public void hide () {
		super.hide();

		save();
	}
	
	private void save() {
		GameState.INSTANCE.position.set(camera.position);
		GameState.INSTANCE.position.add(camera.positionOffset);
		
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

		Vector3 cameraPosition = new Vector3(camera.position).scl(-1);
		camera.recenter();
		renderState.recenter(cameraPosition);

		{
			camera.near = 100f;
			camera.far = 900f;
			camera.update(true);
	
			modelBatch.render(renderState.instancesFar);
			modelBatch.flush();
		}
		
		{
			camera.near = 0.01f;
			camera.far = 1000f;
			camera.update(true);
	
			// always render these instances
			modelBatch.render(renderState.instancesAlways, renderState.environment);
	
			modelBatch.flush();
		}

		{
			if(shipUserInterface.hyperspaceMode) {
				camera.near = 0.0001f;				
			} else {
				camera.near = 0.0000001f;
			}
			//camera.near = nearest.len() / 2;
			camera.far = 300f;
			camera.update(true);
	
			// render these instances only if visible
			if (USE_FRUSTUM_CULLING) {
				for(Map.Entry<Node, Array<ModelInstance>> entry : renderState.nodeToInstances.entrySet()) {
					Array<ModelInstance> nodeInstances = entry.getValue();
					for (final ModelInstance instance : nodeInstances) {
						if (isVisible(camera, instance)) {
							modelBatch.render(instance, renderState.environment);
						}
					}
				}
			} else {
				for(Map.Entry<Node, Array<ModelInstance>> entry : renderState.nodeToInstances.entrySet()) {
					Array<ModelInstance> nodeInstances = entry.getValue();
					modelBatch.render(nodeInstances, renderState.environment);
				}
			}
			modelBatch.flush();
		}
		
		modelBatch.end();

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
			infiniteSpaceGame.genericNodeConverter.createSkyBox(renderState);
			shipUserInterface.setUniverse(universe);
			shipUserInterface.setZoomObject(renderState.nodeToInstances);
			changed = true;
		}
		
		coordinates.starSystem = camera.position;
		infiniteSpaceGame.universeModel.setPosition(coordinates);
		
		return changed;
	}

	private void disposeInstances () {
		renderState.nodeToInstances.clear(); // FIXME correct?
		renderState.instancesAlways.clear();
		renderState.instancesFar.clear();
	}

	private final Vector3 positionForIsVisible = new Vector3();

	private boolean isVisible (final Camera cam, final ModelInstance instance) {
		instance.transform.getTranslation(positionForIsVisible);
		return cam.frustum.pointInFrustum(positionForIsVisible);
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
