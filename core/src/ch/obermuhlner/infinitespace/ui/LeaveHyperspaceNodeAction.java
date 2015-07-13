package ch.obermuhlner.infinitespace.ui;

import com.badlogic.gdx.math.Vector3;

import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.NodeToRenderConverter;
import ch.obermuhlner.infinitespace.ShipUserInterface;
import ch.obermuhlner.infinitespace.model.Node;

public class LeaveHyperspaceNodeAction implements NodeAction {

	public static final LeaveHyperspaceNodeAction INSTANCE = new LeaveHyperspaceNodeAction();
	
	@Override
	public boolean isValid (Node node) {
		return true;
	}

	@Override
	public void execute (InfiniteSpaceGame game, ShipUserInterface shipUserInterface, Node node, AbstractGameScreen fromScreen) {
		float radius = NodeToRenderConverter.calculateRadius(node);
		
		Vector3 nodePos = NodeToRenderConverter.calculatePosition(node);
		nodePos.sub(shipUserInterface.player.camera.positionOffset);
		Vector3 playerPos = new Vector3(nodePos);
		playerPos.add(0, 0, radius * 3);
		shipUserInterface.player.camera.position.set(playerPos);
		shipUserInterface.player.camera.lookAt(nodePos);
		shipUserInterface.player.camera.update();

		shipUserInterface.setHyperspaceMode(false);
	}

}
