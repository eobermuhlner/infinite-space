package ch.obermuhlner.infinitespace.ui;

import ch.obermuhlner.infinitespace.InfiniteSpaceGame;
import ch.obermuhlner.infinitespace.ui.welcome.OptionsScreen;
import ch.obermuhlner.infinitespace.ui.welcome.WelcomeScreen;


public class AbstractInfiniteSpaceGameScreen extends AbstractGameScreen {

	protected InfiniteSpaceGame infiniteSpaceGame;

	public AbstractInfiniteSpaceGameScreen (InfiniteSpaceGame game) {
		super(game);
		
		infiniteSpaceGame = game;
	}
	
	protected void showWelcomeScreen() {
		infiniteSpaceGame.setScreen(new WelcomeScreen(infiniteSpaceGame));
	}

	protected void showOptionsScreen() {
		infiniteSpaceGame.setScreen(new OptionsScreen(infiniteSpaceGame));
	}

}
