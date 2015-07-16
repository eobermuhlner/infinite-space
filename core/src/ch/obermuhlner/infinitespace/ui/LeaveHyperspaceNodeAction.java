package ch.obermuhlner.infinitespace.ui;

import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.ShipUserInterface;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.ui.game.GameScreen;
import ch.obermuhlner.infinitespace.ui.game.GameScreen.RenderMode;

import com.badlogic.gdx.math.Vector3;

public class LeaveHyperspaceNodeAction implements NodeAction {

	public static final LeaveHyperspaceNodeAction INSTANCE = new LeaveHyperspaceNodeAction();
	
	@Override
	public boolean isValid (Node node) {
		return true;
	}

	@Override
	public void execute (InfiniteSpaceGame game, ShipUserInterface shipUserInterface, Node node, AbstractGameScreen fromScreen) {
		float radius = shipUserInterface.nodeToRenderConverter.calculateRadius(node);
		
		Vector3 nodePos = shipUserInterface.nodeToRenderConverter.calculatePosition(node);
		nodePos.sub(shipUserInterface.player.camera.positionOffset);
		Vector3 playerPos = new Vector3(nodePos);
		playerPos.add(0, 0, radius * 3);
		
		shipUserInterface.player.camera.position.set(playerPos);
		shipUserInterface.player.camera.lookAt(nodePos);
		shipUserInterface.player.camera.update();
		
		game.setScreen(new GameScreen(game, RenderMode.NORMALSPACE));
	}

}
