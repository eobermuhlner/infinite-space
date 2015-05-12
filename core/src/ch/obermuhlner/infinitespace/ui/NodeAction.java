package ch.obermuhlner.infinitespace.ui;

import ch.obermuhlner.infinitespace.AbstractGameScreen;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.model.Node;

public interface NodeAction {

	boolean isValid(Node node);
	
	void execute(InfiniteSpaceGame game, Node node, AbstractGameScreen fromScreen);
}
