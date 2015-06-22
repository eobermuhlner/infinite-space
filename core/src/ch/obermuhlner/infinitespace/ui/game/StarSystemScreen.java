package ch.obermuhlner.infinitespace.ui.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.utils.Array;

import ch.obermuhlner.infinitespace.GameState;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.RenderState;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.universe.StarSystem;
import ch.obermuhlner.infinitespace.render.UberShaderProvider;
import ch.obermuhlner.infinitespace.ui.AbstractInfiniteSpaceGameScreen;

public class StarSystemScreen extends AbstractInfiniteSpaceGameScreen {

	private final RenderState renderState = new RenderState();

	private ModelBatch modelBatch;

	private PerspectiveCamera camera;

	public StarSystemScreen (InfiniteSpaceGame game, StarSystem starSystem) {
		super(game);
		
		loadNodes(starSystem);
	}

	private void loadNodes(Node node) {
		infiniteSpaceGame.genericNodeConverter.convertNode(node, renderState);
		
		int childCount = infiniteSpaceGame.universeModel.generator.getChildCount(node);
		for (int i = 0; i < childCount; i++) {
			
		}
	}

	@Override
	public void show() {
		super.show();
		
		modelBatch = new ModelBatch(UberShaderProvider.DEFAULT);

		camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(GameState.INSTANCE.position);
		camera.direction.set(GameState.INSTANCE.direction);
		camera.up.set(GameState.INSTANCE.up);
		camera.near = 0.001f;
		camera.far = 400f;
		camera.update(true);
		
		renderState.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.1f, 0.1f, 0.1f, 1f));
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		modelBatch.begin(camera);

		modelBatch.render(renderState.instancesAlways, renderState.environment);
		for(Array<ModelInstance> nodeInstances : renderState.nodeToInstances.values()) {
			modelBatch.render(nodeInstances, renderState.environment);
		}

		modelBatch.flush();

		modelBatch.end();
	}
}
