package ch.obermuhlner.infinitespace;

import ch.obermuhlner.infinitespace.model.Node;

import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;

public abstract class AbstractNodeStageScreen extends AbstractStageScreen {

	protected Node node;

	private Vector3 target = new Vector3();
	protected float autoRotateAngle = 5.0f;
	
	public AbstractNodeStageScreen (InfiniteSpaceGame game, Node node) {
		super(game);
		
		this.node = node;
	}

	@Override
	protected void prepareRenderState (RenderState renderState) {
		super.prepareRenderState(renderState);
		
		float radius = NodeToRenderConverter.calculateRadius(node);

		camera.near = radius / 100;
		camera.far = radius * 100;
		camera.position.set(radius*1, radius*1, radius*1);
		camera.update(true);
		
		cameraInputController.translateUnits = radius;
		
		renderState.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
		
		infiniteSpaceGame.genericNodeConverter.convertNode(node, renderState);
	}
	
	@Override
	public void render (float delta) {
		super.render(delta);
		
		camera.rotateAround(target, Vector3.Y, delta * autoRotateAngle);
		camera.update();
	}
}
