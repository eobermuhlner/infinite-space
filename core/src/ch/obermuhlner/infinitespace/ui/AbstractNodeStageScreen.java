package ch.obermuhlner.infinitespace.ui;

import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.NodeToRenderConverter;
import ch.obermuhlner.infinitespace.RenderState;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.ui.game.GameScreen.RenderMode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;

public abstract class AbstractNodeStageScreen extends AbstractStageScreen {

	private final NodeToRenderConverter nodeToRenderConverter;

	protected final Node node;

	private Vector3 target = new Vector3();
	protected float autoRotateAngle = 5.0f;
	
	private final PointLight pointLight = new PointLight();

	private RenderState renderState2;
	
	public AbstractNodeStageScreen (InfiniteSpaceGame game, Node node) {
		super(game);
		
		this.nodeToRenderConverter = new NodeToRenderConverter(game.assetManager, RenderMode.HYPERSPACE.sizeFactor);
		this.node = node;
	}

	@Override
	protected void prepareRenderState (RenderState renderState) {
		renderState2 = renderState;
		super.prepareRenderState(renderState);
		
		float radius = nodeToRenderConverter.calculateRadius(node);

		camera.near = radius / 100;
		camera.far = radius * 100;
		camera.position.set(radius*2, radius*2, radius*2);
		camera.update(true);
		
		cameraInputController.translateUnits = radius;
		
		pointLight.set(1f, 1f, 1f, -5, 0, 5, 1f);
		renderState.environment.add(pointLight);
		
		nodeToRenderConverter.convertNode(node, renderState);
	}

	@Override
	protected void prepareStage(Stage stage) {
		super.prepareStage(stage);
		
		Window window = new Window("Light", skin);
		
		{
			// ambientLight luminosity
			final Slider slider = new Slider(0.0f, 1.0f, 0.01f, true, skin);
			slider.setValue(0.0f);
			setAmbientLightLuminosity(slider.getValue());
			slider.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					setAmbientLightLuminosity(slider.getValue());
				}
			});
			window.add(slider);
		}

		{
			// pointLight luminosity
			final Slider slider = new Slider(0.0f, 2.0f, 0.01f, true, skin);
			slider.setValue(1.0f);
			setPointLightLuminosity(slider.getValue());
			slider.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					setPointLightLuminosity(slider.getValue());
				}
			});
			window.add(slider);
		}

		{
			// point light angle
			final Slider slider = new Slider((float)(-Math.PI), (float)(3*Math.PI), 0.01f, true, skin);
			slider.setValue(0.0f);
			updatePointLightAngle(slider.getValue());
			slider.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					updatePointLightAngle(slider.getValue());
				}
			});
			window.add(slider);
		}

		window.pack();
		window.setPosition(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Align.top | Align.right);
	
		stage.addActor(window);
	}
	
	private void setPointLightLuminosity(float luminosity) {
		pointLight.intensity = luminosity;
	}
	
	private void updatePointLightAngle(float angle) {
		float x = (float) (Math.sin(angle) * 5);
		float y = 0;
		float z = (float) (Math.cos(angle) * 5);
		pointLight.position.set(x, y, z);
	}
	
	private void setAmbientLightLuminosity(float luminosity) {
		renderState2.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, luminosity, luminosity, luminosity, 1f));
	}

	@Override
	public void render (float delta) {
		super.render(delta);
		
		camera.rotateAround(target, Vector3.Y, delta * autoRotateAngle);
		camera.update();
	}
}
