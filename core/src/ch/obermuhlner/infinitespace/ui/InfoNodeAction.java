package ch.obermuhlner.infinitespace.ui;

import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.model.Node;
import ch.obermuhlner.infinitespace.ui.info.InfoScreen;

public class InfoNodeAction implements NodeAction {

	public static final InfoNodeAction INSTANCE = new InfoNodeAction();
	
	@Override
	public boolean isValid (Node node) {
		return true;
	}

	@Override
	public void execute (InfiniteSpaceGame game, Node node, AbstractGameScreen fromScreen) {
		game.setScreen(new InfoScreen(game, node, fromScreen));
	}

}
