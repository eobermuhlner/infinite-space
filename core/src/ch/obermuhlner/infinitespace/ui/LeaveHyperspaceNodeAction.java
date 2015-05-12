package ch.obermuhlner.infinitespace.ui;

import ch.obermuhlner.infinitespace.AbstractGameScreen;
import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.model.Node;

public class LeaveHyperspaceNodeAction implements NodeAction {

	public static final LeaveHyperspaceNodeAction INSTANCE = new LeaveHyperspaceNodeAction();
	
	@Override
	public boolean isValid (Node node) {
		return true;
	}

	@Override
	public void execute (InfiniteSpaceGame game, Node node, AbstractGameScreen fromScreen) {
		// TODO Auto-generated method stub
		
	}

}
