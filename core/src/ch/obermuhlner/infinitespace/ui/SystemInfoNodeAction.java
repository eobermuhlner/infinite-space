package ch.obermuhlner.infinitespace.ui;

import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.ShipUserInterface;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.universe.StarSystem;
import ch.obermuhlner.infinitespace.ui.game.StarSystemScreen;

public class SystemInfoNodeAction implements NodeAction {

	public static final SystemInfoNodeAction INSTANCE = new SystemInfoNodeAction();
	
	@Override
	public boolean isValid (Node node) {
		return true;
	}

	@Override
	public void execute (InfiniteSpaceGame game, ShipUserInterface shipUserInterface, Node node, AbstractGameScreen fromScreen) {
		StarSystem starSystem = findStarSystem(node);
		if (starSystem != null) {
			game.setScreen(new StarSystemScreen(game, starSystem));
		}
	}

	private StarSystem findStarSystem(Node node) {
		Node current = node;
		while (current != null && !(current instanceof StarSystem)) {
			current = current.parent;
		}
		return (StarSystem) current;
	}

}
