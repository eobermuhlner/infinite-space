package ch.obermuhlner.infinitespace.ui;

import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.ShipUserInterface;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.model.universe.Planet;
import ch.obermuhlner.infinitespace.model.universe.SpaceStation;
import ch.obermuhlner.infinitespace.ui.land.LandScreen;

public class LandNodeAction implements NodeAction {

	public static final LandNodeAction INSTANCE = new LandNodeAction();
	
	@Override
	public boolean isValid (Node node) {
		return node instanceof Planet || node instanceof SpaceStation;
	}

	@Override
	public void execute (InfiniteSpaceGame game, ShipUserInterface shipUserInterface, Node node, AbstractGameScreen fromScreen) {
		game.setScreen(new LandScreen(game, node));
	}

}
